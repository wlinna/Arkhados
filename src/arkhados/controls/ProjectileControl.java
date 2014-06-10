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
package arkhados.controls;

import arkhados.CollisionGroups;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import arkhados.WorldManager;
import arkhados.actions.SplashAction;
import arkhados.messages.syncmessages.statedata.ProjectileSyncData;
import arkhados.messages.syncmessages.statedata.StateData;
import arkhados.util.UserDataStrings;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;

/**
 *
 * @author william
 */
public class ProjectileControl extends AbstractControl implements SyncControl {

    private Vector3f direction = null;
    private RigidBodyControl rigidBodyControl;
    private float age = 0;
    private static WorldManager worldManager;
    private static final float timeToLive = 3.0f;
    private float range = 0f;
    private float speed = 0f;
    private InfluenceInterfaceControl ownerInterface;
    private SplashAction splashAction = null;
    private boolean needsSync = true;   

    // Not used anymore but I'm saving it for projectiles that can be set
    // to explode at selected location
    public void setTarget(Vector3f target) {
        this.direction = target.subtract(this.rigidBodyControl.getPhysicsLocation()).setY(0.0f)
                .normalizeLocal().multLocal((Float) super.getSpatial().getUserData(UserDataStrings.SPEED_MOVEMENT));
        Quaternion rotation = new Quaternion();
        rotation.lookAt(direction, Vector3f.UNIT_Y);
        this.rigidBodyControl.setPhysicsRotation(rotation);

        this.rigidBodyControl.setLinearVelocity(this.direction);
        this.speed = this.direction.length();
        this.rigidBodyControl.setGravity(Vector3f.ZERO);
    }

    /**
     * Use this method to set projectiles direction if you have it already.
     *
     * @param direction Direction of projectile. ProjectileControl takes its
     * ownerships.
     */
    public void setDirection(Vector3f direction) {
        this.direction = direction.setY(0f).normalizeLocal().multLocal(
                (Float) super.getSpatial().getUserData(
                UserDataStrings.SPEED_MOVEMENT));
        Quaternion rotation = new Quaternion();
        rotation.lookAt(direction, Vector3f.UNIT_Y);
        this.rigidBodyControl.setPhysicsRotation(rotation);
        this.rigidBodyControl.setLinearVelocity(this.direction);
        this.speed = this.direction.length();
        this.rigidBodyControl.setGravity(Vector3f.ZERO);
    }

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        this.rigidBodyControl = spatial.getControl(RigidBodyControl.class);
        
        // HACK: We need to allow projectiles to control this
        this.rigidBodyControl.addCollideWithGroup(CollisionGroups.WALLS);
    }

    @Override
    protected void controlUpdate(float tpf) {
        if (this.direction == null) {
            this.rigidBodyControl.setGravity(Vector3f.ZERO);
            return;
        }        
        this.age += tpf;

        if (this.range > 0f && this.age * this.speed >= this.range) {
            if (this.splashAction != null) {
                splashAction.update(tpf);
            }
            
            ProjectileControl.worldManager.removeEntity((Integer) super.spatial.getUserData(UserDataStrings.ENTITY_ID), "expiration");
        }

        if (this.age > ProjectileControl.timeToLive) {
            if (this.splashAction != null) {
                splashAction.update(tpf);
            }
            ProjectileControl.worldManager.removeEntity((Integer) super.spatial.getUserData(UserDataStrings.ENTITY_ID), "expiration");
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public RigidBodyControl getRigidBodyControl() {
        return this.rigidBodyControl;
    }

    public static void setWorldManager(WorldManager worldManager) {
        ProjectileControl.worldManager = worldManager;
    }

    public void setRange(float range) {
        this.range = range;
    }

    public InfluenceInterfaceControl getOwnerInterface() {
        return this.ownerInterface;
    }

    public void setOwnerInterface(InfluenceInterfaceControl ownerInterface) {
        this.ownerInterface = ownerInterface;
    }

    public void setSplashAction(SplashAction splashAction) {
        this.splashAction = splashAction;
    }

    @Override
    public StateData getSyncableData(StateData stateData) {
        if (this.needsSync) {
            this.needsSync = true;
            return new ProjectileSyncData((int) super.getSpatial().getUserData(UserDataStrings.ENTITY_ID), this);
        }

        return null;
    }
}