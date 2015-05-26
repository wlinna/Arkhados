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
import arkhados.effects.DeathManager;
import arkhados.messages.CmdSelectHero;
import arkhados.messages.CmdPlayerKill;
import arkhados.messages.CmdSetPlayersCharacter;
import arkhados.messages.CmdTopicOnly;
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
import arkhados.util.AudioQueue;
import arkhados.util.NodeBuilderIdHeroNameMatcherSingleton;
import arkhados.util.PlayerDataStrings;
import arkhados.util.RemovalReasons;
import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.audio.AudioNode;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.HostedConnection;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

/**
 *
 * @author william
 */
public class DeathMatch extends GameMode implements CommandHandler {

    private static final Map<Integer, String> spreeAnnouncements =
            new HashMap<>();
    private static final Map<Integer, String> comboAnnouncements =
            new HashMap<>();
    private static final String FIRST_BLOOD_PATH =
            "Interface/Sound/Announcer/FirstBlood.wav";

    static {
        spreeAnnouncements.put(3, "Interface/Sound/Announcer/KillingSpree.wav");
        spreeAnnouncements.put(4, "Interface/Sound/Announcer/MegaKill.wav");
        spreeAnnouncements.put(5, "Interface/Sound/Announcer/Dominating.wav");
        spreeAnnouncements.put(6, "Interface/Sound/Announcer/Ownage.wav");
        spreeAnnouncements.put(7, "Interface/Sound/Announcer/Mayhem.wav");
        spreeAnnouncements.put(8, "Interface/Sound/Announcer/Carnage.wav");
        spreeAnnouncements.put(9, "Interface/Sound/Announcer/Godlike.wav");

        comboAnnouncements.put(2, "Interface/Sound/Announcer/DoubleKill.wav");
        comboAnnouncements.put(3, "Interface/Sound/Announcer/TripleKill.wav");
        comboAnnouncements.put(4, "Interface/Sound/Announcer/Rampage.wav");
        comboAnnouncements.put(5, "Interface/Sound/Announcer/Massacre.wav");
    }
    private static final Logger logger =
            Logger.getLogger(DeathMatch.class.getName());
    private WorldManager worldManager;
    private AppStateManager stateManager;
    private SyncManager syncManager;
    private int spawnLocationIndex = 0;
    private Nifty nifty;
    private int killLimit = 25;
    private final HashMap<Integer, DeathMatchPlayerTracker> trackers =
            new HashMap<>();
    private Element heroSelectionLayer;
    private HashMap<Integer, Boolean> canPickHeroMap = new HashMap<>();
    private boolean firstBloodHappened;
    private AudioQueue audioQueue = new AudioQueue();

    @Override
    public void initialize(Application app) {
        super.initialize(app);
        stateManager = getApp().getStateManager();
        worldManager = stateManager.getState(WorldManager.class);
        syncManager = stateManager.getState(SyncManager.class);
        stateManager.getState(Receiver.class).registerCommandHandler(this);

        CharacterInteraction.startNewRound();

        syncManager.addObject(-1, worldManager);

        firstBloodHappened = false;

        if (stateManager.getState(Sender.class).isServer()) {
            syncManager.setEnabled(true);
            syncManager.startListening();
            Globals.worldRunning = true;
        } else {
            stateManager.getState(UserCommandManager.class).setEnabled(true);
            stateManager.getState(ClientHudManager.class).clearMessages();

            preloadAnnouncer();
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

                if (sender.isClient() && !Globals.replayMode) {
                    nifty.gotoScreen("default_hud");
                    heroSelectionLayer.show();
                    sender.addCommand(
                            new CmdTopicOnly(Topic.CLIENT_WORLD_CREATED));
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
        audioQueue.update();

        for (DeathMatchPlayerTracker tracker : trackers.values()) {
            tracker.update(tpf);
        }
    }

    @Override
    public void playerJoined(final int playerId) {

        Globals.app.enqueue(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                DeathMatchPlayerTracker tracker =
                        new DeathMatchPlayerTracker(0.5f);
                trackers.put(playerId, tracker);

                ServerFogManager fogManager =
                        stateManager.getState(ServerFogManager.class);
                if (fogManager != null) { // Same as asking for if this is server
                    PlayerEntityAwareness awareness =
                            fogManager.createAwarenessForPlayer(playerId);
                    fogManager.teachAboutPrecedingEntities(awareness);

                    canPickHeroMap.put(playerId, Boolean.TRUE);
                    CharacterInteraction.addPlayer(playerId);
                }
                return null;
            }
        });
    }

