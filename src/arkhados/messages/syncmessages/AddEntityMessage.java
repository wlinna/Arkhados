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
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;

/**
 *
 * @author william
 */
@Serializable
public class AddEntityMessage extends AbstractSyncMessage {
    private long entityId;
    private String modelId;
    private Vector3f location;
    private Quaternion rotation;
    private long playerId;

    public AddEntityMessage() {
    }

    public AddEntityMessage(long entityId, String modelId, Vector3f location, Quaternion rotation, long playerId) {
        this.entityId = entityId;
        this.modelId = modelId;
        this.location = location;
        this.rotation = rotation;
        this.playerId = playerId;
    }

    @Override
    public void applyData(Object target) {
        WorldManager worldManager = (WorldManager) target;
        worldManager.addEntity(this.entityId, this.modelId, this.location,
                this.rotation, playerId);
    }
}
