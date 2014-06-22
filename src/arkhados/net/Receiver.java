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
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import java.util.ArrayList;
import java.util.List;
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

    public void registerCommandHandler(CommandHandler handler) {
        this.handlers.add(handler);
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.app = app;
    }

    private void ack(int otmId) {
        this.app.getStateManager().getState(Sender.class).addCommand(new Ack(otmId));
    }

    @Override
    public void messageReceived(Object source, Message m) {
        OneTrueMessage otp = (OneTrueMessage) m;

        if (otp.getOrderNum() < this.lastReceivedOrderNum) {
            return;
        }

        if (otp.getGuaranteed().size() != 1 || !otp.getGuaranteed().get(0).isEmpty()) {
            this.handleGuaranteed(source, otp);
        }
        this.handleUnreliable(source, otp);
    }

    private void handleGuaranteed(Object source, OneTrueMessage otp) {
        int versionDiff = otp.getOrderNum() - this.lastReceivedOrderNum;

        int i = otp.getGuaranteed().size() - versionDiff;

        this.lastReceivedOrderNum = otp.getOrderNum();

        for (; i < otp.getGuaranteed().size(); ++i) {
            for (CommandHandler commandHandler : handlers) {
                commandHandler.readGuaranteed(source, otp.getGuaranteed().get(i));
            }
        }

        this.ack(otp.getOrderNum());
    }

    private void handleUnreliable(Object source, OneTrueMessage otp) {
        for (CommandHandler commandHandler : handlers) {
            commandHandler.readUnreliable(source, otp.getUnreliables());
        }
    }
}