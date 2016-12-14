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
package arkhados.spell.spells.electrobot;

import arkhados.CharacterInteraction;
import arkhados.CollisionGroups;
import arkhados.Globals;
import arkhados.actions.ATrance;
import arkhados.actions.EntityAction;
import arkhados.actions.cast.ACastSelfBuff;
import arkhados.controls.CActionQueue;
import arkhados.controls.CCharacterPhysics;
import arkhados.controls.CInfluenceInterface;
import arkhados.spell.Spell;
import arkhados.spell.buffs.AbstractBuffBuilder.EndListenerBuilder.Predefined;
import arkhados.spell.buffs.SpeedBuff;
import arkhados.util.BuffTypeIds;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.control.GhostControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.ArrayList;

public class ElectroCharge extends Spell {

    {
        iconName = "ElectroCharge.png";
    }

    public ElectroCharge(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static ElectroCharge create() {
        final float cooldown = 8f;
        final float range = 0f;
        final float castTime = 0f;

        final ElectroCharge spell = new ElectroCharge("Electro Charge",
                cooldown, range, castTime);
        final SpeedBuff.MyBuilder speedBuilder
                = new SpeedBuff.MyBuilder(0.6f, 0, 2.1f);
        speedBuilder.setTypeId(BuffTypeIds.ELECTRO_CHARGE);

        spell.castSpellActionBuilder = (Node caster, Vector3f vec) -> {
            ACastSelfBuff buffAction = new ACastSelfBuff();
            buffAction.addBuff(speedBuilder);

            CCharacterPhysics cPhysics = caster
                    .getControl(CCharacterPhysics.class);

            // Add character collider
            GhostControl ghost = new GhostControl(cPhysics.getCapsuleShape());

            ghost.setCollisionGroup(CollisionGroups.NONE);
            ghost.setCollideWithGroups(CollisionGroups.CHARACTERS);
            caster.addControl(ghost);
            PhysicsSpace space = cPhysics.getPhysicsSpace();
            space.add(ghost);
            
            ElectroChargeCollisionHandler collisionHandler
                    = new ElectroChargeCollisionHandler(ghost);
            cPhysics.getPhysicsSpace().addCollisionListener(collisionHandler);

            speedBuilder.addEndListenerBuilder(new Predefined(() -> {
                end(caster, space, collisionHandler, ghost);
            }));

            return buffAction;
        };

        spell.nodeBuilder = null;
        return spell;
    }

    static void end(Spatial owner, PhysicsSpace space,
            ElectroChargeCollisionHandler collisionHandler,
            GhostControl ghost) {
        Globals.app.enqueue(() -> {
            space.removeCollisionListener(collisionHandler);
            space.remove(ghost);
            owner.removeControl(ghost);
        });
    }
}

class ElectroChargeCollisionHandler implements PhysicsCollisionListener {

    private final GhostControl ghost;
    private boolean hasCollided;

    public ElectroChargeCollisionHandler(GhostControl ghost) {
        this.ghost = ghost;
    }

    @Override
    public void collision(PhysicsCollisionEvent event) {
        // TODO: This was copied from ACharge almost exactly. It might be time
        // to write common method for this?
        if (hasCollided) {
            return;
        }
        if ((event.getObjectA() != ghost && event.getObjectB() != ghost)
                || (event.getObjectA().getUserObject()
                == event.getObjectB().getUserObject())) {
            return;
        }

        Spatial spatial = (Spatial) ghost.getUserObject();

        PhysicsCollisionObject otherObject
                = event.getObjectA().getUserObject() == spatial
                        ? event.getObjectB()
                        : event.getObjectA();

        int otherCollisionGroup = otherObject.getCollisionGroup();
        // This filters away shields
        if (otherCollisionGroup == CollisionGroups.CHARACTERS) {
            Spatial targetSpatial = (Spatial) otherObject.getUserObject();
            if (targetSpatial.getControl(CCharacterPhysics.class) == null) {
                return;
            }
        }

        Spatial collidedWith = (Spatial) otherObject.getUserObject();
        EntityAction aCurrent
                = collidedWith.getControl(CActionQueue.class).getCurrent();

        if (aCurrent instanceof ATrance) {
            ((ATrance) aCurrent).activate(spatial);
            ElectroCharge.end(spatial, ghost.getPhysicsSpace(), this, ghost);
            return;
        }

        hasCollided = true;

        Vector3f impulse = collidedWith.getLocalTranslation()
                .subtract(spatial.getLocalTranslation()).setY(0f)
                .normalizeLocal().multLocal(25000f);

        collidedWith.getControl(CCharacterPhysics.class).applyImpulse(impulse);
        CharacterInteraction.harm(spatial.getControl(CInfluenceInterface.class),
                collidedWith.getControl(CInfluenceInterface.class),
                100f, null, true);

        ElectroCharge.end(spatial, ghost.getPhysicsSpace(), this, ghost);
    }
}
