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

import com.jme3.network.Client;
import com.jme3.network.HostedConnection;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author william
 */
public class ClientSender extends Sender {

    private List<OtmIdCommandListPair> unconfirmedGuaranteed = new ArrayList<>();
    private List<Command> enqueuedGuaranteed = new ArrayList<>();
    private List<Command> enqueuedUnreliables = new ArrayList<>();
    private Client client;

    public ClientSender(Client client) {
    }

    @Override
    public void addCommand(Command command) {
        if (this.client == null || !this.client.isConnected()) {
            return;
        }

        if (command.isGuaranteed()) {
            enqueuedGuaranteed.add(command);
        } else {
            enqueuedUnreliables.add(command);
        }

        setShouldSend(true);
    }

    @Override
    public void sendMessage() {
        OneTrueMessage otm = createOneTrueMessage(null);
        client.send(otm);
    }

    @Override
    public boolean isClient() {
        return true;
    }

    @Override
    public boolean isServer() {
        return false;
    }

    @Override
    protected List<OtmIdCommandListPair> getGuaranteedForSource(Object source) {
        return unconfirmedGuaranteed;
    }

    @Override
    protected List<Command> getEnqueuedGuaranteedForSource(HostedConnection connection) {
        return enqueuedGuaranteed;
    }

    @Override
    protected List<Command> getEnqueuedUnreliablesForSource(HostedConnection connection) {
        return enqueuedUnreliables;
    }

    public void setClient(Client client) {
        this.client = client;
    }
}