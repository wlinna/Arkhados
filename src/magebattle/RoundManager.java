/*    This file is part of JMageBattle.

 JMageBattle is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 JMageBattle is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with JMageBattle.  If not, see <http://www.gnu.org/licenses/>. */
package magebattle;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.scene.Node;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import magebattle.messages.SetPlayersCharacterMessage;
import magebattle.messages.roundprotocol.ClientWorldCreatedMessage;
import magebattle.messages.roundprotocol.CreateWorldMessage;
import magebattle.messages.roundprotocol.NewRoundMessage;
import magebattle.messages.roundprotocol.PlayerReadyForNewRoundMessage;
import magebattle.messages.roundprotocol.RoundFinishedMessage;
import magebattle.util.PlayerDataStrings;
import magebattle.util.UserDataStrings;

/**
 *
 * @author william
 */
public class RoundManager extends AbstractAppState implements MessageListener {

    private WorldManager worldManager;
    private SyncManager syncManager;
    private AppStateManager stateManager;
    private ClientMain clientMain = null;
    private ClientHudManager hudManager = null;
    private int currentRound = 0;
    private int rounds = 3;
    private boolean roundRunning = false;
    private float roundStartCountDown = 0.0f;
    private static final Logger logger = Logger.getLogger(RoundManager.class.getName());

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        logger.log(Level.FINEST, "Initializing RoundManager");
        super.initialize(stateManager, app);
        this.worldManager = stateManager.getState(WorldManager.class);
        this.syncManager = stateManager.getState(SyncManager.class);
        this.stateManager = stateManager;
        this.syncManager.addObject(-1, this.worldManager);

