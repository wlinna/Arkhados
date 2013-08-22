/*    This file is part of JMageBattle.

    JMageBattle is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    JMageBattle is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with JMageBattle.  If not, see <http://www.gnu.org/licenses/>. */

package magebattle.messages.syncmessages;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;
import magebattle.WorldManager;

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

    public AddEntityMessage() {
    }

    public AddEntityMessage(long entityId, String modelId, Vector3f location, Quaternion rotation) {
        this.entityId = entityId;
        this.modelId = modelId;
        this.location = location;
        this.rotation = rotation;
    }

    public long getEntityId() {
        return this.entityId;
    }

    public String getModelId() {
        return this.modelId;
    }

    public Vector3f getLocation() {
        return this.location;
    }

    public Quaternion getRotation() {
        return this.rotation;
    }

    @Override
    public void applyData(Object object) {
        WorldManager worldManager = (WorldManager) object;
        worldManager.addEntity(this.entityId, this.modelId, this.location,
                this.rotation);
    }



}