    private void playerChoseHero(final int playerId, final String heroName) {
        Boolean canPickHero = canPickHeroMap.get(playerId);
        if (canPickHero == null || canPickHero == Boolean.FALSE) {
            return;
        }
        canPickHeroMap.put(playerId, Boolean.FALSE);

        long delay = (long) trackers.get(playerId).getSpawnTimeLeft() * 1000;
        if (delay < 0) {
            delay = 100;
        }

        final Callable<Void> callable =
                new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                int oldEntityId = PlayerData
                        .getIntData(playerId, PlayerDataStrings.ENTITY_ID);
                worldManager.removeEntity(oldEntityId, RemovalReasons.DEATH);

                Vector3f startingLocation = getNewSpawnLocation();
                PlayerData playerData = PlayerData.getPlayerId(playerId);

                int nodeBuilderId = NodeBuilderIdHeroNameMatcherSingleton
                        .get().getId(heroName);
                int entityId = worldManager
                        .addNewEntity(nodeBuilderId, startingLocation,
                        new Quaternion(), playerId);
                playerData.setData(PlayerDataStrings.ENTITY_ID, entityId);

                CmdSetPlayersCharacter playersCharacterCommand =
                        new CmdSetPlayersCharacter(entityId, playerId);

                stateManager.getState(ServerSender.class)
                        .addCommand(playersCharacterCommand);
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
        spawnLocationIndex = (spawnLocationIndex + 1)
                % WorldManager.STARTING_LOCATIONS.length;
        return WorldManager.STARTING_LOCATIONS[spawnLocationIndex].clone()
                .setY(1f);
    }

    @Override
    public void playerDied(int playerId, int killersPlayerId) {
        boolean deathByEnvironment = killersPlayerId < 0;

        DeathMatchPlayerTracker dead = trackers.get(playerId);
        int endedSpree = dead.getKillingSpree();

        dead.death(6f, deathByEnvironment);

        canPickHeroMap.put(playerId, Boolean.TRUE);

        Sender sender = stateManager.getState(ServerSender.class);

        int killingSpree = 0;
        int combo = 0;

        if (!deathByEnvironment) {
            DeathMatchPlayerTracker killer = trackers.get(killersPlayerId);
            killer.addKill();

            killingSpree = killer.getKillingSpree();
            combo = killer.getCombo();
        }

        sender.addCommand(new CmdPlayerKill(playerId, killersPlayerId,
                killingSpree, combo, endedSpree));

        int kills = CharacterInteraction.getCurrentRoundStats()
                .getKills(killersPlayerId);

        firstBloodHappened = true;

        if (kills >= killLimit) {
            sender.addCommand(new CmdTopicOnly(Topic.GAME_ENDED));
        }
    }

    private void clientPlayerDied(int playerId, int killersId,
            int killingSpree, int combo, int endedSpree) {
        int myPlayerId =
                stateManager.getState(UserCommandManager.class).getPlayerId();

        String playerName = getPlayerName(playerId);
        String killerName = getPlayerName(killersId);

        killedMessage(playerName, killerName, endedSpree);
        firstBloodMessage(killersId);
        comboMessage(killerName, combo);
        killingSpreeMessage(killerName, killingSpree);

        if (playerId == myPlayerId) {
            handleOwnDeath();
        }
    }

    private void killedMessage(String playerName, String killerName,
            int endedSpree) {
        String message = DeathMatchMessageMaker
                .killed(playerName, killerName, endedSpree);
        stateManager.getState(ClientHudManager.class).addMessage(message);
    }

    private void firstBloodMessage(int killersId) {
        if (firstBloodHappened || killersId < 0) {
            return;
        }

        firstBloodHappened = true;

        String name = getPlayerName(killersId);

        String message = String.format("%s just drew First Blood!", name);
        stateManager.getState(ClientHudManager.class).addMessage(message);

        playAnnouncerSound(FIRST_BLOOD_PATH);
    }

    private void killingSpreeMessage(String playerName, int spree) {
        if (spree < 3) {
            return;
        } else if (spree > 9) {
            spree = 9;
        }

        String message = DeathMatchMessageMaker.spree(playerName, spree);
        stateManager.getState(ClientHudManager.class).addMessage(message);

        String audioPath = spreeAnnouncements.get(spree);
        playAnnouncerSound(audioPath);
    }

    private void comboMessage(String playerName, int combo) {
        if (combo < 2) {
            return;
        } else if (combo > 5) {
            combo = 5;
        }

        String message = DeathMatchMessageMaker.combo(playerName, combo);
        stateManager.getState(ClientHudManager.class).addMessage(message);

        String audioPath = comboAnnouncements.get(combo);
        playAnnouncerSound(audioPath);
    }

