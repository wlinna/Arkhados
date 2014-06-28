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
import arkhados.messages.ClientLoginCommand;
import arkhados.messages.ClientSelectHeroCommand;
import arkhados.messages.ClientSettingsCommand;
import arkhados.messages.PlayerDataTableCommand;
import arkhados.messages.ServerLoginCommand;
import arkhados.messages.TopicOnlyCommand;
import arkhados.net.Command;
import arkhados.net.CommandHandler;
import arkhados.net.Receiver;
import arkhados.net.Sender;
import arkhados.net.ServerSender;
import arkhados.util.PlayerDataStrings;
import java.util.List;

/**
 *
 * @author william
 */
public class ServerNetListener implements ConnectionListener, CommandHandler {

    private ServerMain app;
    private Server server;

    public ServerNetListener(ServerMain app, Server server) {
        this.app = app;
        this.server = server;
        server.addConnectionListener(this);
    }

    @Override
    public void connectionAdded(Server server, HostedConnection conn) {
        final int clientId = conn.getId();
        if (!ServerClientData.exists(clientId)) {
            ServerClientData.add(clientId);
            ServerSender sender = app.getStateManager().getState(ServerSender.class);
            sender.addConnection(conn);
            app.getStateManager().getState(Receiver.class).addConnection(conn);
            TopicOnlyCommand connectionEstablishendCommand =
                    new TopicOnlyCommand(Topic.CONNECTION_ESTABLISHED);
            sender.addCommand(connectionEstablishendCommand);
        } else {
            Logger.getLogger(ServerNetListener.class.getName()).log(Level.SEVERE, "Client ID exists!");
            conn.close("ID exists already");
        }
    }

    @Override
    public void connectionRemoved(Server server, HostedConnection conn) {
    }

    @Override
    public void readGuaranteed(Object source, List<Command> guaranteed) {
        ServerSender sender = app.getStateManager().getState(ServerSender.class);
        for (Command command : guaranteed) {
            if (command instanceof TopicOnlyCommand) {
                handleTopicOnlyCommand((HostedConnection) source,
                        (TopicOnlyCommand) command);
            } else if (command instanceof ChatMessage) {
                sender.addCommand(command);
            } else if (command instanceof ClientLoginCommand) {
                handleClientLoginCommand((HostedConnection) source,
                        (ClientLoginCommand) command);
            } else if (command instanceof ClientSelectHeroCommand) {
                handleClientSelectHeroCommand((HostedConnection) source,
                        (ClientSelectHeroCommand) command);
            } else if (command instanceof ClientSettingsCommand) {
                handleClientSettingsCommand((HostedConnection) source,
                        (ClientSettingsCommand) command);
            }
        }
    }

    private void handleTopicOnlyCommand(HostedConnection source, TopicOnlyCommand topicCommand) {
        ServerSender sender = app.getStateManager().getState(ServerSender.class);

        switch (topicCommand.getTopicId()) {
            case Topic.BATTLE_STATISTICS_REQUEST:
                final BattleStatisticsResponse response = BattleStatisticsResponse.buildBattleStatisticsResponse();
                sender.addCommand(response);
                break;
            case Topic.START_GAME:
                sender.addCommand(topicCommand);
                app.startGame();
                break;
            case Topic.UDP_HANDSHAKE_REQUEST:
                sender.addCommandForSingle(new TopicOnlyCommand(
                        Topic.UDP_HANDSHAKE_ACK, false), source);
                break;

        }
    }

    private void handleClientLoginCommand(HostedConnection source, ClientLoginCommand commmand) {
        Sender sender = app.getStateManager().getState(Sender.class);
        final int clientId = source.getId();

        if (!ServerClientData.exists(clientId)) {
            Logger.getLogger(ServerNetListener.class.getName()).log(Level.WARNING,
                    "Receiving join message from unknown client (id: {0})", clientId);
            return;
        }

        final int playerId = PlayerData.getNew(commmand.getName());
        PlayerData.setData(playerId, PlayerDataStrings.HERO, "Mage");
        PlayerData.setData(playerId, PlayerDataStrings.TEAM_ID, playerId);
        ServerClientData.setConnected(clientId, true);
        ServerClientData.setPlayerId(clientId, playerId);
        ServerLoginCommand serverLoginMessage = new ServerLoginCommand(commmand.getName(), playerId, true);
        sender.addCommand(serverLoginMessage);
        sender.addCommand(PlayerDataTableCommand.makeFromPlayerDataList());
    }

    private void handleClientSelectHeroCommand(HostedConnection source, ClientSelectHeroCommand command) {
        final int playerId = ServerClientData.getPlayerId(((HostedConnection) source).getId());

        // TODO: Check hero name validity
        PlayerData.setData(playerId, PlayerDataStrings.HERO, command.getHeroName());
    }

    private void handleClientSettingsCommand(HostedConnection source, ClientSettingsCommand clientSettings) {
        int playerId = ServerClientData.getPlayerId(source.getId());
        PlayerData.setData(playerId, PlayerDataStrings.COMMAND_MOVE_INTERRUPTS,
                clientSettings.commandMoveInterrupts());
    }

    @Override
    public void readUnreliable(Object source, List<Command> unreliables) {
        for (Command command : unreliables) {
            if (command instanceof TopicOnlyCommand) {
                handleTopicOnlyCommand((HostedConnection) source,
                        (TopicOnlyCommand) command);
            }
        }
    }
}