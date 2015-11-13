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
import com.jme3.network.HostedConnection;
import com.jme3.scene.Spatial;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import arkhados.controls.CSync;
import arkhados.messages.sync.statedata.StateData;
import arkhados.net.Command;
import arkhados.net.CommandHandler;
import arkhados.net.Sender;
import arkhados.settings.server.Settings;
import com.jme3.util.IntMap;

public class Sync extends AbstractAppState implements CommandHandler {

    private final Application app;
    private final AppStateManager stateManager;
    private Sender sender;
    private final IntMap<Object> syncObjects = new IntMap<>();
    private float syncTimer = 0.0f;
    private float defaultSyncFrequency;
    private final Queue<StateData> stateDataQueue = new LinkedList<>();
    private boolean listening = false; // NOTE: Only server is affected

    public Sync(Application app) {
        this.app = app;
        stateManager = app.getStateManager();
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        sender = stateManager.getState(Sender.class);
        if (sender.isServer()) {
            defaultSyncFrequency = Settings.get().General()
                    .getDefaultSyncFrequency();
        }
    }

    @Override
    public void update(float tpf) {
        if (sender.isClient()) {

            for (Iterator<StateData> it = stateDataQueue.iterator();
                    it.hasNext();) {
                StateData stateData = it.next();
                Object object = syncObjects.get(stateData.getSyncId());
                if (object != null) {
                    stateData.applyData(object);
                }

                it.remove();
            }
        } else {
            syncTimer += tpf;
            if (syncTimer >= defaultSyncFrequency) {
                sendSyncData();
                syncTimer = 0.0f;
            }
        }
    }

    private void sendSyncData() {
        ServerFog fog = stateManager.getState(ServerFog.class);

        for (IntMap.Entry<Object> entry : syncObjects) {
            if (!(entry.getValue() instanceof Spatial)) {
                continue;
            }

            Spatial spatial = (Spatial) entry.getValue();

            CSync syncControl = spatial.getControl(CSync.class);
            if (syncControl != null) {
                StateData data = syncControl.getSyncableData(null);
                if (data != null) {
                    fog.addCommand(spatial, data);
                }
            }
        }
    }

    private void doMessage(int syncId, Command command) {
        Object object = syncObjects.get(syncId);

        if (object == null) {
            return;
        }

        if (command instanceof StateData) {
            ((StateData) command).applyData(object);
        }
    }

    public void addObject(int id, Object object) {
        syncObjects.put(id, object);
    }

    public void removeEntity(int id) {
        syncObjects.remove(id);
    }

    public void clear() {
        syncObjects.clear();
        stateDataQueue.clear();
    }

    public void stopListening() {
        listening = false;
    }

    public void startListening() {
        listening = true;
    }

    @Override
    public void readGuaranteed(Object source, Command guaranteed) {
        if (sender.isClient()) {
            clientHandleCommands(guaranteed);
        } else {
            serverHandleCommands((HostedConnection) source, guaranteed);
        }
    }

    @Override
    public void readUnreliable(Object source, Command unreliable) {
        if (sender.isClient()) {
            clientHandleCommands(unreliable);
        } else {
            serverHandleCommands((HostedConnection) source, unreliable);
        }
    }

    private void clientHandleCommands(final Command command) {
        app.enqueue(() -> {
            if (command instanceof StateData) {
                stateDataQueue.add((StateData) command);
            }
            return null;
        });
    }

    private void serverHandleCommands(HostedConnection source,
            final Command command) {
        if (!listening) {
            return;
        }

        int player = ServerClientData.getPlayerId(source.getId());
        final int syncId = PlayerData.getIntData(player, PlayerData.ENTITY_ID);
        if (syncId != -1) {
            app.enqueue(() -> {
                doMessage(syncId, command);
                return null;
            });
        } else {
            System.out.println("Entity id for player " + player
                    + " does not exist");
        }
    }
}
