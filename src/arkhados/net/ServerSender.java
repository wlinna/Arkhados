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

import com.jme3.network.Filters;
import com.jme3.network.HostedConnection;
import com.jme3.network.Server;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 *
 * @author william
 */
public class ServerSender extends Sender {

    private Map<HostedConnection, List<OtmIdCommandListPair>> unconfirmedGuaranteed = new HashMap<>();
    private Map<HostedConnection, List<Command>> enqueuedGuaranteed = new HashMap<>();
    private Map<HostedConnection, List<Command>> enqueuedUnreliables = new HashMap<>();
    private Server server;

    public ServerSender(Server server) {
        this.server = server;
    }

    private void broadcast() {
        for (HostedConnection connection : enqueuedGuaranteed.keySet()) {
            OneTrueMessage otm = createOneTrueMessage(connection);            
            server.broadcast(Filters.in(connection), otm);
        }
    }

    
    public void addCommandForSingle(Command command, HostedConnection connection) {
        if (command.isGuaranteed()) {
            logger.log(Level.INFO, "Adding GUARANTEED command");
            enqueuedGuaranteed.get(connection).add(command);
        } else {
            logger.info("Adding UNRELIABLE command");

            enqueuedUnreliables.get(connection).add(command);
        }

        setShouldSend(true);
    }

    @Override
    public void addCommand(Command command) {
        addCommand(command, server.getConnections());
    }

    public void addCommand(Command command, Collection<HostedConnection> connections) {
        setShouldSend(true);
        for (HostedConnection hostedConnection : connections) {
            addCommandForSingle(command, hostedConnection);
        }
    }

    public void addConnection(HostedConnection conn) {
        unconfirmedGuaranteed.put(conn, new ArrayList<OtmIdCommandListPair>());
        enqueuedGuaranteed.put(conn, new ArrayList<Command>());
        enqueuedUnreliables.put(conn, new ArrayList<Command>());
    }

    @Override
    public boolean isClient() {
        return false;
    }

    @Override
    public boolean isServer() {
        return true;
    }

    @Override
    public void sendMessage() {
        this.broadcast();
    }

    @Override
    protected List<OtmIdCommandListPair> getGuaranteedForSource(Object source) {
        return unconfirmedGuaranteed.get((HostedConnection) source);
    }

    @Override
    protected List<Command> getEnqueuedGuaranteedForSource(HostedConnection connection) {
        return enqueuedGuaranteed.get(connection);
    }

    @Override
    protected List<Command> getEnqueuedUnreliablesForSource(HostedConnection connection) {
        return enqueuedUnreliables.get(connection);
    }
}