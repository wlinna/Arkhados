/*    This file is part of Arkhados.

 Arkhados is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Arkhados is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Arkhados.  If not, see <http://www.gnu.org/licenses/>. */
package arkhados.gamemode;

import arkhados.CharacterInteraction;
import arkhados.ClientMain;
import arkhados.Globals;
import arkhados.MusicManager;
import arkhados.PlayerData;
import arkhados.ServerFogManager;
import arkhados.SyncManager;
import arkhados.Topic;
import arkhados.UserCommandManager;
import arkhados.WorldManager;
import arkhados.controls.PlayerEntityAwareness;
import arkhados.messages.ClientSelectHeroCommand;
import arkhados.messages.PlayerKillCommand;
import arkhados.messages.SetPlayersCharacterCommand;
import arkhados.messages.TopicOnlyCommand;
import arkhados.net.ClientSender;
import arkhados.net.Command;
import arkhados.net.CommandHandler;
import arkhados.net.Receiver;
import arkhados.net.Sender;
import arkhados.net.ServerSender;
import arkhados.ui.hud.ClientHudManager;
import arkhados.ui.hud.DeathMatchHeroSelectionLayerBuilder;
import arkhados.ui.hud.DeathMatchHeroSelectionLayerController;
import arkhados.ui.hud.ServerClientDataStrings;
import arkhados.util.NodeBuilderIdHeroNameMatcherSingleton;
import arkhados.util.PlayerDataStrings;
import arkhados.util.RemovalReasons;
import arkhados.util.Timer;
import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.HostedConnection;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

/**
 *
 * @author william
 */
public class DeathMatch extends GameMode implements CommandHandler {

    private static final Logger logger = Logger.getLogger(DeathMatch.class.getName());
    private WorldManager worldManager;
    private AppStateManager stateManager;
    private SyncManager syncManager;
    private int spawnLocationIndex = 0;
    private final HashMap<Integer, Timer> spawnTimers = new HashMap<>();
    private Nifty nifty;
    private int killLimit = 15;
    private final HashMap<Integer, Integer> killingSprees = new HashMap<>();
    private Element heroSelectionLayer;
    private HashMap<Integer, Boolean> canPickHeroMap = new HashMap<>();

    @Override
    public void initialize(Application app) {
        super.initialize(app);
        stateManager = getApp().getStateManager();
        worldManager = stateManager.getState(WorldManager.class);
        syncManager = stateManager.getState(SyncManager.class);
        stateManager.getState(Receiver.class).registerCommandHandler(this);

        CharacterInteraction.startNewRound();

        syncManager.addObject(-1, worldManager);
        if (stateManager.getState(Sender.class).isServer()) {
            syncManager.setEnabled(true);
            syncManager.startListening();
            Globals.worldRunning = true;
        } else {
            stateManager.getState(UserCommandManager.class).setEnabled(true);
        }
    }

