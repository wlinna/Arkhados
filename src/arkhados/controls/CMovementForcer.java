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

import arkhados.CharacterInteraction;
import arkhados.CollisionGroups;
import arkhados.Globals;
import arkhados.actions.ATrance;
import arkhados.actions.EntityAction;
import arkhados.spell.buffs.AbstractBuffBuilder;
import arkhados.util.UserData;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import java.util.ArrayList;
import java.util.List;

public class CMovementForcer extends AbstractControl implements
        PhysicsCollisionListener {

    private Spatial ignored;
    private boolean isCharging = false;
    private final float chargeSpeed;
    private float distanceMoved = 0f;
    private final float range;
    private final Vector3f direction;
    private GhostControl ghost;
    private Node ghostNode;
    private final List<AbstractBuffBuilder> buffs = new ArrayList<>();
    private boolean hasCollided = false;
    private Spatial collidedWith = null;
    private float hitDamage;

    public CMovementForcer(float range, Vector3f direction) {
        this.range = range;
        this.direction = direction;
        chargeSpeed = direction.length();
    }

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        if (spatial != null) {
            start();
        }
    }

    private void start() {
        CCharacterPhysics physics = spatial.getControl(CCharacterPhysics.class);
        CapsuleCollisionShape shape = physics.getCapsuleShape();
        shape.setScale(new Vector3f(1.5f, 1f, 1.5f));
        ghost = new GhostControl(shape);
        ghost.setCollisionGroup(CollisionGroups.NONE);
        ghost.setCollideWithGroups(CollisionGroups.CHARACTERS
                | CollisionGroups.WALLS | CollisionGroups.SPIRIT_STONE);
        ghostNode = new Node("Ghost Node");
        ((Node) spatial).attachChild(ghostNode);
        ghostNode.addControl(ghost);
        ghost.setUserObject(spatial);
        physics.getPhysicsSpace().add(ghost);
        physics.getPhysicsSpace().addCollisionListener(this);
    }

    @Override
    protected void controlUpdate(float tpf) {
        if (hasCollided) {
            if (collidedWith != null) {
                collided(collidedWith);
            }
            end();
            return;
        }
        
        CCharacterPhysics physics = spatial.getControl(CCharacterPhysics.class);
        CInfluenceInterface influenceInterface
                = spatial.getControl(CInfluenceInterface.class);
        influenceInterface.setCanControlMovement(false);
        
        if (!isCharging) {
            physics.setDictatedDirection(direction);
            isCharging = true;
            influenceInterface.setSpeedConstant(true);
            return;
        }
        
        distanceMoved += chargeSpeed * tpf;
        if (distanceMoved >= range) {
            end();
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    private void collided(Spatial target) {
        int myTeam = spatial.getUserData(UserData.TEAM_ID);
        int targetTeam = target.getUserData(UserData.TEAM_ID);
        if (myTeam == targetTeam) {
            return;
        }
        EntityAction aCurrent
                = target.getControl(CActionQueue.class).getCurrent();
        if (aCurrent instanceof ATrance) {
            ((ATrance) aCurrent).activate(spatial);
            return;
        }
        float damageFactor = spatial.getUserData(UserData.DAMAGE_FACTOR);
        float rawDamage = hitDamage * damageFactor;
        CInfluenceInterface targetInfluenceControl
                = target.getControl(CInfluenceInterface.class);
        CharacterInteraction.harm(spatial.getControl(CInfluenceInterface.class),
                targetInfluenceControl, rawDamage, buffs, true);
    }

    private void end() {
        Globals.app.enqueue(() -> {
            PhysicsSpace physicsSpace = ghost.getPhysicsSpace();
            physicsSpace.removeCollisionListener(CMovementForcer.this);
            physicsSpace.remove(ghost);
            ghostNode.removeFromParent();
            ghostNode.removeControl(ghost);
            spatial.removeControl(CMovementForcer.this);
            return null;
        });
        CInfluenceInterface influenceInterface
                = spatial.getControl(CInfluenceInterface.class);
        influenceInterface.setCanControlMovement(true);
        influenceInterface.setSpeedConstant(false);
        CCharacterPhysics physics = spatial.getControl(CCharacterPhysics.class);
        physics.getDictatedDirection().zero();
        spatial.getControl(CCharacterMovement.class).stop();
        physics.enqueueSetLinearVelocity(Vector3f.ZERO);
        setEnabled(false);
    }

    @Override
    public void collision(PhysicsCollisionEvent event) {
        if (hasCollided) {
            return;
        }
        if ((event.getObjectA() != ghost && event.getObjectB() != ghost)
                || (event.getObjectA().getUserObject()
                == event.getObjectB().getUserObject())) {
            return;
        }
        PhysicsCollisionObject otherObject = event.getObjectA().getUserObject()
                == spatial ? event.getObjectB() : event.getObjectA();
        int otherCollisionGroup = otherObject.getCollisionGroup();
        if (otherCollisionGroup != CollisionGroups.CHARACTERS
                && otherCollisionGroup != CollisionGroups.WALLS
                && otherCollisionGroup != CollisionGroups.SPIRIT_STONE) {
            return;
        }
        if (otherObject.getUserObject() == ignored) {
            return;
        }
        // This filters away shields
        if (otherCollisionGroup == CollisionGroups.CHARACTERS) {
            Spatial targetSpatial = (Spatial) otherObject.getUserObject();
            if (targetSpatial.getControl(CCharacterPhysics.class) == null) {
                return;
            }
        }
        hasCollided = true;
        if (otherObject.getCollisionGroup() == CollisionGroups.CHARACTERS) {
            collidedWith = (Spatial) otherObject.getUserObject();
        }
    }

    public void setIgnored(Spatial ignored) {
        this.ignored = ignored;
    }
}