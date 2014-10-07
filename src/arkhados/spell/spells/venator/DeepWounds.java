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
package arkhados.spell.spells.venator;

import arkhados.CharacterInteraction;
import arkhados.CollisionGroups;
import arkhados.actions.EntityAction;
import arkhados.characters.Venator;
import arkhados.controls.ActionQueueControl;
import arkhados.controls.CharacterPhysicsControl;
import arkhados.controls.InfluenceInterfaceControl;
import arkhados.controls.UserInputControl;
import arkhados.spell.CastSpellActionBuilder;
import arkhados.spell.Spell;
import arkhados.spell.buffs.AbstractBuff;
import arkhados.util.BuffTypeIds;
import arkhados.util.UserDataStrings;
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
public class DeepWounds extends Spell {

    {
        iconName = "deep_wounds.png";
        setMoveTowardsTarget(true);
    }

    public DeepWounds(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static DeepWounds create() {
        final float cooldown = 8f;
        final float range = 50f;
        final float castTime = 0.3f;

        final DeepWounds spell = new DeepWounds("Deep Wounds", cooldown, range, castTime);

        spell.castSpellActionBuilder = new CastSpellActionBuilder() {
            public EntityAction newAction(Node caster, Vector3f vec) {
                return new CastDeepWoundsAction(spell);
            }
        };

        return spell;
    }
}

class CastDeepWoundsAction extends EntityAction {

    private final DeepWounds spell;

    public CastDeepWoundsAction(DeepWounds spell) {
        this.spell = spell;
        setTypeId(Venator.ANIM_SWIPE_UP);
    }

    @Override
    public boolean update(float tpf) {
        ChargeAction charge = new ChargeAction(spell);
        spatial.getControl(ActionQueueControl.class).enqueueAction(charge);

        BleedBuff bleedBuff = new BleedBuff(-1, 5f);
        bleedBuff.setOwnerInterface(spatial.getControl(InfluenceInterfaceControl.class));
        
        Float damageFactor = spatial.getUserData(UserDataStrings.DAMAGE_FACTOR);
        bleedBuff.setDamagePerUnit(2f * damageFactor);
        charge.addBuff(bleedBuff);

        spatial.getControl(UserInputControl.class).restoreWalking();
        return false;
    }
}

class ChargeAction extends EntityAction {

    private boolean isCharging = false;
    private final float chargeSpeed = 255f;
    private float distanceMoved = 0f;
    private final float range;
    private Vector3f direction;
    private GhostControl ghost;
    private Node ghostNode;
    private List<AbstractBuff> buffs = new ArrayList<>();

    public ChargeAction(final DeepWounds spell) {
        range = spell.getRange();
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
    }

    @Override
    public boolean update(float tpf) {
        List<PhysicsCollisionObject> collisionObjects = ghost.getOverlappingObjects();
        for (PhysicsCollisionObject collisionObject : collisionObjects) {
            if (collisionObject.getUserObject() instanceof Spatial) {
                Spatial target = (Spatial) collisionObject.getUserObject();
                if (target == spatial) {
                    continue;
                }

                if (collisionObject.getCollisionGroup() == CollisionGroups.CHARACTERS) {
                    collided(target);
                }
                return false;
            }
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
        final Float damageFactor = spatial.getUserData(UserDataStrings.DAMAGE_FACTOR);
        final float rawDamage = 100f * damageFactor;

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

        ghost.getPhysicsSpace().remove(ghost);
        ghostNode.removeFromParent();
        ghostNode.removeControl(ghost);
    }
}

class BleedBuff extends AbstractBuff {

    private CharacterPhysicsControl physics = null;
    private Spatial spatial = null;
    private float dmgPerUnit = 2f;

    {
        name = "Deep Wounds";
        setTypeId(BuffTypeIds.DEEP_WOUNDS);
    }

    public BleedBuff(int buffGroupId, float duration) {
        super(buffGroupId, duration);
    }

    @Override
    public void attachToCharacter(InfluenceInterfaceControl targetInterface) {
        super.attachToCharacter(targetInterface);
        spatial = targetInterface.getSpatial();
        physics = spatial.getControl(CharacterPhysicsControl.class);
    }

    @Override
    public void update(float time) {
        super.update(time);
        if (physics.getWalkDirection().equals(Vector3f.ZERO)) {
            return;
        }

        float speedMovement = spatial.getUserData(UserDataStrings.SPEED_MOVEMENT);
        Float dmg = speedMovement * time * dmgPerUnit;
        CharacterInteraction.harm(getOwnerInterface(), targetInterface, dmg, null, true);
    }

    public void setDamagePerUnit(float dmgPerUnit) {
        this.dmgPerUnit = dmgPerUnit;
    }
}