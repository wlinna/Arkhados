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
package arkhados;

import arkhados.ui.hud.ClientHudManager;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.HostedConnection;
import com.jme3.scene.Node;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import arkhados.messages.SetPlayersCharacterCommand;
import arkhados.messages.TopicOnlyCommand;
import arkhados.messages.roundprotocol.RoundStartCountdownCommand;
import arkhados.net.ClientSender;
import arkhados.net.Command;
import arkhados.net.CommandHandler;
import arkhados.net.Sender;
import arkhados.net.ServerSender;
import arkhados.util.NodeBuilderIdHeroNameMatcherSingleton;
import arkhados.util.PlayerDataStrings;
import arkhados.util.Timer;
import arkhados.util.UserDataStrings;
import java.util.List;

/**
 *
 * @author william TODO: I think that current Round-protocol is very confusing
 * and hard to understand. It might need rework
 */
public class RoundManager extends AbstractAppState implements CommandHandler {

    private static final Logger logger = Logger.getLogger(RoundManager.class.getName());
    private WorldManager worldManager;
    private SyncManager syncManager;
    private AppStateManager stateManager;
    private Application app;
    private ClientMain clientMain = null;
    private int currentRound = 0;
    private int rounds = 3;
    private boolean roundRunning = false;
    private Timer roundStartTimer = new Timer(5);
    private Timer roundEndTimer = new Timer(5);

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        logger.setLevel(Level.INFO);
        super.initialize(stateManager, app);
        worldManager = stateManager.getState(WorldManager.class);
        syncManager = stateManager.getState(SyncManager.class);
        this.stateManager = stateManager;
        syncManager.addObject(-1, worldManager);
        this.app = app;

