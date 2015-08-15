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

import com.jme3.network.serializing.Serializable;
import arkhados.World;
import arkhados.messages.syncmessages.statedata.StateData;

@Serializable
public class CmdRemoveEntity extends StateData {

    private short entityId;
    private byte reason;

    public CmdRemoveEntity() {
    }

    public CmdRemoveEntity(int entityId, int reason) {
        super(-1);
        this.entityId = (short) entityId;
        this.reason = (byte) reason;
    }

    @Override
    public void applyData(Object target) {
        World world = (World) target;
        world.removeEntity(entityId, reason);
    }
}
