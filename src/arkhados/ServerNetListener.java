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

import arkhados.messages.BattleStatisticsResponse;
import com.jme3.network.ConnectionListener;
import com.jme3.network.HostedConnection;
import com.jme3.network.Server;
import java.util.logging.Level;
import java.util.logging.Logger;
import arkhados.messages.ChatMessage;
import arkhados.messages.CmdClientLogin;
import arkhados.messages.CmdClientSelectHero;
import arkhados.messages.CmdClientSettings;
import arkhados.messages.CmdPlayerDataTable;
import arkhados.messages.CmdServerLogin;
import arkhados.messages.CmdTopicOnly;
import arkhados.net.Command;
import arkhados.net.CommandHandler;
import arkhados.net.Receiver;
import arkhados.net.ServerSender;
import arkhados.ui.hud.ServerClientDataStrings;
import arkhados.util.PlayerDataStrings;
import arkhados.util.RemovalReasons;
import com.jme3.app.state.AppStateManager;

/**
 *
 * @author william
 */
public class ServerNetListener implements ConnectionListener,
        CommandHandler, ServerClientDataStrings {

    private ServerMain app;
    private AppStateManager stateManager;

    public ServerNetListener(ServerMain app, Server server) {
        this.app = app;
        stateManager = app.getStateManager();
        server.addConnectionListener(this);
    }

    @Override
    public void connectionAdded(Server server, HostedConnection conn) {
        final int clientId = conn.getId();
        if (!ServerClientData.exists(clientId)) {
            ServerClientData.add(clientId);
            ServerSender sender = stateManager.getState(ServerSender.class);
            sender.addConnection(conn);
            stateManager.getState(Receiver.class).addConnection(conn);

            CmdTopicOnly connectionEstablishendCommand =
                    new CmdTopicOnly(Topic.CONNECTION_ESTABLISHED);
            sender.addCommand(connectionEstablishendCommand);
        } else {
            Logger.getLogger(ServerNetListener.class.getName())
                    .log(Level.SEVERE, "Client ID exists!");
            conn.close("ID exists already");
        }
    }

    @Override
    public void connectionRemoved(Server server, HostedConnection conn) {
        int playerId = conn.getAttribute(PLAYER_ID);
        ServerSender sender = stateManager.getState(ServerSender.class);
        sender.removeConnection(conn);
        ServerFogManager fog = stateManager.getState(ServerFogManager.class);
        fog.removeConnection(conn);
        ServerClientData.removeConnection(playerId);
        ServerClientData.remove(conn.getId());
        int entityId =
                PlayerData.getIntData(playerId, PlayerDataStrings.ENTITY_ID);
        if (entityId > -1) {
            WorldManager world = stateManager.getState(WorldManager.class);
            world.removeEntity(entityId, RemovalReasons.DISCONNECT);
        }
        
        PlayerData.remove(playerId);
        CharacterInteraction.removePlayer(playerId);
    }

    @Override
    public void readGuaranteed(Object source, Command command) {
        ServerSender sender = stateManager.getState(ServerSender.class);

        if (command instanceof CmdTopicOnly) {
            handleTopicOnlyCommand((HostedConnection) source,
                    (CmdTopicOnly) command);
        } else if (command instanceof ChatMessage) {
            sender.addCommand(command);
        } else if (command instanceof CmdClientLogin) {
            handleClientLoginCommand((HostedConnection) source,
                    (CmdClientLogin) command);
        } else if (command instanceof CmdClientSelectHero) {
            handleClientSelectHeroCommand((HostedConnection) source,
                    (CmdClientSelectHero) command);
        } else if (command instanceof CmdClientSettings) {
            handleClientSettingsCommand((HostedConnection) source,
                    (CmdClientSettings) command);
        }

    }

    private void handleTopicOnlyCommand(HostedConnection source,
            CmdTopicOnly topicCommand) {
        ServerSender sender = stateManager.getState(ServerSender.class);

        switch (topicCommand.getTopicId()) {
            case Topic.BATTLE_STATISTICS_REQUEST:
                BattleStatisticsResponse response =
                        BattleStatisticsResponse
                        .buildBattleStatisticsResponse();
                sender.addCommand(response);
                break;
            case Topic.UDP_HANDSHAKE_REQUEST:
                sender.addCommandForSingle(
                        new CmdTopicOnly(Topic.UDP_HANDSHAKE_ACK, false),
                        source);
                break;
        }
    }

    private void handleClientLoginCommand(HostedConnection source,
            CmdClientLogin commmand) {
        ServerSender sender = stateManager.getState(ServerSender.class);
        int clientId = source.getId();

        if (!ServerClientData.exists(clientId)) {
            Logger.getLogger(ServerNetListener.class.getName()).log(
                    Level.WARNING,
                    "Receiving join message from unknown client (id: {0})",
                    clientId);
            return;
        }

        final int playerId = PlayerData.getNew(commmand.getName());
        PlayerData.setData(playerId, PlayerDataStrings.HERO, "EmberMage");
        PlayerData.setData(playerId, PlayerDataStrings.TEAM_ID, playerId);

        source.setAttribute(PLAYER_ID, playerId);

        ServerPlayerInputHandler.get().addPlayerInputState(playerId);

        ServerClientData.setConnected(clientId, true);
        ServerClientData.setPlayerId(clientId, playerId);
        ServerClientData.addConnection(playerId, source);

        ServerGameManager gameManager =
                stateManager.getState(ServerGameManager.class);
        gameManager.playerJoined(playerId);

        String modeKey = gameManager.getGameMode().getClass().getSimpleName();
        CmdServerLogin serverLoginMessage =
                new CmdServerLogin(commmand.getName(), playerId,
                true, modeKey);
        sender.addCommandForSingle(serverLoginMessage, source);
        sender.addCommand(CmdPlayerDataTable.makeFromPlayerDataList());
    }

    private void handleClientSelectHeroCommand(HostedConnection source,
            CmdClientSelectHero command) {
        int playerId = ServerClientData.getPlayerId(
                ((HostedConnection) source).getId());

        // TODO: Check hero name validity
        PlayerData.setData(playerId, PlayerDataStrings.HERO,
                command.getHeroName());
    }

    private void handleClientSettingsCommand(HostedConnection source,
            CmdClientSettings clientSettings) {
        int playerId = ServerClientData.getPlayerId(source.getId());
        PlayerData.setData(playerId, PlayerDataStrings.COMMAND_MOVE_INTERRUPTS,
                clientSettings.commandMoveInterrupts());
    }

    @Override
    public void readUnreliable(Object source, Command command) {
        if (command instanceof CmdTopicOnly) {
            handleTopicOnlyCommand((HostedConnection) source,
                    (CmdTopicOnly) command);
        }
    }
}