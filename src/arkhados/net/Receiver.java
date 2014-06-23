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
package arkhados.net;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author william
 */
public class Receiver extends AbstractAppState implements MessageListener {

    private static final Logger logger = Logger.getLogger(Receiver.class.getName());

    static {
        logger.setLevel(Level.SEVERE);
    }
    private List<CommandHandler> handlers = new ArrayList<>();
    private Application app;
    private int lastReceivedOrderNum = -1;
//    private Ack reUsedAck = new Ack(-1);
    private Map<HostedConnection, Integer> lastReceivedOrderNumMap = new HashMap<>();

    public void registerCommandHandler(CommandHandler handler) {
        handlers.add(handler);
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.app = app;
    }

    private void ack(Object source, int otmId) {
        Ack ack = new Ack(otmId);
        logger.log(Level.INFO, "Acking otmId {0}", otmId);
//        reUsedAck.setConfirmedOtmId(otmId);
        Sender sender = app.getStateManager().getState(Sender.class);
        if (sender.isClient()) {
            sender.addCommand(ack);
        } else {
            ((ServerSender) sender).addCommandForSingle(ack, (HostedConnection) source);
        }
    }

    @Override
    public void messageReceived(Object source, Message m) {
        OneTrueMessage otp = (OneTrueMessage) m;

        if (otp.getOrderNum() < getLastReceivedOrderNum(source)) {
            return;
        }

        if (!otp.getGuaranteed().isEmpty()) {
            handleGuaranteed(source, otp);
        }

        setLastReceivedOrderNum(source, otp.getOrderNum());

        handleUnreliable(source, otp);
    }

    private int getLastReceivedOrderNum(Object source) {
        Sender sender = app.getStateManager().getState(Sender.class);
        if (sender.isClient()) {
            return lastReceivedOrderNum;
        } else {
            return lastReceivedOrderNumMap.get((HostedConnection) source);
        }
    }

    private void setLastReceivedOrderNum(Object source, int num) {
        Sender sender = app.getStateManager().getState(Sender.class);
        if (sender.isClient()) {
            lastReceivedOrderNum = num;
        } else {
            lastReceivedOrderNumMap.put((HostedConnection) source, num);
        }
    }

    private void handleGuaranteed(Object source, OneTrueMessage otp) {
        int lastReceivedOrderNumber = getLastReceivedOrderNum(source);
        
        for (OtmIdCommandListPair otmIdCommandListPair : otp.getGuaranteed()) {
            if (otmIdCommandListPair.getOtmId() <= lastReceivedOrderNumber) {
                continue;
            }
                    
            for (CommandHandler commandHandler : handlers) {
                commandHandler.readGuaranteed(source, otmIdCommandListPair.getCommandList());
            }
        }

        ack(source, otp.getOrderNum());
    }

    private void handleUnreliable(Object source, OneTrueMessage otp) {
        for (CommandHandler commandHandler : handlers) {
            commandHandler.readUnreliable(source, otp.getUnreliables());
        }
    }
    
    public void addConnection(HostedConnection connection) {
        lastReceivedOrderNumMap.put(connection, -1);
    }
}