        if (this.worldManager.isClient()) {
            this.syncManager.getClient().addMessageListener(this, CreateWorldMessage.class, NewRoundMessage.class, RoundFinishedMessage.class);
            this.clientMain = (ClientMain) app;

        } else if (this.worldManager.isServer()) {
            this.syncManager.getServer().addMessageListener(this, ClientWorldCreatedMessage.class, PlayerReadyForNewRoundMessage.class);
        }
        logger.log(Level.FINEST, "Initialized RoundManager");
    }

    public void serverStartGame() {
        logger.log(Level.FINEST, "serverStartGame");
        PlayerData.setDataForAll(PlayerDataStrings.WORLD_CREATED, false);
        PlayerData.setDataForAll(PlayerDataStrings.READY_FOR_ROUND, false);

        if (currentRound == 0) {
            this.createWorld();
        }
    }

    private void createWorld() {
        logger.log(Level.FINEST, "Creating world");
        ++this.currentRound;
        if (this.worldManager.isClient()) {
            this.clientMain.enqueue(new Callable<Void>() {
                public Void call() throws Exception {

                    if (currentRound > 1) {
                        cleanupPreviousRound();
                    }

                    worldManager.setEnabled(true);
                    worldManager.loadLevel();
                    worldManager.attachLevel();

                    syncManager.getClient().send(new ClientWorldCreatedMessage());

                    return null;
                }
            });
        }

        if (this.worldManager.isServer()) {
            if (this.currentRound > 1) {
                this.cleanupPreviousRound();
            }

            logger.log(Level.FINEST, "Enablind worldManager");
            worldManager.setEnabled(true);
            logger.log(Level.FINEST, "Loading level");
            worldManager.loadLevel();
            logger.log(Level.FINEST, "Attaching level");
            worldManager.attachLevel();

            logger.log(Level.FINEST, "Broadcasting CreateWorldMessage");
            this.syncManager.getServer().broadcast(new CreateWorldMessage());
            logger.log(Level.FINEST, "Enablind syncManager");
            this.syncManager.setEnabled(true);
        }

    }

    private void createCharacters() {
        logger.log(Level.FINEST, "Creating characters");
        if (this.worldManager.isServer()) {
            int i = 0;
            for (Iterator<PlayerData> it = PlayerData.getPlayers().iterator(); it.hasNext();) {
                PlayerData playerData = it.next();
                Vector3f startingLocation = new Vector3f(WorldManager.STARTING_LOCATIONS[i++]);
                startingLocation.setY(7.0f);
                long entityId = this.worldManager.addNewEntity("Mage", startingLocation, new Quaternion());
                playerData.setData(PlayerDataStrings.ENTITY_ID, entityId);
            }

            logger.log(Level.FINEST, "Created characters");

            for (PlayerData playerData : PlayerData.getPlayers()) {
                long entityId = playerData.getLongData(PlayerDataStrings.ENTITY_ID);
                this.syncManager.getServer().broadcast(new SetPlayersCharacterMessage(entityId, playerData.getId()));
            }

            logger.log(Level.FINEST, "Informing players of their characters");
        }

        // TODO: Wait for confirmation from clients
        this.startNewRound();
    }

    private void startNewRound() {
        logger.log(Level.FINEST, "Starting new round");
        if (this.worldManager.isServer()) {
            this.syncManager.getServer().broadcast(new NewRoundMessage());
        }
        this.roundRunning = true;
        if (this.worldManager.isClient()) {
            this.clientMain.getUserCommandManager().setEnabled(true);
        }
    }

    private void cleanupPreviousRound() {
        logger.log(Level.FINEST, "Cleaning up previous round");
        worldManager.clear();
        this.syncManager.addObject(-1, this.worldManager);
        if (this.worldManager.isClient()) {
            this.stateManager.getState(ClientHudManager.class).clear();
        }
    }

    private void endRound() {
        logger.log(Level.FINEST, "Ending round");
        if (this.worldManager.isServer()) {
            this.syncManager.getServer().broadcast(new RoundFinishedMessage());
            PlayerData.setDataForAll(PlayerDataStrings.WORLD_CREATED, false);
            PlayerData.setDataForAll(PlayerDataStrings.READY_FOR_ROUND, false);
            logger.log(Level.FINEST, "Disabling syncManager");

            this.syncManager.setEnabled(false);
        }
        this.roundRunning = false;
        logger.log(Level.FINEST, "Enabling worldManager");

        this.worldManager.setEnabled(false);

        if (this.worldManager.isClient()) {
            this.clientMain.getUserCommandManager().setEnabled(false);

        }

        // TODO: Add wait time so players can watch their stats and get ready

        if (this.worldManager.isServer() && this.currentRound < this.rounds) {
            this.createWorld();
        }
    }

    @Override
    public void update(float tpf) {

        if (this.worldManager.isServer()) {
            if (this.roundStartCountDown > 0f) {
                this.roundStartCountDown -= tpf;
                if (this.roundStartCountDown <= 0f) {
                    this.startNewRound();
                }
            }
        }

        if (!this.roundRunning) {
            return;
        }

        if (this.worldManager.isServer()) {
            int aliveAmount = 0;
            for (PlayerData playerData : PlayerData.getPlayers()) {
                long entityId = playerData.getLongData(PlayerDataStrings.ENTITY_ID);
                Node character = (Node) this.worldManager.getEntity(entityId);
                if ((Float) character.getUserData(UserDataStrings.HEALTH_CURRENT) > 0f) {
                    ++aliveAmount;
                    if (aliveAmount > 1) {
                        break;
                    }
                }
            }
            if (aliveAmount == 0) {
                this.endRound();
            }
        }
    }

    private boolean allClientsWorldReady() {
        for (PlayerData playerData : PlayerData.getPlayers()) {
            if (!playerData.getBooleanData(PlayerDataStrings.WORLD_CREATED)) {
                System.out.println("Not all players are ready yet");
                return false;
            }
        }
        logger.log(Level.INFO, "All players have created world");

        return true;
    }

    private boolean allReadyForRound() {
        for (PlayerData playerData : PlayerData.getPlayers()) {
            if (!playerData.getBooleanData(PlayerDataStrings.READY_FOR_ROUND)) {
                System.out.println("Not all players are ready yet");
                return false;
            }
        }
        System.out.println("All players are ready!");
        return true;
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }

    public void messageReceived(Object source, Message m) {
        if (this.worldManager.isClient()) {
            this.clientMessageReceived(source, m);
        } else if (this.worldManager.isServer()) {
            this.serverMessageReceived((HostedConnection) source, m);
        }
    }

    private void clientMessageReceived(Object source, Message m) {
        if (m instanceof CreateWorldMessage) {
            this.createWorld();
        } else if (m instanceof NewRoundMessage) {
            this.startNewRound();
        } else if (m instanceof RoundFinishedMessage) {
            this.endRound();
        }
    }

    private void serverMessageReceived(HostedConnection client, Message m) {
        logger.log(Level.FINEST, "Received {0} -message", new Object[]{m.getClass()});

        if (m instanceof ClientWorldCreatedMessage) {
            long playerId = ServerClientData.getPlayerId(client.getId());
            PlayerData.setData(playerId, PlayerDataStrings.WORLD_CREATED, true);
            if (this.allClientsWorldReady()) {
                this.createCharacters();
            }
//        } else if (m instanceof PlayerReadyForNewRoundMessage) {
//            long playerdId = ServerClientData.getPlayerId(client.getId());
//            PlayerData.setData(playerdId, PlayerDataStrings.ENTITY_ID, true);
//            if (this.allReadyForRound()) {
//                // TODO: Countdown like 3...2...1...Fight!
//                this.startNewRound();
//            }
        }
    }
}