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

import arkhados.World;
import arkhados.net.Command;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;

@Serializable
public class CmdAddEntity implements Command {
    private short entityId;
    private short nodeBuilderId;
    private Vector3f loc = new Vector3f();
    private Quaternion rot = new Quaternion();
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
        this.entityId = (short) entityId;
        this.nodeBuilderId = (short) nodeBuilderId;
        this.loc.set(location);
        this.rot.set(rotation);
        this.playerId = (byte) playerId;
        this.age = age;
    }

    public void applyData(World world) {
        world.addEntity(entityId, nodeBuilderId, loc, rot, playerId, age);
    }

    @Override
    public boolean isGuaranteed() {
        return true;
    }
}
