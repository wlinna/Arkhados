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

import arkhados.messages.BattleStatisticsRequest;
import arkhados.messages.BattleStatisticsResponse;
import com.jme3.network.ConnectionListener;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Server;
import java.util.logging.Level;
import java.util.logging.Logger;
import arkhados.messages.ChatMessage;
import arkhados.messages.ClientLoginMessage;
import arkhados.messages.ClientSelectHeroMessage;
import arkhados.messages.ClientSettingsMessage;
import arkhados.messages.ConnectionEstablishedMessage;
import arkhados.messages.MessageUtils;
import arkhados.messages.PlayerDataTableMessage;
import arkhados.messages.ServerLoginMessage;
import arkhados.messages.StartGameMessage;
import arkhados.messages.UDPHandshakeAck;
import arkhados.messages.UDPHandshakeRequest;
import arkhados.util.PlayerDataStrings;

/**
 *
 * @author william
 */
public class ServerNetListener implements MessageListener<HostedConnection>, ConnectionListener {

    private ServerMain app;
    private Server server;

    public ServerNetListener(ServerMain app, Server server) {

        this.app = app;
        this.server = server;
        this.server.addConnectionListener(this);
        this.server.addMessageListener(this,
                UDPHandshakeRequest.class,
                ClientLoginMessage.class, ChatMessage.class,
                StartGameMessage.class, ClientSettingsMessage.class,
                ClientSelectHeroMessage.class,
                BattleStatisticsRequest.class);
    }

    @Override
    public void connectionAdded(Server server, HostedConnection conn) {
        final int clientId = conn.getId();
        if (!ServerClientData.exists(clientId)) {
            ServerClientData.add(clientId);
            final ConnectionEstablishedMessage connectionMessage = new ConnectionEstablishedMessage();
            conn.send(connectionMessage);
        } else {
            Logger.getLogger(ServerNetListener.class.getName()).log(Level.SEVERE, "Client ID exists!");
            conn.close("ID exists already");
        }
    }

    @Override
    public void connectionRemoved(Server server, HostedConnection conn) {
    }

    @Override
    public void messageReceived(HostedConnection source, Message m) {
        if (m instanceof UDPHandshakeRequest) {
            source.send(new UDPHandshakeAck());
            
        } else if (m instanceof ClientLoginMessage) {
            final ClientLoginMessage message = (ClientLoginMessage) m;
            final int clientId = source.getId();

            if (!ServerClientData.exists(clientId)) {
                Logger.getLogger(ServerNetListener.class.getName()).log(Level.WARNING, "Receiving join message from unknown client (id: {0})", clientId);
                return;
            }
            final long playerId = PlayerData.getNew(message.getName());
            PlayerData.setData(playerId, PlayerDataStrings.HERO, "Mage");
            PlayerData.setData(playerId, PlayerDataStrings.TEAM_ID, playerId);
            ServerClientData.setConnected(clientId, true);
            ServerClientData.setPlayerId(clientId, playerId);
            ServerLoginMessage serverLoginMessage = new ServerLoginMessage(message.getName(), playerId, true);
            source.send(serverLoginMessage);
            this.server.broadcast(PlayerDataTableMessage.makeFromPlayerDataList());

        } else if (m instanceof ClientSettingsMessage) {
            ClientSettingsMessage clientSettings = (ClientSettingsMessage) m;
            long playerId = ServerClientData.getPlayerId(source.getId());            
            PlayerData.setData(playerId, PlayerDataStrings.COMMAND_MOVE_INTERRUPTS, clientSettings.commandMoveInterrupts());
            
        } else if (m instanceof ChatMessage) {
            final ChatMessage message = (ChatMessage) m;
            this.server.broadcast(message);
            
        } else if (m instanceof ClientSelectHeroMessage) {
            final ClientSelectHeroMessage message = (ClientSelectHeroMessage) m;
            final long playerId = ServerClientData.getPlayerId(source.getId());

            // TODO: Check hero name validity
            PlayerData.setData(playerId, PlayerDataStrings.HERO, message.getHeroName());

        } else if (m instanceof StartGameMessage) {
            final StartGameMessage message = (StartGameMessage) m;
            this.server.broadcast(message);
            this.app.startGame();
            
        } else if (m instanceof BattleStatisticsRequest) {
            final BattleStatisticsResponse response = BattleStatisticsResponse.buildBattleStatisticsResponse();
            source.send(response);
        }
    }
}