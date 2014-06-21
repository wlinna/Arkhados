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
import arkhados.util.ValueWrapper;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.network.NetworkClient;
import com.jme3.network.Server;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author william
 */
public class Sender extends AbstractAppState implements CommandHandler {

    private static final Logger logger = Logger.getLogger(Sender.class.getName());
    static {
        logger.setLevel(Level.SEVERE);
    }
    // These should always be added first
    private List<List<Command>> unconfirmedGuaranteed = new ArrayList<>();
    private List<Command> enqueuedGuaranteed = new ArrayList<>();
    private List<Command> enqueuedUnreliables = new ArrayList<>();
    private Server server;
    private ValueWrapper<NetworkClient> client;
    private int otmIdCounter = 0;

    public Sender(ValueWrapper<NetworkClient> client) {
        this.client = client;
    }

    public Sender(Server server) {
        this.server = server;
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        AbstractBuff.setSender(this);
    }

    private void broadcast() {
        assert this.client == null;

        // TODO: Consider reusing same OTM every time to reduce heap fragmentation

        OneTrueMessage otm = this.createOneTrueMessage();
        this.server.broadcast(otm);
    }

    private OneTrueMessage createOneTrueMessage() {
        this.unconfirmedGuaranteed.add(new ArrayList<>(this.enqueuedGuaranteed));

        OneTrueMessage otm = new OneTrueMessage(this.otmIdCounter++);
        
        otm.getGuaranteed().addAll(this.unconfirmedGuaranteed);
        otm.getUnreliables().addAll(this.enqueuedUnreliables);

        this.enqueuedGuaranteed.clear();
        this.enqueuedUnreliables.clear();

        return otm;
    }

    private void clientSend() {
        assert this.client != null && this.server == null;
        OneTrueMessage otm = this.createOneTrueMessage();

        this.client.get().send(otm);
    }

    public void forceSend() {
        this.broadcast();
    }

    private void confirmAllUntil(int until) {
        logger.log(Level.INFO, "Confirming all messages until {0}", until);

        int listsToRemove = this.unconfirmedGuaranteed.size() - (this.otmIdCounter - 1 - until);

        Iterator<List<Command>> iterator = this.unconfirmedGuaranteed.iterator();

        for (; listsToRemove > 0; --listsToRemove) {
            iterator.next();
            iterator.remove();
        }
        
    }

    public void addCommand(Command command) {
        if (command.isGuaranteed()) {
            this.enqueuedGuaranteed.add(command);
            logger.log(Level.INFO, "Adding GUARANTEED command");

        } else {
            this.enqueuedUnreliables.add(command);
            logger.info("Adding UNRELIABLE command");
        }
    }

    public void addCommands(List<? extends Command> commands) {
        for (Command command : commands) {
            this.addCommand(command);
        }
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
        boolean shouldSend = false;

        if (!this.enqueuedGuaranteed.isEmpty()) {
            logger.info("Guaranteed commands waiting");
            shouldSend = true;
        }

        if (!this.enqueuedUnreliables.isEmpty()) {
            logger.info("Unreliable commands waiting");
            shouldSend = true;
        }

        if (!shouldSend) {
            return;
        }

        logger.info("Sending data");

        if (this.server != null) {
            this.broadcast();
        } else if (this.client.get() != null) {
            this.clientSend();
        }
    }

    @Override
    public void readGuaranteed(List<Command> guaranteed) {
    }

    @Override
    public void readUnreliable(List<Command> unreliables) {
        for (Command command : unreliables) {
            if (command.getTypeId() == CommandTypeIds.ACK) {
                Ack ack = (Ack) command;
                this.confirmAllUntil(ack.getConfirmedOtmId());
                break;
            }
        }
    }

    public boolean isServer() {
        return this.server != null;
    }
}