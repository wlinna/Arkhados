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
import arkhados.messages.ChatMessage;
import arkhados.messages.ClientLoginCommand;
import arkhados.messages.ClientSettingsCommand;
import arkhados.messages.PlayerDataTableMessage;
import arkhados.messages.ServerLoginCommand;
import arkhados.messages.SetPlayersCharacterMessage;
import arkhados.messages.TopicOnlyCommand;
import arkhados.net.Command;
import arkhados.net.CommandHandler;
import arkhados.net.Sender;
import arkhados.ui.hud.ClientHudManager;
import arkhados.util.PlayerDataStrings;
import arkhados.util.Timer;
import arkhados.util.ValueWrapper;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.network.NetworkClient;
import com.jme3.system.AppSettings;
import java.util.List;

/**
 *
 * @author william
 */
public class ClientNetListener extends AbstractAppState implements ClientStateListener, CommandHandler {

    private ClientMain app;
    private ValueWrapper<NetworkClient> client;
    private String name = "";
    private Timer udpHandshakeAckTimer = new Timer(1f);
    private boolean handshakeComplete = false;

    public ClientNetListener(ValueWrapper<NetworkClient> client) {
        this.client = client;
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.app = (ClientMain) app;
    }

    public void reset() {
        udpHandshakeAckTimer.setTimeLeft(1f);
        udpHandshakeAckTimer.setActive(false);
        handshakeComplete = false;
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
        udpHandshakeAckTimer.update(tpf);
        if (udpHandshakeAckTimer.timeJustEnded()) {
            Sender sender = app.getStateManager().getState(Sender.class);
            sender.addCommand(new TopicOnlyCommand(TopicOnlyCommand.UDP_HANDSHAKE_REQUEST, false));
            udpHandshakeAckTimer.setTimeLeft(udpHandshakeAckTimer.getOriginal());
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
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void readGuaranteed(Object source, List<Command> guaranteed) {
        for (Command command : guaranteed) {
            if (command instanceof TopicOnlyCommand) {
                handleTopicCommand((TopicOnlyCommand) command);
            } else if (command instanceof PlayerDataTableMessage) {
                PlayerDataTableMessage dataTable = (PlayerDataTableMessage) command;
                app.refreshPlayerData(dataTable.getPlayerData());
            } else if (command instanceof ChatMessage) {
                ChatMessage chat = (ChatMessage) command;
                app.addChat(chat.getName(), chat.getMessage());
            } else if (command instanceof ServerLoginCommand) {
                handleLoginCommand((ServerLoginCommand) command);
            } else if (command instanceof BattleStatisticsResponse) {
                BattleStatisticsResponse response = (BattleStatisticsResponse) command;
                app.getStateManager().getState(ClientHudManager.class).updateStatistics(response.getPlayerRoundStatsList());
            } else if (command instanceof SetPlayersCharacterMessage) {
                handleSetPlayersCharacter((SetPlayersCharacterMessage) command);
            }
        }
    }

    @Override
    public void readUnreliable(Object source, List<Command> unreliables) {
        for (Command command : unreliables) {
            if (command instanceof TopicOnlyCommand) {
                handleTopicCommand((TopicOnlyCommand) command);
            }
        }
    }

    private void handleTopicCommand(TopicOnlyCommand topicOnlyCommand) {
        switch (topicOnlyCommand.getTopicId()) {
            case TopicOnlyCommand.START_GAME:
                app.startGame();
                udpHandshakeAckTimer.setActive(false);
                break;
            case TopicOnlyCommand.UDP_HANDSHAKE_ACK:
                handleUdpHandshakeAck();
                break;
            case TopicOnlyCommand.CONNECTION_ESTABLISHED:
                Sender sender = app.getStateManager().getState(Sender.class);
                sender.addCommand(new TopicOnlyCommand(
                        TopicOnlyCommand.UDP_HANDSHAKE_REQUEST, false));
                udpHandshakeAckTimer.setActive(true);
        }
    }

    private void handleLoginCommand(ServerLoginCommand loginCommand) {
        if (loginCommand.isAccepted()) {
            app.getUserCommandManager().setPlayerId(loginCommand.getPlayerId());
            AppSettings settings = app.getContext().getSettings();

            boolean movingInterrupts = settings.getBoolean(PlayerDataStrings.COMMAND_MOVE_INTERRUPTS);

            ClientSettingsCommand clientSettingsCommand = new ClientSettingsCommand(movingInterrupts);
            Sender sender = app.getStateManager().getState(Sender.class);
            sender.addCommand(clientSettingsCommand);
        }
    }

    private void handleSetPlayersCharacter(SetPlayersCharacterMessage message) {
        if (app.getUserCommandManager().getPlayerId() == message.getPlayerId()) {
            app.getUserCommandManager().setCharacterId(message.getEntityId());
            System.out.println(String.format("Your entityId: %d", message.getEntityId()));
        }
    }

    private void handleUdpHandshakeAck() {
        Sender sender = app.getStateManager().getState(Sender.class);
        if (!handshakeComplete) {
            ClientLoginCommand command = new ClientLoginCommand(name);
            sender.addCommand(command);
            handshakeComplete = true;
            app.toLobby();
            app.setStatusText("");
        }
    }
}