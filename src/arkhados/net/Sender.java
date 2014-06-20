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

import arkhados.util.ValueWrapper;
import com.jme3.app.state.AbstractAppState;
import com.jme3.network.NetworkClient;
import com.jme3.network.Server;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author william
 */
public class Sender extends AbstractAppState implements CommandHandler {

    // These should always be added first
    private List<Command> unconfirmedGuaranteed = new ArrayList<>();
    private List<Command> enqueuedGuaranteed = new ArrayList<>();
    private List<Command> enqueuedUnreliables = new ArrayList<>();
    private Map<Integer, Integer> otmIdConfirmsUntil = new HashMap<>();
    private Map<Command, Integer> guaranteedCommandIdMap = new HashMap<>();
    private Server server;
    private ValueWrapper<NetworkClient> client;
    private int otmIdCounter = 0;
    private int guaranteeCounter = 0;

    private void broadcast() {
        assert this.client == null;

        // TODO: Consider reusing same OTM every time to reduce heap fragmentation

        OneTrueMessage otm = this.createOneTrueMessage();
        this.server.broadcast(otm);
    }

    private OneTrueMessage createOneTrueMessage() {
        OneTrueMessage otm = new OneTrueMessage(this.otmIdCounter++);
        otm.getGuaranteed().addAll(this.unconfirmedGuaranteed);
        otm.getGuaranteed().addAll(this.enqueuedGuaranteed);
        otm.getUnreliables().addAll(this.enqueuedUnreliables);

        this.enqueuedUnreliables.clear();
        this.unconfirmedGuaranteed.addAll(enqueuedGuaranteed);
        
        otmIdConfirmsUntil.put(otm.getOrderNum(), this.guaranteeCounter);

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
        for (Iterator<Command> it = unconfirmedGuaranteed.iterator(); it.hasNext();) {
            Command command = it.next();

            if (this.guaranteedCommandIdMap.get(command) <= until) {
                it.remove();
                this.guaranteedCommandIdMap.remove(command);
            }
        }
    }

    public void addCommand(Command command) {
        if (command.isGuaranteed()) {
            this.enqueuedGuaranteed.add(command);
            this.guaranteedCommandIdMap.put(command, this.guaranteeCounter++);

        } else {
            this.enqueuedUnreliables.add(command);
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
        if (this.enqueuedGuaranteed.isEmpty() && this.enqueuedUnreliables.isEmpty()) {
            return;
        }
        if (this.server != null) {
            this.broadcast();
        } else if (this.client.get() != null) {
            this.clientSend();
        }
    }

    @Override
    public void readGuaranteed(List<Command> guaranteed) {
        for (Command command : guaranteed) {
            if (command.getTypeId() == CommandTypeIds.ACK) {
                Ack ack = (Ack) command;
                if (this.otmIdConfirmsUntil.containsKey(ack.getId())) {
                    this.confirmAllUntil(this.otmIdConfirmsUntil.get(ack.getId()));
                }
            }
        }
    }

    @Override
    public void readUnreliable(List<Command> unreliables) {
    }
}