    private String getPlayerName(int id) {
        return id < 0
                ? "Environment"
                : PlayerData.getStringData(id, PlayerDataStrings.NAME);
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
                ClientHudManager hudManager =
                        stateManager.getState(ClientHudManager.class);
                hudManager.clearAllButCharactersInfo();
                hudManager.showStatistics();
                stateManager.getState(DeathManager.class).death();
                if (!Globals.replayMode) {
                    heroSelectionLayer.showWithoutEffects();
                }
                return null;
            }
        });
    }

    @Override
    public void readGuaranteed(Object source, Command guaranteed) {
        if (source instanceof HostedConnection) {
            serverReadGuaranteed((HostedConnection) source, guaranteed);
        } else {
            clientReadGuaranteed(guaranteed);
        }
    }

    private void serverReadGuaranteed(HostedConnection source,
            Command command) {
        if (command instanceof CmdSelectHero) {
            int playerId =
                    source.getAttribute(ServerClientDataStrings.PLAYER_ID);
            playerChoseHero(playerId,
                    ((CmdSelectHero) command).getHeroName());
            stateManager.getState(ServerSender.class)
                    .addCommandForSingle(command, source);
        } else if (command instanceof CmdTopicOnly) {
            serverHandleTopicOnlyCommand(source, (CmdTopicOnly) command);
        }
    }

    private void clientReadGuaranteed(Command command) {
        if (command instanceof CmdPlayerKill) {
            CmdPlayerKill pkCommand = (CmdPlayerKill) command;
            clientPlayerDied(pkCommand.getDiedPlayerId(),
                    pkCommand.getKillerPlayerId(), pkCommand.getKillingSpree(),
                    pkCommand.getCombo(), pkCommand.getEndedSpree());
        } else if (command instanceof CmdTopicOnly) {
            clientHandleTopicOnlyCommand((CmdTopicOnly) command);
        } else if (command instanceof CmdSelectHero) {
            String hero = ((CmdSelectHero) command).getHeroName();
            stateManager.getState(MusicManager.class).setMusicCategory(hero);
        }
    }

    @Override
    public void readUnreliable(Object source, Command unreliable) {
    }

    public void setNifty(Nifty nifty) {
        this.nifty = nifty;

        if (Globals.replayMode) {
            return;
        }

        DeathMatchHeroSelectionLayerBuilder layerBuilder =
                new DeathMatchHeroSelectionLayerBuilder();

        Screen screen = nifty.getScreen("default_hud");

        heroSelectionLayer =
                layerBuilder.build(nifty, screen, screen.getRootElement());

        DeathMatchHeroSelectionLayerController control = heroSelectionLayer
                .getControl(DeathMatchHeroSelectionLayerController.class);
        control.setStateManager(stateManager);
    }

    @Override
    public void cleanup() {
        super.cleanup();
        stateManager.getState(Receiver.class).removeCommandHandler(this);
        stateManager.getState(MusicManager.class).setPlaying(false);
    }

    private void clientHandleTopicOnlyCommand(CmdTopicOnly command) {
        switch (command.getTopicId()) {
            case Topic.GAME_ENDED:
                gameEnded();
                break;
            case Topic.FIRST_BLOOD_HAPPENED:
                firstBloodHappened = true;
                break;
        }
    }

    private void serverHandleTopicOnlyCommand(HostedConnection source,
            CmdTopicOnly command) {
        switch (command.getTopicId()) {
            case Topic.CLIENT_WORLD_CREATED:
                if (firstBloodHappened) {
                    ServerSender sender =
                            stateManager.getState(ServerSender.class);
                    sender.addCommandForSingle(
                            new CmdTopicOnly(Topic.FIRST_BLOOD_HAPPENED),
                            source);
                }
                break;
        }
    }

    @Override
    public void gameEnded() {
        final Sender sender = stateManager.getState(Sender.class);

        if (sender.isClient()) {

            final ClientHudManager hudManager =
                    stateManager.getState(ClientHudManager.class);
            getApp().enqueue(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    hudManager.clear();
                    hudManager.showStatistics();
                    nifty.removeElement(nifty.getScreen("default_hud"),
                            heroSelectionLayer);
                    return null;
                }
            });

            getApp().enqueue(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    stateManager.getState(SyncManager.class).clear();
                    // TODO: Find out why following line causes statistics to not appear
                    //  stateManager.getState(UserCommandManager.class).nullifyCharacter();
                    stateManager.getState(ClientHudManager.class)
                            .disableCharacterHudControl();
                    return null;
                }
            });

            final Callable<Void> callable =
                    new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    if (sender instanceof ClientSender) {
                        ((ClientSender) sender).getClient().close();
                    }

                    PlayerData.destroyAllData();
                    hudManager.endGame();
                    stateManager.getState(WorldManager.class).clear();
                    stateManager.getState(UserCommandManager.class)
                            .nullifyCharacter();
                    ((ClientMain) getApp()).gameEnded();
                    trackers.clear();
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

    private void preloadAnnouncer() {
        Globals.assetManager.loadAudio(FIRST_BLOOD_PATH);

        for (String path : spreeAnnouncements.values()) {
            Globals.assetManager.loadAudio(path);
        }
    }

    private void playAnnouncerSound(final String path) {
        AudioNode audio = new AudioNode(Globals.assetManager, path);
        audio.setVolume(1.2f);
        audio.setPositional(false);

        audioQueue.enqueueAudio(audio);
    }
}