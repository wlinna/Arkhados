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
import arkhados.controls.CharacterPhysicsControl;
import arkhados.controls.ProjectileControl;
import arkhados.messages.syncmessages.AbstractSyncMessage;
import arkhados.messages.syncmessages.GenericSyncMessage;
import arkhados.messages.syncmessages.SyncCharacterMessage;
import arkhados.messages.syncmessages.SyncProjectileMessage;
import arkhados.spell.buffs.AbstractBuff;
import arkhados.util.PlayerDataStrings;
import com.jme3.cinematic.MotionPath;
import com.jme3.cinematic.events.MotionEvent;

/**
 *
 * @author william
 */
public class SyncManager extends AbstractAppState implements MessageListener {

    private Server server = null;
    private Client client = null;
    private Application app;
    HashMap<Long, Object> syncObjects = new HashMap<Long, Object>();
    private float syncTimer = 0.0f;
    private Queue<AbstractSyncMessage> syncQueue =
            new LinkedList<AbstractSyncMessage>();
    private boolean listening = false; // NOTE: Only server is affected

    public SyncManager(Application app, Server server) {
        this.app = app;
        this.server = server;
    }

    public SyncManager(Application app, Client client) {
        this.app = app;
        this.client = client;
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        System.out.println("Initializing SyncManager");
        super.initialize(stateManager, app);
        AbstractBuff.setSyncManager(this);
        System.out.println("Initialized SyncManager");
    }

    @Override
    public void update(float tpf) {
        if (this.client != null) {
            for (Iterator<AbstractSyncMessage> it = this.syncQueue.iterator();
                    it.hasNext();) {
                AbstractSyncMessage message = it.next();
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
        for (Iterator<Entry<Long, Object>> it = this.syncObjects.entrySet().iterator(); it.hasNext();) {
            Entry<Long, Object> entry = it.next();
            if (!(entry.getValue() instanceof Spatial)) {
                continue;
            }

            Spatial spatial = (Spatial) entry.getValue();

            CharacterPhysicsControl characterControl = spatial.getControl(CharacterPhysicsControl.class);
            if (characterControl != null) {
                SyncCharacterMessage syncMessage = new SyncCharacterMessage(entry.getKey(), entry.getValue());
                this.broadcast(syncMessage);
            }

            ProjectileControl projectileControl = spatial.getControl(ProjectileControl.class);
            if (projectileControl != null) {
                SyncProjectileMessage syncMessage = new SyncProjectileMessage(entry.getKey(), projectileControl);
                this.broadcast(syncMessage);
            }

            MotionEvent motionEvent = spatial.getControl(MotionEvent.class);
            if (motionEvent != null && characterControl == null) {
                GenericSyncMessage syncMessage = new GenericSyncMessage(entry.getKey(), spatial);
                this.broadcast(syncMessage);
            }
        }
    }

    public void setMessagesToListen(Class... classes) {
        if (this.client != null) {
            this.client.addMessageListener(this, classes);
        } else if (this.server != null) {
            this.server.addMessageListener(this, classes);
        }
    }

    private void doMessage(AbstractSyncMessage message) {
        Object object = this.syncObjects.get(message.getSyncId());
        if (object != null) {
            message.applyData(object);
        } else {
//            System.out.println("SyncId does not exist");
        }
    }

    public void broadcast(AbstractSyncMessage message) {
        this.server.broadcast(message);
    }

    public void messageReceived(Object source, final Message m) {

        assert (m instanceof AbstractSyncMessage);
        final AbstractSyncMessage message = (AbstractSyncMessage) m;
        if (this.client != null) {
            this.app.enqueue(new Callable<Void>() {
                public Void call() throws Exception {
                    SyncManager.this.enqueueMessage(message);
                    return null;
                }
            });
        } else if (this.server != null) {
            if (!this.listening) {
                return;
            }
            HostedConnection client = (HostedConnection) source;
            final long playerId = ServerClientData.getPlayerId(client.getId());
            // FIXME: Sometimes NullPointerException occurs when receiving UcWalkDirection at start
            final long syncId = PlayerData.getLongData(playerId, PlayerDataStrings.ENTITY_ID);
            message.setSyncId(syncId);
            this.app.enqueue(new Callable<Void>() {
                public Void call() throws Exception {
                    SyncManager.this.doMessage(message);
                    return null;
                }
            });
        }
    }

    public Server getServer() {
        return this.server;
    }

    void addObject(long id, Object object) {
        this.syncObjects.put(id, object);
    }

    private void enqueueMessage(AbstractSyncMessage message) {
        this.syncQueue.add(message);
    }

    public Client getClient() {
        return this.client;
    }

    public void removeEntity(long id) {
        this.syncObjects.remove(id);
    }

    public void clear() {
        this.syncObjects.clear();
        this.syncQueue.clear();
    }

    public void stopListening() {
        this.listening = false;
    }

    public void startListening() {
        this.listening = true;
    }
}