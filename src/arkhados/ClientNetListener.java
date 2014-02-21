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
import com.jme3.network.Client;
import com.jme3.network.ClientStateListener;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import arkhados.messages.ChatMessage;
import arkhados.messages.ClientLoginMessage;
import arkhados.messages.ConnectionEstablishedMessage;
import arkhados.messages.PlayerDataTableMessage;
import arkhados.messages.ServerLoginMessage;
import arkhados.messages.SetPlayersCharacterMessage;
import arkhados.messages.StartGameMessage;
import arkhados.ui.hud.ClientHudManager;
import arkhados.util.ValueWrapper;
import com.jme3.network.NetworkClient;

/**
 *
 * @author william
 */
public class ClientNetListener implements MessageListener, ClientStateListener {

    private ClientMain app;
    private ValueWrapper<NetworkClient> client;
    private String name = "";    

    public ClientNetListener(ClientMain app, ValueWrapper<NetworkClient> client) {
        this.app = app;
        this.client = client;
    }

    @Override
    public void messageReceived(Object source, Message m) {
        if (m instanceof ConnectionEstablishedMessage) {
            ClientLoginMessage message = new ClientLoginMessage(this.name);
            this.client.get().send(message);
        } else if (m instanceof ServerLoginMessage) {
            ServerLoginMessage message = (ServerLoginMessage) m;
            if (message.isAccepted()) {
                this.app.getUserCommandManager().setPlayerId(message.getPlayerId());
            }
        } else if (m instanceof PlayerDataTableMessage) {
            PlayerDataTableMessage message = (PlayerDataTableMessage) m;
            this.app.refreshPlayerData(message.getPlayerData());
        } else if (m instanceof ChatMessage) {
            ChatMessage message = (ChatMessage) m;
            this.app.addChat(message.getName(), message.getMessage());
        } else if (m instanceof StartGameMessage) {
            this.app.startGame();
        } else if (m instanceof SetPlayersCharacterMessage) {
            SetPlayersCharacterMessage message = (SetPlayersCharacterMessage)m;
            if (this.app.getUserCommandManager().getPlayerId() == message.getPlayerId()) {
                this.app.getUserCommandManager().setCharacterId(message.getEntityId());
                System.out.println(String.format("Your entityId: %d", message.getEntityId()));
            }
        } else if (m instanceof BattleStatisticsResponse) {
            final BattleStatisticsResponse message = (BattleStatisticsResponse) m;
            this.app.getStateManager().getState(ClientHudManager.class).updateStatistics(message.getPlayerRoundStatsList());
        }
    }

    @Override
    public void clientConnected(Client c) {
    }

    @Override
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