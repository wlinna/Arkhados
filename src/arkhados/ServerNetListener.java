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
import arkhados.messages.MessageUtils;
import arkhados.messages.PlayerDataTableMessage;
import arkhados.messages.ServerLoginMessage;
import arkhados.messages.StartGameMessage;
import arkhados.util.PlayerDataStrings;

/**
 *
 * @author william
 */
public class ServerNetListener implements MessageListener<HostedConnection>, ConnectionListener {

    private ServerMain app;
    private Server server;
    //
//    private WorldManager worldManager;

    public ServerNetListener(ServerMain app, Server server) {
        MessageUtils.registerMessages();
        this.app = app;
        this.server = server;
        this.server.addConnectionListener(this);
        this.server.addMessageListener(this,
                ClientLoginMessage.class, ChatMessage.class,
                StartGameMessage.class,
                ClientSelectHeroMessage.class);
    }

    public void connectionAdded(Server server, HostedConnection conn) {
        final int clientId = conn.getId();
        System.out.println("New connection");
        if (!ServerClientData.exists(clientId)) {
            System.out.println("Adding connection");
            ServerClientData.add(clientId);
        } else {
            Logger.getLogger(ServerNetListener.class.getName()).log(Level.SEVERE, "Client ID exists!");
            conn.close("ID exists already");
        }
    }

    public void connectionRemoved(Server server, HostedConnection conn) {
    }

    public void messageReceived(HostedConnection source, Message m) {
        if (m instanceof ClientLoginMessage) {
            final ClientLoginMessage message = (ClientLoginMessage) m;
            final int clientId = source.getId();

            if (!ServerClientData.exists(clientId)) {
                Logger.getLogger(ServerNetListener.class.getName()).log(Level.WARNING, "Receiving join message from unknown client");
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
        }
    }
}