        if (worldManager.isClient()) {
            clientMain = (ClientMain) app;
        }
    }

    public void serverStartGame() {
        logger.log(Level.INFO, "");
        PlayerData.setDataForAll(PlayerDataStrings.WORLD_CREATED, false);
        PlayerData.setDataForAll(PlayerDataStrings.READY_FOR_ROUND, false);

        if (currentRound == 0) {
            createWorld();
        }
    }

    private void createWorld() {
        logger.log(Level.INFO, "Creating world");
        ++currentRound;
        app.enqueue(new Callable<Void>() {
            public Void call() throws Exception {
                if (currentRound > 1) {
                    cleanupPreviousRound();
                }

                worldManager.setEnabled(true);
                worldManager.loadLevel();
                worldManager.attachLevel();

                Sender sender = app.getStateManager().getState(Sender.class);
                if (worldManager.isClient()) {
                    sender.addCommand(new TopicOnlyCommand(Topic.CLIENT_WORLD_CREATED));
                }

                if (sender.isServer()) {
                    logger.log(Level.INFO, "Broadcasting CreateWorldMessage");
                    sender.addCommand(new TopicOnlyCommand(Topic.CREATE_WORLD));
                    syncManager.setEnabled(true);
                    syncManager.startListening();
                }
                return null;
            }
        });
    }

    private void createCharacters() {
        final ServerSender sender = app.getStateManager().getState(ServerSender.class);
        logger.log(Level.INFO, "Creating characters");
        if (sender.isServer()) {
            app.enqueue(new Callable<Void>() {
                @Override
                public Void call() throws Exception {


                    int i = 0;
                    for (PlayerData playerData : PlayerData.getPlayers()) {
                        Vector3f startingLocation = new Vector3f(WorldManager.STARTING_LOCATIONS[i++]);
                        startingLocation.setY(7.0f);
                        final String heroName = playerData.getStringData(PlayerDataStrings.HERO);
                        final int nodeBuilderId =
                                NodeBuilderIdHeroNameMatcherSingleton.get().getId(heroName);
                        final int entityId = worldManager.addNewEntity(nodeBuilderId,
                                startingLocation, new Quaternion(), playerData.getId());
                        playerData.setData(PlayerDataStrings.ENTITY_ID, entityId);
                    }

                    logger.log(Level.INFO, "Created characters");

                    for (PlayerData playerData : PlayerData.getPlayers()) {
                        int entityId = playerData.getIntData(PlayerDataStrings.ENTITY_ID);

                        sender.addCommand(new SetPlayersCharacterCommand(entityId, playerData.getId()));
                    }

                    logger.log(Level.INFO, "Informing players of their characters");
                    return null;
                }
            });
        }

        sender.addCommand(new RoundStartCountdownCommand(5));
        roundStartTimer.setTimeLeft(5f);
        roundStartTimer.setActive(true);
    }

    private void startNewRound() {
        Sender sender = app.getStateManager().getState(Sender.class);
        logger.log(Level.INFO, "Starting new round");
        if (sender.isServer()) {
            sender.addCommand(new TopicOnlyCommand(Topic.NEW_ROUND));
            CharacterInteraction.startNewRound();
        }
        roundRunning = true;
        if (sender.isClient()) {
            clientMain.getUserCommandManager().setEnabled(true);
            stateManager.getState(ClientHudManager.class).startRound();
        }
    }

    private void cleanupPreviousRound() {
        logger.log(Level.INFO, "Cleaning up previous round");
        worldManager.clear();
        syncManager.addObject(-1, worldManager);
        if (worldManager.isClient()) {
            stateManager.getState(ClientHudManager.class).clear();
            clientMain.getUserCommandManager().nullifyCharacter();
        }
    }

    private void endRound() {
        Sender sender = app.getStateManager().getState(Sender.class);
        logger.log(Level.INFO, "Ending round");
        if (sender.isServer()) {
            sender.addCommand(new TopicOnlyCommand(Topic.ROUND_FINISHED));
            PlayerData.setDataForAll(PlayerDataStrings.WORLD_CREATED, false);
            PlayerData.setDataForAll(PlayerDataStrings.READY_FOR_ROUND, false);
            logger.log(Level.INFO, "Disabling syncManager");

            syncManager.stopListening();
        }
        roundRunning = false;
        logger.log(Level.INFO, "Enabling worldManager");

        worldManager.setEnabled(false);

        if (worldManager.isClient()) {
            clientMain.getUserCommandManager().setEnabled(false);
            stateManager.getState(ClientHudManager.class).showRoundStatistics();
        }

        roundEndTimer.setTimeLeft(5f);
        roundEndTimer.setActive(true);
    }

    @Override
    public void update(float tpf) {
        Sender sender = app.getStateManager().getState(Sender.class);
        roundStartTimer.update(tpf);
        if (roundStartTimer.timeJustEnded()) {
            if (sender.isServer()) {
                startNewRound();
            }
            roundStartTimer.setActive(false);
        }

        if (roundStartTimer.isActive() && worldManager.isClient()) {
            stateManager.getState(ClientHudManager.class)
                    .setSecondsLeftToStart((int) roundStartTimer.getTimeLeft());
        }

        roundEndTimer.update(tpf);
        if (roundEndTimer.timeJustEnded() && sender.isServer()) {
            if (currentRound < rounds) {
                createWorld();
            } else {
                sender.addCommand(new TopicOnlyCommand(Topic.GAME_ENDED));
            }
        }

        if (!roundRunning) {
            return;
        }

        if (sender.isServer()) {
            int aliveAmount = 0;
            for (PlayerData playerData : PlayerData.getPlayers()) {
                int entityId = playerData.getIntData(PlayerDataStrings.ENTITY_ID);
                Node character = (Node) worldManager.getEntity(entityId);
                if (character != null) {
                    Float healthCurrent = character.getUserData(UserDataStrings.HEALTH_CURRENT);
                    if (healthCurrent != null && healthCurrent > 0f) {
                        ++aliveAmount;
                        if (aliveAmount > 1) {
                            break;
                        }
                    } else if (healthCurrent == null) {
                        logger.log(Level.WARNING, "Current health of id {0} is null", entityId);
                    }
                } else {
                    logger.log(Level.WARNING, "Character of id {0} is null!", entityId);
                }
            }
            if (aliveAmount == 0) {
                endRound();
            }
        }
    }

    private boolean allClientsWorldReady() {
        for (PlayerData playerData : PlayerData.getPlayers()) {
            if (!playerData.getBooleanData(PlayerDataStrings.WORLD_CREATED)) {
                return false;
            }
        }
        logger.log(Level.INFO, "All players have created world");

        return true;
    }

    @Override
    public void readGuaranteed(Object source, List<Command> guaranteed) {
        Sender sender = app.getStateManager().getState(Sender.class);
        if (sender.isServer()) {
            serverReadGuaranteed((HostedConnection) source, guaranteed);
        } else {
            clientReadGuaranteed(guaranteed);
        }

    }

    private void serverReadGuaranteed(HostedConnection source, List<Command> guaranteed) {
        for (Command command : guaranteed) {
            if (command instanceof TopicOnlyCommand) {
                serverHandleTopicOnly(source, (TopicOnlyCommand) command);
            }
        }
    }

    private void clientReadGuaranteed(List<Command> guaranteed) {
        for (Command command : guaranteed) {
            if (command instanceof TopicOnlyCommand) {
                clientHandleTopicOnly((TopicOnlyCommand) command);
            } else if (command instanceof RoundStartCountdownCommand) {
                RoundStartCountdownCommand countDownCommand = (RoundStartCountdownCommand) command;
                roundStartTimer.setTimeLeft(countDownCommand.getTime());
                roundStartTimer.setActive(true);
            }
        }
    }

    @Override
    public void readUnreliable(Object source, List<Command> unreliables) {
    }

    private void clientHandleTopicOnly(TopicOnlyCommand command) {
        switch (command.getTopicId()) {
            case Topic.CREATE_WORLD:
                createWorld();
                stateManager.getState(ClientHudManager.class).hideRoundStatistics();
                break;
            case Topic.NEW_ROUND:
                startNewRound();
                break;
            case Topic.ROUND_FINISHED:
                endRound();
                break;
            case Topic.GAME_ENDED:
                clientHandleGameEnded();
                break;
        }
    }

    private void clientHandleGameEnded() {
        app.enqueue(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ClientSender sender = app.getStateManager().getState(ClientSender.class);
                sender.getClient().close();
                worldManager.clear();
                syncManager.clear();

                PlayerData.destroyAllData();
                clientMain.getUserCommandManager().nullifyCharacter();
                stateManager.getState(ClientHudManager.class).endGame();
                return null;
            }
        });
    }

    private void serverHandleTopicOnly(HostedConnection source, TopicOnlyCommand topicOnlyCommand) {
        switch (topicOnlyCommand.getTopicId()) {
            case Topic.CLIENT_WORLD_CREATED:
                serverHandleClientWorldCreated(source);
                break;
        }
    }

    private void serverHandleClientWorldCreated(HostedConnection source) {
        int playerId = ServerClientData.getPlayerId(source.getId());
        PlayerData.setData(playerId, PlayerDataStrings.WORLD_CREATED, true);
        if (allClientsWorldReady()) {
            createCharacters();
        }
    }
}