    @Override
    public void startGame() {
        getApp().enqueue(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                worldManager.setEnabled(true);
                worldManager.loadLevel();
                worldManager.attachLevel();

                Sender sender = stateManager.getState(Sender.class);

                if (sender.isClient()) {
                    nifty.gotoScreen("default_hud");
                    heroSelectionLayer.show();
                    sender.addCommand(new TopicOnlyCommand(Topic.CLIENT_WORLD_CREATED));
                } else if (sender.isServer()) {
                    syncManager.setEnabled(true);
                    syncManager.startListening();
                }
                return null;
            }
        });
    }

    @Override
    public void update(float tpf) {
        for (Timer timer : spawnTimers.values()) {
            timer.update(tpf);
        }
    }

    @Override
    public void playerJoined(int playerId) {
        Timer timer = new Timer(6);
        timer.setTimeLeft(0.5f);
        spawnTimers.put(playerId, timer);
        timer.setActive(true);

        ServerFogManager fogManager = stateManager.getState(ServerFogManager.class);
        if (fogManager != null) { // Same as asking for if this is server
            PlayerEntityAwareness awareness = fogManager.createAwarenessForPlayer(playerId);
            fogManager.teachAboutPrecedingEntities(awareness);

            canPickHeroMap.put(playerId, Boolean.TRUE);
            CharacterInteraction.addPlayer(playerId);
        }

        killingSprees.put(playerId, 0);
    }

    private void playerChoseHero(final int playerId, final String heroName) {
        Boolean canPickHero = canPickHeroMap.get(playerId);
        if (canPickHero == null || canPickHero == Boolean.FALSE) {
            return;
        }
        canPickHeroMap.put(playerId, Boolean.FALSE);

        long delay = (long) spawnTimers.get(playerId).getTimeLeft() * 1000;
        if (delay < 0) {
            delay = 100;
        }

        final Callable<Void> callable =
                new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                int oldEntityId = PlayerData.getIntData(playerId, PlayerDataStrings.ENTITY_ID);
                worldManager.removeEntity(oldEntityId, RemovalReasons.DEATH);

                Vector3f startingLocation = getNewSpawnLocation();
                PlayerData playerData = PlayerData.getPlayerId(playerId);

                int nodeBuilderId = NodeBuilderIdHeroNameMatcherSingleton.get().getId(heroName);
                int entityId = worldManager.addNewEntity(nodeBuilderId, startingLocation,
                        new Quaternion(), playerId);
                playerData.setData(PlayerDataStrings.ENTITY_ID, entityId);

                SetPlayersCharacterCommand playersCharacterCommand =
                        new SetPlayersCharacterCommand(entityId, playerId);

                stateManager.getState(ServerSender.class).addCommand(playersCharacterCommand);
                return null;
            }
        };

        new java.util.Timer().schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                getApp().enqueue(callable);
            }
        }, delay);
    }

    private Vector3f getNewSpawnLocation() {
        spawnLocationIndex = (spawnLocationIndex + 1) % WorldManager.STARTING_LOCATIONS.length;
        return WorldManager.STARTING_LOCATIONS[spawnLocationIndex].clone().setY(1f);
    }

    @Override
    public void playerDied(int playerId, int killersPlayerId) {
        canPickHeroMap.put(playerId, Boolean.TRUE);

        Sender sender = stateManager.getState(ServerSender.class);

        int killingSpree = 0;
        
        if (killersPlayerId > -1) {
            killingSpree = killingSprees.get(killersPlayerId) + 1;
            killingSprees.put(killersPlayerId, killingSpree);
        }

        sender.addCommand(new PlayerKillCommand(playerId, killersPlayerId, killingSpree));
        spawnTimers.get(playerId).setTimeLeft(6f);
        int kills = CharacterInteraction.getCurrentRoundStats().getKills(killersPlayerId);

        if (kills >= killLimit) {
            sender.addCommand(new TopicOnlyCommand(Topic.GAME_ENDED));
        }
    }

    private void clientPlayerDied(int playerId, int killersId, int killingSpree) {
        killingSprees.put(playerId, 0);
        killingSprees.put(killersId, killingSpree);
        int myPlayerId = stateManager.getState(UserCommandManager.class).getPlayerId();

        if (playerId == myPlayerId) {
            handleOwnDeath();
        }
    }

    private void handleOwnDeath() {
        getApp().enqueue(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                UserCommandManager userCommandManager =
                        stateManager.getState(UserCommandManager.class);
                int characterId = userCommandManager.getCharacterId();
                worldManager.removeEntity(characterId, spawnLocationIndex); // TODO: Get rid of this
                userCommandManager.nullifyCharacter();
                ClientHudManager hudManager = stateManager.getState(ClientHudManager.class);
                hudManager.clearAllButHpBars();
                hudManager.showRoundStatistics();
                heroSelectionLayer.showWithoutEffects();
                return null;
            }
        });
    }

    @Override
    public void readGuaranteed(Object source, List<Command> guaranteed) {
        if (source instanceof HostedConnection) {
            serverReadGuaranteed((HostedConnection) source, guaranteed);
        } else {
            clientReadGuaranteed(guaranteed);
        }
    }

    private void serverReadGuaranteed(HostedConnection source, List<Command> guaranteed) {
        for (Command command : guaranteed) {
            if (command instanceof ClientSelectHeroCommand) {
                int playerId = source.getAttribute(ServerClientDataStrings.PLAYER_ID);
                playerChoseHero(playerId, ((ClientSelectHeroCommand) command).getHeroName());
            }
        }
    }

    private void clientReadGuaranteed(List<Command> guaranteed) {
        for (Command command : guaranteed) {
            if (command instanceof PlayerKillCommand) {
                PlayerKillCommand pkCommand = (PlayerKillCommand) command;
                clientPlayerDied(pkCommand.getDiedPlayerId(), pkCommand.getKillerPlayerId(),
                        pkCommand.getKillingSpree());
            } else if (command instanceof TopicOnlyCommand) {
                clientHandleTopicOnlyCommand((TopicOnlyCommand) command);
            }
        }
    }

    @Override
    public void readUnreliable(Object source, List<Command> unreliables) {
    }

    public void setNifty(Nifty nifty) {
        this.nifty = nifty;

        DeathMatchHeroSelectionLayerBuilder layerBuilder =
                new DeathMatchHeroSelectionLayerBuilder();

        Screen screen = nifty.getScreen("default_hud");

        heroSelectionLayer = layerBuilder.build(nifty, screen, screen.getRootElement());

        DeathMatchHeroSelectionLayerController control =
                heroSelectionLayer.getControl(DeathMatchHeroSelectionLayerController.class);
        control.setStateManager(stateManager);
    }

    @Override
    public void cleanup() {
        super.cleanup();
        stateManager.getState(MusicManager.class).setPlaying(false);
    }

    private void clientHandleTopicOnlyCommand(TopicOnlyCommand command) {
        switch (command.getTopicId()) {
            case Topic.GAME_ENDED:
                gameEnded();
                break;
        }
    }

    @Override
    public void gameEnded() {
        final Sender sender = stateManager.getState(Sender.class);

        if (sender.isClient()) {

            final ClientHudManager hudManager = stateManager.getState(ClientHudManager.class);
            getApp().enqueue(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    hudManager.clear();
                    hudManager.showRoundStatistics();
                    nifty.removeElement(nifty.getScreen("default_hud"), heroSelectionLayer);
                    return null;
                }
            });

            getApp().enqueue(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    stateManager.getState(SyncManager.class).clear();
                    // TODO: Find out why following line causes statistics to not appear
//                    stateManager.getState(UserCommandManager.class).nullifyCharacter();
                    stateManager.getState(ClientHudManager.class).disableCharacterHudControl();
                    return null;
                }
            });

            final Callable<Void> callable =
                    new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    ((ClientSender) sender).getClient().close();

                    PlayerData.destroyAllData();
                    hudManager.endGame();
                    stateManager.getState(WorldManager.class).clear();
                    stateManager.getState(UserCommandManager.class).nullifyCharacter();
                    ((ClientMain) getApp()).gameEnded();
                    killingSprees.clear();
                    return null;
                }
            };

            new java.util.Timer().schedule(new java.util.TimerTask() {
                @Override
                public void run() {
                    getApp().enqueue(callable);
                }
            }, 15000);
        }
    }
}