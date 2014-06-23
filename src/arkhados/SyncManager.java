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
import com.jme3.network.Server;
import com.jme3.scene.Spatial;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.Callable;
import arkhados.controls.SyncControl;
import arkhados.messages.syncmessages.statedata.StateData;
import arkhados.net.Command;
import arkhados.net.CommandHandler;
import arkhados.net.CommandTypeIds;
import arkhados.net.Sender;
import arkhados.util.PlayerDataStrings;
import arkhados.util.ValueWrapper;
import com.jme3.network.NetworkClient;
import java.util.List;
import java.util.Set;

/**
 *
 * @author william
 */
public class SyncManager extends AbstractAppState implements CommandHandler {

    private Server server = null;
    private ValueWrapper<NetworkClient> client;
    private Application app;
    HashMap<Integer, Object> syncObjects = new HashMap<>();
    private float syncTimer = 0.0f;
    private Queue<StateData> stateDataQueue = new LinkedList<>();
    private boolean listening = false; // NOTE: Only server is affected

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
    }

    @Override
    public void update(float tpf) {
        if (this.getClient() != null) {

            for (Iterator<StateData> it = stateDataQueue.iterator(); it.hasNext();) {
                StateData stateData = it.next();
                Object object = this.syncObjects.get(stateData.getSyncId());
                if (object != null) {
                    stateData.applyData(object);
                }

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
        Sender sender = app.getStateManager().getState(Sender.class);
        Set<Entry<Integer, Object>> entrySet = this.syncObjects.entrySet();

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
                    sender.addCommand(data);
                }
            }
        }
    }

    private void doMessage(int syncId, List<Command> m) {

        Object object = this.syncObjects.get(syncId);
        if (object != null) {
            for (Command command : m) {
                if (command.getTypeId() == CommandTypeIds.SYNC_DATA) {
                    ((StateData) command).applyData(object);
                }
            }
        }
    }

    public Server getServer() {
        return this.server;
    }

    void addObject(int id, Object object) {
        this.syncObjects.put(id, object);
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
        syncObjects.clear();
        stateDataQueue.clear();
    }

    public void stopListening() {
        this.listening = false;
    }

    public void startListening() {
        this.listening = true;
    }

    @Override
    public void readGuaranteed(Object source, List<Command> guaranteed) {
        if (this.client != null && this.client.get() != null) {
            this.clientHandleCommands(guaranteed);
        } else if (this.server != null) {
            this.serverHandleCommands((HostedConnection) source, guaranteed);
        }
    }

    @Override
    public void readUnreliable(Object source, List<Command> unreliables) {
        if (this.client != null && this.client.get() != null) {
            this.clientHandleCommands(unreliables);
        } else if (this.server != null) {
            this.serverHandleCommands((HostedConnection) source, unreliables);
        }
    }

    private void clientHandleCommands(final List<Command> stateDataList) {
        this.app.enqueue(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                for (Command command : stateDataList) {
                    if (command.getTypeId() == CommandTypeIds.SYNC_DATA) {
                        stateDataQueue.add((StateData) command);
                    }
                }
                return null;
            }
        });
    }

    private void serverHandleCommands(HostedConnection source, final List<Command> commands) {
        final int playerId = ServerClientData.getPlayerId(source.getId());
        final int syncId = PlayerData.getIntData(playerId, PlayerDataStrings.ENTITY_ID);
        if (syncId != -1) {
            this.app.enqueue(new Callable<Void>() {
                public Void call() throws Exception {
                    doMessage(syncId, commands);
                    return null;
                }
            });
        } else {
            System.out.println("Entity id for player " + playerId + " does not exist");
        }
    }
}