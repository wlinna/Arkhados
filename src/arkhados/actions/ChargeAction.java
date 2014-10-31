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
package arkhados.actions;

import arkhados.CharacterInteraction;
import arkhados.CollisionGroups;
import arkhados.controls.CharacterPhysicsControl;
import arkhados.controls.InfluenceInterfaceControl;
import arkhados.spell.buffs.AbstractBuff;
import arkhados.util.UserDataStrings;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author william
 */

public class ChargeAction extends EntityAction implements PhysicsCollisionListener {

    private boolean isCharging = false;
    private float chargeSpeed;
    private float distanceMoved = 0f;
    private final float range;
    private Vector3f direction;
    private GhostControl ghost;
    private Node ghostNode;
    private List<AbstractBuff> buffs = new ArrayList<>();
    private boolean hasCollided = false;
    private Spatial collidedWith = null;
    private float hitDamage;

    public ChargeAction(float range) {
        this.range = range;
    }

    public void addBuff(AbstractBuff buff) {
        buffs.add(buff);
    }

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        CharacterPhysicsControl physics = spatial.getControl(CharacterPhysicsControl.class);
        CapsuleCollisionShape shape = physics.getCapsuleShape();
        shape.setScale(new Vector3f(1.5f, 1f, 1.5f));
        ghost = new GhostControl(shape);
        ghost.setCollisionGroup(CollisionGroups.NONE);
        ghost.setCollideWithGroups(CollisionGroups.CHARACTERS | CollisionGroups.WALLS);

        ghostNode = new Node("Ghost Node");
        ((Node) spatial).attachChild(ghostNode);
        ghostNode.addControl(ghost);

        physics.getPhysicsSpace().add(ghost);
        physics.getPhysicsSpace().addCollisionListener(this);
    }

    @Override
    public boolean update(float tpf) {
        if (hasCollided) {
            if (collidedWith != null) {
                collided(collidedWith);
            }
            return false;
        }

        CharacterPhysicsControl physics = spatial.getControl(CharacterPhysicsControl.class);
        InfluenceInterfaceControl influenceInterface =
                spatial.getControl(InfluenceInterfaceControl.class);
        influenceInterface.setCanControlMovement(false);

        if (!isCharging) {
            direction = physics.getTargetLocation().subtract(spatial.getLocalTranslation())
                    .normalizeLocal();
            physics.setViewDirection(direction);
            direction.multLocal(chargeSpeed);
            physics.setDictatedDirection(direction);
            isCharging = true;
            influenceInterface.setSpeedConstant(true);
            return true;
        }

        physics.setViewDirection(direction);

        distanceMoved += chargeSpeed * tpf;
        if (distanceMoved >= range) {
            return false;
        }

        return true;
    }

    private void collided(Spatial target) {
        float damageFactor = spatial.getUserData(UserDataStrings.DAMAGE_FACTOR);
        float rawDamage = hitDamage * damageFactor;

        InfluenceInterfaceControl targetInfluenceControl =
                target.getControl(InfluenceInterfaceControl.class);
        CharacterInteraction.harm(spatial.getControl(InfluenceInterfaceControl.class),
                targetInfluenceControl, rawDamage, buffs, true);
    }

    @Override
    public void end() {
        super.end();
        InfluenceInterfaceControl influenceInterface =
                spatial.getControl(InfluenceInterfaceControl.class);
        influenceInterface.setCanControlMovement(true);
        influenceInterface.setSpeedConstant(false);
        CharacterPhysicsControl physics = spatial.getControl(CharacterPhysicsControl.class);

        physics.getDictatedDirection().zero();
        physics.setWalkDirection(Vector3f.ZERO);
        physics.enqueueSetLinearVelocity(Vector3f.ZERO);

        ghost.getPhysicsSpace().removeCollisionListener(this);

        ghost.getPhysicsSpace().remove(ghost);
        ghostNode.removeFromParent();
        ghostNode.removeControl(ghost);
    }

    @Override
    public void collision(PhysicsCollisionEvent event) {
        if (hasCollided) {
            return;
        }
        if ((event.getObjectA() != ghost && event.getObjectB() != ghost) ||
                (event.getObjectA().getUserObject() == event.getObjectB().getUserObject())) {
            return;
        }
               
        PhysicsCollisionObject otherObject = event.getObjectA().getUserObject() == spatial
                ? event.getObjectB()
                : event.getObjectA();        

        if (otherObject.getCollisionGroup() != CollisionGroups.CHARACTERS &&
                otherObject.getCollisionGroup() != CollisionGroups.WALLS) {
            return;
        }

        hasCollided = true;

        if (otherObject.getCollisionGroup() == CollisionGroups.CHARACTERS) {
            collidedWith = (Spatial) otherObject.getUserObject();
        }
    }

    public void setChargeSpeed(float chargeSpeed) {
        this.chargeSpeed = chargeSpeed;
    }

    public void setHitDamage(float hitDamage) {
        this.hitDamage = hitDamage;
    }

    public Spatial getCollidedWith() {
        return collidedWith;
    }
}
