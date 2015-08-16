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
package arkhados.messages.sync.statedata;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;
import com.jme3.scene.Spatial;

@Serializable
public class ProjectileSyncData extends StateData {

    private Vector3f location = new Vector3f();
    private Vector3f velocity = new Vector3f();
    private Quaternion rotation = new Quaternion();

    public ProjectileSyncData() {
    }

    public ProjectileSyncData(int syncId, RigidBodyControl control) {
        super(syncId);
        control.getPhysicsLocation(this.location);
        control.getLinearVelocity(this.velocity);
        control.getPhysicsRotation(this.rotation);
    }

    @Override
    public void applyData(Object target) {
        Spatial spatial = (Spatial) target;
        spatial.getControl(RigidBodyControl.class).setPhysicsLocation(this.location);
        spatial.getControl(RigidBodyControl.class).setLinearVelocity(this.velocity);
        spatial.getControl(RigidBodyControl.class).setPhysicsRotation(this.rotation);
    }

    @Override
    public boolean isGuaranteed() {
        return false;
    }
}