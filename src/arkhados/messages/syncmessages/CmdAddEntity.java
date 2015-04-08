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
public class CmdAddEntity extends StateData {
    private int entityId;
    private short nodeBuilderId;
    private Vector3f location;
    private Quaternion rotation;
    private byte playerId;
    private float age;

    public CmdAddEntity() {
    }

    public CmdAddEntity(int entityId, int nodeBuilderId, Vector3f location,
            Quaternion rotation, int playerId) {
        this(entityId, nodeBuilderId, location, rotation, playerId, 0f);
    }

    public CmdAddEntity(int entityId, int nodeBuilderId, Vector3f location,
            Quaternion rotation, int playerId, float age) {
        this.entityId = entityId;
        this.nodeBuilderId = (short) nodeBuilderId;
        this.location = location;
        this.rotation = rotation;
        this.playerId = (byte) playerId;
        this.age = age;
    }

    @Override
    public void applyData(Object target) {
        WorldManager worldManager = (WorldManager) target;
        worldManager.addEntity(entityId, nodeBuilderId, location,
                rotation, playerId, age);
    }
}
