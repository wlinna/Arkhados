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

import com.jme3.network.Client;
import com.jme3.network.ClientStateListener;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import magebattle.messages.ChatMessage;
import magebattle.messages.ClientLoginMessage;
import magebattle.messages.MessageUtils;
import magebattle.messages.PlayerDataTableMessage;
import magebattle.messages.ServerLoginMessage;
import magebattle.messages.StartGameMessage;

/**
 *
 * @author william
 */
public class ClientNetListener implements MessageListener, ClientStateListener {

    private ClientMain app;
    private Client client;
    private String name = "";
    private WorldManager worldManager;

    public ClientNetListener(ClientMain app, Client client, WorldManager worldManager) {
        MessageUtils.registerMessages();
        this.app = app;
        this.client = client;
        this.worldManager = worldManager;


    }

    public void messageReceived(Object source, Message m) {
        if (m instanceof ServerLoginMessage) {
            ServerLoginMessage message = (ServerLoginMessage) m;
            if (message.isAccepted()) {
                System.out.println("Hooray! We've... I mean you've been accepted!");

            }

        } else if (m instanceof PlayerDataTableMessage) {
            PlayerDataTableMessage message = (PlayerDataTableMessage) m;
            this.app.refreshPlayerData(message.getNames());
        } else if (m instanceof ChatMessage) {
            ChatMessage message = (ChatMessage) m;
            this.app.addChat(message.getName(), message.getMessage());
        } else if (m instanceof StartGameMessage) {
            this.app.startGame();
        }
    }

    public void clientConnected(Client c) {
        System.out.println("Connected to server");
        ClientLoginMessage message = new ClientLoginMessage(this.name);
        this.client.send(message);
    }

    public void clientDisconnected(Client c, DisconnectInfo info) {
        System.out.println("Disconnected from server");
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
