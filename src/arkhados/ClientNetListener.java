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

import com.jme3.network.Client;
import com.jme3.network.ClientStateListener;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.scene.Spatial;
import arkhados.messages.ChatMessage;
import arkhados.messages.ClientLoginMessage;
import arkhados.messages.MessageUtils;
import arkhados.messages.roundprotocol.NewRoundMessage;
import arkhados.messages.PlayerDataTableMessage;
import arkhados.messages.roundprotocol.RoundFinishedMessage;
import arkhados.messages.ServerLoginMessage;
import arkhados.messages.SetPlayersCharacterMessage;
import arkhados.messages.StartGameMessage;
import arkhados.messages.roundprotocol.PlayerReadyForNewRoundMessage;

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
                this.app.getUserCommandManager().setPlayerId(message.getPlayerId());
                System.out.println(String.format("Your playerId: %d", message.getPlayerId()));
            }

        } else if (m instanceof PlayerDataTableMessage) {
            PlayerDataTableMessage message = (PlayerDataTableMessage) m;
            this.app.refreshPlayerData(message.getNames());
        } else if (m instanceof ChatMessage) {
            ChatMessage message = (ChatMessage) m;
            this.app.addChat(message.getName(), message.getMessage());
        } else if (m instanceof StartGameMessage) {
            this.app.startGame();
        } else if (m instanceof SetPlayersCharacterMessage) {
            SetPlayersCharacterMessage message = (SetPlayersCharacterMessage)m;
            if (this.app.getUserCommandManager().getPlayerId() == message.getPlayerId()) {
//                Spatial character = this.worldManager.getEntity(message.getEntityId());
                this.app.getUserCommandManager().setCharacterId(message.getEntityId());
                System.out.println(String.format("Your entityId: %d", message.getEntityId()));
            }
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