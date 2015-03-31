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
import arkhados.messages.CmdClientLogin;
import arkhados.messages.CmdClientSettings;
import arkhados.messages.CmdPlayerDataTable;
import arkhados.messages.CmdServerLogin;
import arkhados.messages.CmdSetPlayersCharacter;
import arkhados.messages.CmdTopicOnly;
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

/**
 *
 * @author william
 */
public class ClientNetListener extends AbstractAppState
        implements ClientStateListener, CommandHandler {

    private ClientMain app;
    private String name = "";
    private Timer udpHandshakeAckTimer = new Timer(1f);
    private boolean handshakeComplete = false;

    public ClientNetListener(ValueWrapper<NetworkClient> client) {
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
            sender.addCommand(
                    new CmdTopicOnly(Topic.UDP_HANDSHAKE_REQUEST, false));
            udpHandshakeAckTimer
                    .setTimeLeft(udpHandshakeAckTimer.getOriginal());
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
    public void readGuaranteed(Object source, Command command) {
        if (command instanceof CmdTopicOnly) {
            handleTopicCommand((CmdTopicOnly) command);
        } else if (command instanceof CmdPlayerDataTable) {
            CmdPlayerDataTable dataTable = (CmdPlayerDataTable) command;
            PlayerData.setPlayers(dataTable.getPlayerData());
        } else if (command instanceof CmdServerLogin) {
            handleLoginCommand((CmdServerLogin) command);
        } else if (command instanceof BattleStatisticsResponse) {
            BattleStatisticsResponse response =
                    (BattleStatisticsResponse) command;
            app.getStateManager().getState(ClientHudManager.class)
                    .setLatestStatsList(response.getPlayerRoundStatsList());
        } else if (command instanceof CmdSetPlayersCharacter) {
            handleSetPlayersCharacter((CmdSetPlayersCharacter) command);
        }
    }

    @Override
    public void readUnreliable(Object source, Command command) {
        if (command instanceof CmdTopicOnly) {
            handleTopicCommand((CmdTopicOnly) command);
        }
    }

    private void handleTopicCommand(CmdTopicOnly topicOnlyCommand) {
        switch (topicOnlyCommand.getTopicId()) {
            case Topic.START_GAME:
                app.startGame();
                udpHandshakeAckTimer.setActive(false);
                break;
            case Topic.UDP_HANDSHAKE_ACK:
                handleUdpHandshakeAck();
                break;
            case Topic.CONNECTION_ESTABLISHED:
                Sender sender = app.getStateManager().getState(Sender.class);
                sender.addCommand(new CmdTopicOnly(
                        Topic.UDP_HANDSHAKE_REQUEST, false));
                udpHandshakeAckTimer.setActive(true);
        }
    }

    private void handleLoginCommand(CmdServerLogin loginCommand) {
        if (loginCommand.isAccepted()) {
            app.getStateManager().getState(UserCommandManager.class).
                    setPlayerId(loginCommand.getPlayerId());
            AppSettings settings = app.getContext().getSettings();

            boolean movingInterrupts = settings
                    .getBoolean(PlayerDataStrings.COMMAND_MOVE_INTERRUPTS);

            CmdClientSettings clientSettingsCommand =
                    new CmdClientSettings(movingInterrupts);
            Sender sender = app.getStateManager().getState(Sender.class);
            sender.addCommand(clientSettingsCommand);

            app.setupGameMode(loginCommand.getGameMode());
        }
    }

    private void handleSetPlayersCharacter(CmdSetPlayersCharacter message) {
        UserCommandManager userCommandManager = app.getStateManager()
                .getState(UserCommandManager.class);
        if (userCommandManager.getPlayerId() == message.getPlayerId()) {
            userCommandManager.setCharacterId(message.getEntityId());
        }
    }

    private void handleUdpHandshakeAck() {
        Sender sender = app.getStateManager().getState(Sender.class);
        if (!handshakeComplete) {
            CmdClientLogin command = new CmdClientLogin(name);
            sender.addCommand(command);
            handshakeComplete = true;
            app.getMenu().setStatusText("");
        }
    }
}