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
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.util.TempVars;

/**
 *
 * @author william
 */
public class CCharacterPhysics extends BetterCharacterControl {

    private static final float BASE_DAMPING = 1 / 30f;
    private Vector3f impulseToApply = null;
    private Vector3f queuedLinearVelocity = null;
    private Vector3f targetLocation = new Vector3f();
    private Vector3f dictatedDirection = new Vector3f();
    private boolean motionControlled = false;
    private int defaultCollideWithGroups;

    public CCharacterPhysics(float radius, float height, float mass) {
        super(radius, height, mass);
    }

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        rigidBody.setUserObject(spatial);
        rigidBody.setCollisionGroup(CollisionGroups.CHARACTERS);
        rigidBody.setCollideWithGroups(CollisionGroups.TERRAIN
                | CollisionGroups.CHARACTERS | CollisionGroups.WALLS);
        rigidBody.setFriction(1f);
        rigidBody.setRestitution(0f);
        rigidBody.setGravity(Vector3f.ZERO);

        defaultCollideWithGroups = rigidBody.getCollideWithGroups();
    }

    public void lookAt(Vector3f targetlocation) {
        setViewDirection(targetLocation.subtract(spatial.getLocalTranslation()));
    }

    @Override
    public void prePhysicsTick(PhysicsSpace space, float tpf) {
        // Most of this code is taken from BetterCharacterControl (BSD-licensed)

        float dampScale = space.getAccuracy() / BASE_DAMPING;

        checkOnGround();
        if (wantToUnDuck && checkCanUnDuck()) {
            setHeightPercent(1);
            wantToUnDuck = false;
            ducked = false;
        }
        TempVars vars = TempVars.get();

        // dampen existing x/z forces
        float existingLeftVelocity = velocity.dot(localLeft);
        float existingForwardVelocity = velocity.dot(localForward);
        Vector3f counter = vars.vect1;
        float damping = physicsDamping * dampScale;
        existingLeftVelocity *= damping;
        existingForwardVelocity *= damping;
        counter.set(-existingLeftVelocity, 0, -existingForwardVelocity);
        localForwardRotation.multLocal(counter);
        velocity.addLocal(counter);

        float designatedSpeed = walkDirection.length();
        if (designatedSpeed > 0) {
            Vector3f localWalkDirection = vars.vect1;
            //normalize walkdirection
            localWalkDirection.set(walkDirection).normalizeLocal();
            //check for the existing velocity in the desired direction
            float existingSpeed = velocity.dot(localWalkDirection);

            //calculate the final velocity in the desired direction

            float finalSpeed = FastMath.abs(existingSpeed) > designatedSpeed
                    ? 0 : designatedSpeed - existingSpeed;

            localWalkDirection.multLocal(finalSpeed);

            //add resulting vector to existing velocity

            velocity.addLocal(localWalkDirection);
        }
        rigidBody.setLinearVelocity(velocity);
        if (jump) {
            //TODO: precalculate jump force
            Vector3f rotatedJumpForce = vars.vect1;
            rotatedJumpForce.set(jumpForce);
            rigidBody.applyImpulse(localForwardRotation
                    .multLocal(rotatedJumpForce), Vector3f.ZERO);
            jump = false;
        }
        vars.release();

        if (impulseToApply != null) {
            rigidBody.applyImpulse(impulseToApply, Vector3f.ZERO);
            impulseToApply = null;
        }
        if (queuedLinearVelocity != null) {
            rigidBody.setLinearVelocity(queuedLinearVelocity);
            queuedLinearVelocity = null;
        }
    }

    public void makeEthereal() {
        rigidBody.setCollisionGroup(CollisionGroups.NONE);
        rigidBody.setCollideWithGroups(
                CollisionGroups.TERRAIN
                | CollisionGroups.WALLS
                | CollisionGroups.SPIRIT_STONE);
    }

    public void defaultCollisionGroups() {
        rigidBody.setCollisionGroup(CollisionGroups.CHARACTERS);
        rigidBody.setCollideWithGroups(defaultCollideWithGroups);
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
    }

    public void applyImpulse(Vector3f impulse) {
        impulseToApply = impulse;
    }

    public void enqueueSetLinearVelocity(Vector3f velocity) {
        queuedLinearVelocity = velocity;
    }

    public Vector3f getTargetLocation() {
        return targetLocation;
    }

    public void setTargetLocation(Vector3f target) {
        targetLocation.set(target);
    }

    public void switchToMotionCollisionMode() {
        setEnabled(false);
        setMotionControlled(true);
    }

    public void switchToNormalPhysicsMode() {
        setEnabled(true);
        setMotionControlled(false);
    }

    public Vector3f calculateTargetDirection() {
        return targetLocation.subtract(spatial.getLocalTranslation());
    }

    public CapsuleCollisionShape getCapsuleShape() {
        CapsuleCollisionShape capsuleCollisionShape =
                new CapsuleCollisionShape(getFinalRadius(),
                (getFinalHeight() - (2 * getFinalRadius())));
        return capsuleCollisionShape;
    }

    public Vector3f getDictatedDirection() {
        return dictatedDirection;
    }

    public void setDictatedDirection(Vector3f dictatedDirection) {
        this.dictatedDirection = dictatedDirection;
        setWalkDirection(dictatedDirection);
    }

    public boolean isMotionControlled() {
        return motionControlled;
    }

    public void setMotionControlled(boolean motionControlled) {
        this.motionControlled = motionControlled;
    }
}
