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
import arkhados.net.Command;
import arkhados.net.CommandHandler;
import arkhados.net.Receiver;
import arkhados.net.Sender;
import arkhados.net.ServerSender;
import arkhados.ui.HeroSelectionBuilder;
import arkhados.ui.hud.ClientHudManager;
import arkhados.ui.hud.DeathMatchHeroSelectionScreenController;
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
import com.jme3.scene.Spatial;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.builder.LayerBuilder;
import de.lessvoid.nifty.builder.ScreenBuilder;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
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
    private ScreenController heroSelectionScreenController = null;

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
                    nifty.gotoScreen("deathmatch-hero-selection");
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
    }

    @Override
    public void playerJoined(int playerId) {
        spawnTimers.put(playerId, new Timer(0));
        ServerFogManager fogManager = stateManager.getState(ServerFogManager.class);
        if (fogManager != null) {
            PlayerEntityAwareness awareness = fogManager.createAwarenessForPlayer(playerId);
            fogManager.teachAboutPrecedingEntities(awareness);
        }
    }

    private void playerChoseHero(final int playerId, final String heroName) {
        long delay = (long) spawnTimers.get(playerId).getTimeLeft() * 1000;
        if (delay < 0) {
            delay = 0;
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
        Sender sender = stateManager.getState(Sender.class);

        if (sender.isServer()) {
            ServerSender serverSender = (ServerSender) sender;
            serverSender.addCommand(new PlayerKillCommand(playerId, killersPlayerId));
            spawnTimers.get(playerId).setTimeLeft(5f);
        } else if (sender.isClient()) {
            int myPlayerId = stateManager.getState(UserCommandManager.class).getPlayerId();
            if (playerId == myPlayerId) {
                handleOwnDeath();
            }
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
                stateManager.getState(ClientHudManager.class).clear();
                nifty.gotoScreen("deathmatch-hero-selection");
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
                playerDied(pkCommand.getDiedPlayerId(), pkCommand.getKillerPlayerId());
            }
        }
    }

    @Override
    public void readUnreliable(Object source, List<Command> unreliables) {
    }

    public void setNifty(Nifty nifty) {
        this.nifty = nifty;
        heroSelectionScreenController = new DeathMatchHeroSelectionScreenController(getApp());
        nifty.registerScreenController();
        String id = "deathmatch-hero-selection";
        Screen screen = new DeathmatchHeroSelectionScreenBuilder(id, heroSelectionScreenController)
                .build(nifty);
        nifty.addScreen(id, screen);
    }

    @Override
    public void cleanup() {
        super.cleanup();
        nifty.removeScreen("deathmatch-hero-selection");
        nifty.unregisterScreenController(heroSelectionScreenController);
        stateManager.getState(MusicManager.class).setPlaying(false);
    }
}

class DeathmatchHeroSelectionScreenBuilder extends ScreenBuilder {

    public DeathmatchHeroSelectionScreenBuilder(String id, ScreenController controller) {
        super(id, controller);
        layer(new LayerBuilder() {
            {
                childLayoutCenter();
                panel(new HeroSelectionBuilder());
            }
        });
    }
}