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
package arkhados.confirmation;

import arkhados.ServerClientData;
import arkhados.SyncManager;
import arkhados.WorldManager;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.network.Client;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Server;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author william
 */
public class ConfirmationWaitingSystem extends AbstractAppState implements MessageListener {

    private static int counter = 0;
    private Map<Integer, ConfirmableAction> confirmables = new HashMap<>();
    private Map<ConfirmationTopic, List<Confirmer>> confirmers = new HashMap<>();
    private SimpleApplication app;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.app = (SimpleApplication) app;
    }

    public void configure(Server server) {
        server.addMessageListener(this, ConfirmationRequestMessage.class,
                ConfirmationResponseMessage.class);
    }

    public void configure(Client client) {
        client.addMessageListener(this, ConfirmationRequestMessage.class);
    }

    public void confirmOnTopic(ConfirmationTopic topic, Confirmer confirmer) {
        if (!this.confirmers.containsKey(topic)) {
            this.confirmers.put(topic, new ArrayList<Confirmer>());
        }
        List<Confirmer> topicConfirmers = this.confirmers.get(topic);
        topicConfirmers.add(confirmer);
    }

    public synchronized void pleaseConfirm(ConfirmationTopic topic, PostConfirmationAction action) {
        ConfirmableAction confirm = new ConfirmableAction(action, ServerClientData.getClients());
        this.confirmables.put(counter++, confirm);
    }

    @Override
    public void messageReceived(Object source, Message m) {
        if (this.app.getStateManager().getState(WorldManager.class).isServer()) {
            this.serverMessageReceived((HostedConnection) source, m);
        } else {
            this.clientMessageReceived(m);
        }
    }

    private void clientMessageReceived(Message m) {
        if (m instanceof ConfirmationRequestMessage) {
            ConfirmationRequestMessage request = (ConfirmationRequestMessage) m;
            this.confirmationRequested(request);
        }
    }

    private void serverMessageReceived(HostedConnection client, Message m) {
        if (m instanceof ConfirmationResponseMessage) {
            this.clientConfirmed(client.getId(), (ConfirmationResponseMessage) m);
        }
    }

    private void clientConfirmed(int sourceId, ConfirmationResponseMessage response) {
        if (!response.isAccepted()) {
            //  Handle confirmation AND / OR client
            return;
        }
        ConfirmableAction action = this.confirmables.get(response.getId());
        if (action == null) {
            return;
        }

        boolean actionTotallyConfirmed = action.confirmClient(sourceId);
        if (actionTotallyConfirmed) {
            action.runConfirmableAction();
        }
    }

    private void confirmationRequested(ConfirmationRequestMessage request) {
        boolean confirmed = false;
        for (Confirmer confirmer : this.confirmers.get(request.getTopic())) {
            if (!confirmer.answerConfirmation(request.getTopic())) {
                break;
            }
        }

        if (confirmed) {
            this.sendConfirmation(request.getId());
        }
    }

    private void sendConfirmation(int id) {
        ConfirmationResponseMessage response = new ConfirmationResponseMessage(id, true);
        this.app.getStateManager().getState(SyncManager.class).getClient().send(response);
    }
}

class ConfirmableAction {

    private PostConfirmationAction action;
    private Collection<Integer> clients;
    private Set<Integer> confirmedClients;

    public ConfirmableAction(PostConfirmationAction action, Collection<Integer> clients) {
        this.action = action;
        this.clients = clients;
        this.confirmedClients = new HashSet<>(this.clients.size());
    }

    public boolean confirmClient(int newConfirmed) {
        this.confirmedClients.add(newConfirmed);
        for (Integer clientId : clients) {
            if (!this.confirmedClients.contains(newConfirmed)) {
                return false;
            }
        }

        return true;
    }

    public void runConfirmableAction() {
        this.action.call();
    }

    public Collection<Integer> getClients() {
        return clients;
    }
}