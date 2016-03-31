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

package arkhados.messages.sync;

import com.jme3.network.serializing.Serializable;
import arkhados.World;
import arkhados.net.Command;

@Serializable
public class CmdRemoveEntity implements Command {

    private short entityId;
    private byte reason;

    public CmdRemoveEntity() {
    }

    public CmdRemoveEntity(int entityId, int reason) {
        this.entityId = (short) entityId;
        this.reason = (byte) reason;
    }

    public void applyData(World world) {
        world.removeEntity(entityId, reason);
    }

    @Override
    public boolean isGuaranteed() {
        return true;
    }
}
