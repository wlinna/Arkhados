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
package arkhados.actions;

import arkhados.Globals;
import arkhados.ServerFog;
import arkhados.messages.sync.CmdEndAction;
import arkhados.util.UserData;
import com.jme3.scene.Spatial;

public abstract class EntityAction {

    protected String name = null;
    private int typeId = -1;
    protected Spatial spatial;
    private boolean interruptible = false;

    /**
     *
     * @param tpf
     * @return true if action is still active and should not be removed, false
     * if action is completed and should be removed
     */
    public abstract boolean update(float tpf);

    public void setSpatial(Spatial spatial) {
        this.spatial = spatial;
    }

    /**
     * ActionQueueControl calls end method when it's removed from queue if it's
     * active
     */
    public void end() {
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isInterruptible() {
        return interruptible;
    }

    public void setInterruptible(boolean interruptible) {
        this.interruptible = interruptible;
    }

    public int getTypeId() {
        return typeId;
    }
    
    public void setTypeId(int id) {
        this.typeId = id;
    }
    
    protected void announceEnd() {
        int entityId = spatial.getUserData(UserData.ENTITY_ID);
        CmdEndAction endAction = new CmdEndAction(entityId);
        Globals.app.getStateManager().getState(ServerFog.class)
                .addCommand(spatial, endAction);
    }
}
