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

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.network.Client;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Server;
import com.jme3.scene.Spatial;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.Callable;
import arkhados.controls.SyncControl;
import arkhados.messages.syncmessages.AbstractSyncMessage;
import arkhados.messages.syncmessages.ActionMessage;
import arkhados.messages.syncmessages.AddEntityMessage;
import arkhados.messages.syncmessages.BuffMessage;
import arkhados.messages.syncmessages.MassSyncMessage;
import arkhados.messages.syncmessages.RemoveEntityMessage;
import arkhados.messages.syncmessages.RestoreTemporarilyRemovedEntityMessage;
import arkhados.messages.syncmessages.SetCooldownMessage;
import arkhados.messages.syncmessages.StartCastingSpellMessage;
import arkhados.messages.syncmessages.TemporarilyRemoveEntityMessage;
import arkhados.messages.syncmessages.statedata.StateData;
import arkhados.spell.buffs.AbstractBuff;
import arkhados.util.PlayerDataStrings;
import arkhados.util.ValueWrapper;
import com.jme3.network.AbstractMessage;
import com.jme3.network.NetworkClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author william
 */
public class SyncManager extends AbstractAppState implements MessageListener {

    private Server server = null;
    private ValueWrapper<NetworkClient> client;
    private Application app;
    HashMap<Integer, Object> syncObjects = new HashMap<>();
    private float syncTimer = 0.0f;
    private Queue<AbstractMessage> syncQueue = new LinkedList<>();
    private boolean listening = false; // NOTE: Only server is affected
    private int orderNum = 0;

    public SyncManager(Application app, Server server) {
        this.app = app;
        this.server = server;
    }

    public SyncManager(Application app, ValueWrapper<NetworkClient> client) {
        this.app = app;
        this.client = client;
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        AbstractBuff.setSyncManager(this);
    }

    @Override
    public void update(float tpf) {
        if (this.getClient() != null) {
            for (Iterator<AbstractMessage> it = this.syncQueue.iterator();
                    it.hasNext();) {
                AbstractMessage message = it.next();
                this.doMessage(message);
                it.remove();
            }
        } else if (this.server != null) {
            this.syncTimer += tpf;
            if (this.syncTimer >= Globals.DEFAULT_SYNC_FREQUENCY) {
                this.sendSyncData();
                this.syncTimer = 0.0f;
            }
        }
    }

    private void sendSyncData() {
        MassSyncMessage massSyncMessage = new MassSyncMessage();

        Set<Entry<Integer, Object>> entrySet = this.syncObjects.entrySet();
        List<StateData> stateData = new ArrayList<>(this.syncObjects.entrySet().size());

        for (Iterator<Entry<Integer, Object>> it = entrySet.iterator(); it.hasNext();) {
            Entry<Integer, Object> entry = it.next();
            if (!(entry.getValue() instanceof Spatial)) {
                continue;
            }

            Spatial spatial = (Spatial) entry.getValue();

            SyncControl syncControl = spatial.getControl(SyncControl.class);
            if (syncControl != null) {
                StateData data = syncControl.getSyncableData(null);
                if (data != null) {
                    stateData.add(data);
                }
            }
        }

        if (!stateData.isEmpty()) {
            massSyncMessage.setStateData(stateData);
            this.broadcast(massSyncMessage);
        }
    }

    public void setMessagesToListen(Class... classes) {
        if (this.getClient() != null) {
            this.getClient().addMessageListener(this, classes);
        } else if (this.server != null) {
            this.server.addMessageListener(this, classes);
        }
    }

    private void doMessage(AbstractMessage m) {
        if (m instanceof AbstractSyncMessage) {
            AbstractSyncMessage message = (AbstractSyncMessage) m;
            Object object = this.syncObjects.get(message.getSyncId());
            if (object != null) {
                message.applyData(object);
            }
        } else if (m instanceof MassSyncMessage) {
            this.clientApplyMassSyncMessage((MassSyncMessage) m);
        }
    }

    public void broadcast(AbstractMessage message) {
        assert message.getClass().isAssignableFrom(MassSyncMessage.class)
                || message.getClass().isAssignableFrom(AbstractSyncMessage.class);
        this.server.broadcast(message);
    }

    @Override
    public void messageReceived(Object source, final Message m) {
        if (this.getClient() != null) {
            this.clientReceivedMessage((AbstractMessage) m);
        } else if (this.server != null) {
            if (!this.listening) {
                return;
            }

            this.serverReceivedMessage((HostedConnection) source, (AbstractSyncMessage) m);
        }
    }

    private void clientApplyMassSyncMessage(MassSyncMessage message) {
        for (StateData stateData : message.getStateData()) {
            Object object = this.syncObjects.get(stateData.getSyncId());
            if (object != null) {
                stateData.applyData(object);
            }
        }
    }

    private void clientReceivedMessage(final AbstractMessage message) {
        if (message instanceof MassSyncMessage) {
            MassSyncMessage stateMessage = (MassSyncMessage) message;
            if (stateMessage.getOrderNum() <= this.getCurrentOrderNum()) {
                return;
            } else {
                this.setCurrentOrderNum(stateMessage.getOrderNum());
            }
        }
        this.app.enqueue(new Callable<Void>() {
            public Void call() throws Exception {
                SyncManager.this.enqueueMessage(message);
                return null;
            }
        });
    }

    private void serverReceivedMessage(HostedConnection source, final AbstractSyncMessage message) {
        final int playerId = ServerClientData.getPlayerId(source.getId());
        final int syncId = PlayerData.getIntData(playerId, PlayerDataStrings.ENTITY_ID);
        if (syncId != -1) {
            message.setSyncId(syncId);
            this.app.enqueue(new Callable<Void>() {
                public Void call() throws Exception {
                    SyncManager.this.doMessage(message);
                    return null;
                }
            });
        } else {
            System.out.println("Entity id for player " + playerId + " does not exist");
        }
    }

    public Server getServer() {
        return this.server;
    }

    void addObject(int id, Object object) {
        this.syncObjects.put(id, object);
    }

    private void enqueueMessage(AbstractMessage message) {
        this.syncQueue.add(message);
    }

    public Client getClient() {
        if (this.client == null) {
            return null;
        }
        return this.client.get();
    }

    public void removeEntity(int id) {
        this.syncObjects.remove(id);
    }

    public void clear() {
        this.syncObjects.clear();
        this.syncQueue.clear();
        this.orderNum = 0;
    }

    public void stopListening() {
        this.listening = false;
    }

    public void startListening() {
        this.listening = true;
    }

    public void configureForClient() {
        this.getClient().addMessageListener(this,
                AddEntityMessage.class,
                RestoreTemporarilyRemovedEntityMessage.class,
                RemoveEntityMessage.class,
                TemporarilyRemoveEntityMessage.class,
                MassSyncMessage.class,
                StartCastingSpellMessage.class,
                SetCooldownMessage.class,
                ActionMessage.class,
                BuffMessage.class);
    }

    private int getCurrentOrderNum() {
        return this.orderNum;
    }

    private void setCurrentOrderNum(int orderNum) {
        this.orderNum = orderNum;
    }
}