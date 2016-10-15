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
import arkhados.World;
import arkhados.actions.ASplash;
import arkhados.messages.sync.statedata.ProjectileSyncData;
import arkhados.messages.sync.statedata.StateData;
import arkhados.util.RemovalReasons;
import arkhados.util.UserData;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import java.util.HashSet;
import java.util.Set;

public class CProjectile extends AbstractControl implements CSync {

    private Vector3f direction = null;
    private RigidBodyControl rigidBodyControl;
    private float age = 0;
    private static World world;
    private static final float timeToLive = 3.0f;
    private float range = 0f;
    private float speed = 0f;
    private CInfluenceInterface ownerInterface;
    private ASplash splashAction = null;
    private boolean needsSync = true;
    // This is if we want spatial to behave like projectile without certain properties
    private boolean isProjectile = true;
    private final Set<Spatial> hurtList = new HashSet<>();

    public void setTarget(Vector3f target) {
        float speedMovement = getSpatial().getUserData(UserData.SPEED);
        direction = target.subtract(rigidBodyControl.getPhysicsLocation())
                .setY(0f).normalizeLocal().multLocal(speedMovement);
        Quaternion rotation = new Quaternion();
        rotation.lookAt(direction, Vector3f.UNIT_Y);
        rigidBodyControl.setPhysicsRotation(rotation);

        rigidBodyControl.setLinearVelocity(direction);
        speed = direction.length();
        rigidBodyControl.setGravity(Vector3f.ZERO);
        range = rigidBodyControl.getPhysicsLocation().distance(target);
        range = FastMath.clamp(range, 0.001f, range);
    }

    /**
     * Use this method to set projectiles direction if you have it already.
     *
     * @param direction Direction of projectile. ProjectileControl takes its
     * ownerships.
     */
    public void setDirection(Vector3f direction) {
        float speedMovement
                = getSpatial().getUserData(UserData.SPEED);
        this.direction
                = direction.setY(0f).normalizeLocal().multLocal(speedMovement);
        Quaternion rotation = new Quaternion();
        rotation.lookAt(direction, Vector3f.UNIT_Y);
        rigidBodyControl.setPhysicsRotation(rotation);
        rigidBodyControl.setLinearVelocity(direction);
        speed = direction.length();
        rigidBodyControl.setGravity(Vector3f.ZERO);
    }

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        if (spatial == null) {
            return;
        }

        rigidBodyControl = spatial.getControl(RigidBodyControl.class);

        // This is default behaviour and can be overridden
        rigidBodyControl.addCollideWithGroup(CollisionGroups.WALLS);
    }

    @Override
    protected void controlUpdate(float tpf) {
        if (direction == null) {
            rigidBodyControl.setGravity(Vector3f.ZERO);
            return;
        }
        age += tpf;

        if (range > 0f && age * speed >= range) {
            if (splashAction != null) {
                splashAction.update(tpf);
            }

            int entityId = spatial.getUserData(UserData.ENTITY_ID);
            world.removeEntity(entityId, RemovalReasons.EXPIRED);
            return;
        }

        if (age > CProjectile.timeToLive) {
            if (splashAction != null) {
                splashAction.update(tpf);
            }
            world.removeEntity((int) spatial.getUserData(UserData.ENTITY_ID),
                    RemovalReasons.EXPIRED);
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public RigidBodyControl getRigidBodyControl() {
        return rigidBodyControl;
    }

    public static void setWorld(World world) {
        CProjectile.world = world;
    }

    public void setRange(float range) {
        this.range = range;
    }

    public CInfluenceInterface getOwnerInterface() {
        return ownerInterface;
    }

    public void setOwnerInterface(CInfluenceInterface ownerInterface) {
        this.ownerInterface = ownerInterface;
    }

    public void setSplashAction(ASplash splashAction) {
        this.splashAction = splashAction;
    }

    @Override
    public StateData getSyncableData(StateData stateData) {
        if (needsSync) {
            needsSync = true;
            return new ProjectileSyncData(
                    (int) getSpatial().getUserData(UserData.ENTITY_ID),
                    rigidBodyControl);
        }

        return null;
    }

    public ASplash getSplashAction() {
        return splashAction;
    }

    public boolean isProjectile() {
        return isProjectile;
    }

    public void setIsProjectile(boolean isProjectile) {
        this.isProjectile = isProjectile;
    }

    public Set<Spatial> getHurted() {
        return hurtList;
    }
}
