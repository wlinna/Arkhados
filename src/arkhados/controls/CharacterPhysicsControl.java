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
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

/**
 *
 * @author william
 */
public class CharacterPhysicsControl extends BetterCharacterControl {

    private Vector3f impulseToApply = null;
    private Vector3f queuedLinearVelocity = null;
    private Vector3f targetLocation = new Vector3f();
    private Vector3f dictatedDirection = new Vector3f();
    private boolean motionControlled = false;

    public CharacterPhysicsControl(float radius, float height, float mass) {
        super(radius, height, mass);
    }

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        super.rigidBody.setUserObject(spatial);
        super.rigidBody.setCollisionGroup(CollisionGroups.CHARACTERS);
        super.rigidBody.setCollideWithGroups(CollisionGroups.TERRAIN |
                CollisionGroups.CHARACTERS | CollisionGroups.WALLS);
        super.rigidBody.setFriction(1f);
        super.rigidBody.setRestitution(0f);
        super.rigidBody.setGravity(Vector3f.ZERO);
    }

    public void lookAt(Vector3f targetlocation) {
        this.setViewDirection(targetLocation.subtract(super.spatial.getLocalTranslation()));
    }

    @Override
    public void prePhysicsTick(PhysicsSpace space, float tpf) {
        super.prePhysicsTick(space, tpf);

        if (impulseToApply != null) {
            this.rigidBody.applyImpulse(this.impulseToApply, Vector3f.ZERO);
            this.impulseToApply = null;
        }
        if (queuedLinearVelocity != null) {
            this.rigidBody.setLinearVelocity(this.queuedLinearVelocity);
            this.queuedLinearVelocity = null;
        }
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
    }

    public void applyImpulse(Vector3f impulse) {
        this.impulseToApply = impulse;
    }

    public void enqueueSetLinearVelocity(Vector3f velocity) {
        this.queuedLinearVelocity = velocity;
    }

    public Vector3f getTargetLocation() {
        return this.targetLocation;
    }

    public void setTargetLocation(Vector3f target) {
        this.targetLocation.set(target);
    }

    public void switchToMotionCollisionMode() {
        super.setEnabled(false);
        this.setMotionControlled(true);
    }

    public void switchToNormalPhysicsMode() {
        super.setEnabled(true);
        this.setMotionControlled(false);
    }

    public Vector3f calculateTargetDirection() {
        return this.targetLocation.subtract(super.spatial.getLocalTranslation());
    }

    public CapsuleCollisionShape getCapsuleShape() {
        CapsuleCollisionShape capsuleCollisionShape = new CapsuleCollisionShape(getFinalRadius(), (getFinalHeight() - (2 * getFinalRadius())));
        return capsuleCollisionShape;
    }

    public Vector3f getDictatedDirection() {
        return dictatedDirection;
    }

    public void setDictatedDirection(Vector3f dictatedDirection) {
        this.dictatedDirection = dictatedDirection;
        this.setWalkDirection(dictatedDirection);
    }       

    public boolean isMotionControlled() {
        return this.motionControlled;
    }

    public void setMotionControlled(boolean motionControlled) {
        this.motionControlled = motionControlled;
    }
}