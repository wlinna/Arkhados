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
package arkhados.messages.syncmessages;

import arkhados.WorldManager;
import arkhados.messages.syncmessages.statedata.StateData;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;

/**
 *
 * @author william
 */
@Serializable
public class RestoreTemporarilyRemovedEntityCommand extends StateData {

    private int entityId;
    private Vector3f location;
    private Quaternion rotation;

    public RestoreTemporarilyRemovedEntityCommand() {
    }

    public RestoreTemporarilyRemovedEntityCommand(int entityId, Vector3f location, Quaternion rotation) {
        this.entityId = entityId;
        this.location = location;
        this.rotation = rotation;
    }

    @Override
    public void applyData(Object target) {
        WorldManager worldManager = (WorldManager) target;
        worldManager.restoreTemporarilyRemovedEntity(this.entityId, this.location, this.rotation);
    }
}