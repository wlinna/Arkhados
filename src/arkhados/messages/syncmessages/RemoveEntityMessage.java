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

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import arkhados.WorldManager;

/**
 *
 * @author william
 */
@Serializable
public class RemoveEntityMessage extends AbstractSyncMessage {

    private int entityId;
    private String reason = "";

    public RemoveEntityMessage() {
    }

    public RemoveEntityMessage(int entityId) {
        this.entityId = entityId;
        super.setSyncId(-1);
    }

    public RemoveEntityMessage(int entityId, String reason) {
        this.entityId = entityId;
        this.reason = reason;
        super.setSyncId(-1);
    }

    @Override
    public void applyData(Object target) {
        WorldManager worldManager = (WorldManager) target;
        worldManager.removeEntity(entityId, reason);
    }
}
