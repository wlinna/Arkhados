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

import arkhados.spell.buffs.AbstractBuff;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.network.HostedConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author william
 */
public abstract class Sender extends AbstractAppState implements CommandHandler {

    protected static final Logger logger = Logger.getLogger(Sender.class.getName());

    static {
        logger.setLevel(Level.SEVERE);
    }
    private int otmIdCounter = 0;
    private boolean shouldSend;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        AbstractBuff.setSender(this);
    }

    protected OneTrueMessage createOneTrueMessage(HostedConnection connection) {
        List<OtmIdCommandListPair> unconfirmedGuaranteed = getGuaranteedForSource(connection);
        List<Command> enqueuedGuaranteed = getEnqueuedGuaranteedForSource(connection);
        List<Command> enqueuedUnreliables = getEnqueuedUnreliablesForSource(connection);

        OneTrueMessage otm = new OneTrueMessage(otmIdCounter);

        if (!enqueuedGuaranteed.isEmpty()) {
            unconfirmedGuaranteed.add(new OtmIdCommandListPair(otmIdCounter, new ArrayList<>(enqueuedGuaranteed)));
        }

        otm.setOrderNum(otmIdCounter);
        otm.getGuaranteed().clear();
        otm.getUnreliables().clear();

        if (!unconfirmedGuaranteed.isEmpty()) {
            otm.getGuaranteed().addAll(unconfirmedGuaranteed);
        }
        if (!enqueuedUnreliables.isEmpty()) {
            otm.getUnreliables().addAll(enqueuedUnreliables);
        }

        enqueuedGuaranteed.clear();
        enqueuedUnreliables.clear();

        return otm;
    }

    private void confirmAllUntil(Object source, int until) {
        logger.log(Level.INFO, "Confirming all messages until {0}", until);

        List<OtmIdCommandListPair> listToRemoveFrom = getGuaranteedForSource(source);

        for (Iterator<OtmIdCommandListPair> it = listToRemoveFrom.iterator(); it.hasNext();) {
            OtmIdCommandListPair otmIdCommandListPair = it.next();
            if (otmIdCommandListPair.getOtmId() <= until) {
                it.remove();
            } else if (otmIdCommandListPair.getOtmId() > until) {
                break;
            }
        }
    }

    public abstract void addCommand(Command command);

    @Override
    public void update(float tpf) {
        super.update(tpf);

        if (!shouldSend) {
            return;
        }

        logger.info("Sending data");

        sendMessage();

        ++otmIdCounter;

        shouldSend = false;
    }

    public abstract void sendMessage();

    @Override
    public void readGuaranteed(Object source, List<Command> guaranteed) {
    }

    @Override
    public void readUnreliable(Object source, List<Command> unreliables) {
        for (Command command : unreliables) {
            if (command.getTypeId() == CommandTypeIds.ACK) {
                Ack ack = (Ack) command;
                confirmAllUntil(source, ack.getConfirmedOtmId());
                break;
            }
        }
    }

    public abstract boolean isClient();

    public abstract boolean isServer();

    protected abstract List<OtmIdCommandListPair> getGuaranteedForSource(Object source);

    protected abstract List<Command> getEnqueuedGuaranteedForSource(HostedConnection connection);

    protected abstract List<Command> getEnqueuedUnreliablesForSource(HostedConnection connection);

    public void setShouldSend(boolean shouldSend) {
        this.shouldSend = shouldSend;
    }
}