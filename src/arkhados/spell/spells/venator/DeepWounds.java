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
        super.iconName = "deep_wounds.png";
        super.setMoveTowardsTarget(true);
    }

    public DeepWounds(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static DeepWounds create() {
        final float cooldown = 8f;
        final float range = 30f;
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
        super.setTypeId(Venator.ANIM_SWIPE_UP);
    }

    @Override
    public boolean update(float tpf) {
        ActionQueueControl actionQueue = super.spatial.getControl(ActionQueueControl.class);
        ChargeAction charge = new ChargeAction(this.spell);
        actionQueue.enqueueAction(charge);

        BleedBuff bleedBuff = new BleedBuff(-1, 5f);
        bleedBuff.setOwnerInterface(super.spatial.getControl(InfluenceInterfaceControl.class));
        Float damageFactor = super.spatial.getUserData(UserDataStrings.DAMAGE_FACTOR);
        bleedBuff.setDamagePerUnit(2f * damageFactor);
        charge.addBuff(bleedBuff);
        super.spatial.getControl(UserInputControl.class).restoreWalking();
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
        this.range = spell.getRange();
    }

    public void addBuff(AbstractBuff buff) {
        this.buffs.add(buff);
    }

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        CharacterPhysicsControl physics = super.spatial.getControl(CharacterPhysicsControl.class);
        CapsuleCollisionShape shape = physics.getCapsuleShape();
        shape.setScale(new Vector3f(1.5f, 1f, 1.5f));
        ghost = new GhostControl(shape);
        ghost.setCollisionGroup(CollisionGroups.NONE);
        ghost.setCollideWithGroups(CollisionGroups.CHARACTERS | CollisionGroups.WALLS);

        this.ghostNode = new Node("Ghost Node");
        ((Node) spatial).attachChild(this.ghostNode);
        this.ghostNode.addControl(ghost);

        physics.getPhysicsSpace().add(ghost);
    }

    @Override
    public boolean update(float tpf) {
        List<PhysicsCollisionObject> collisionObjects = this.ghost.getOverlappingObjects();
        for (PhysicsCollisionObject collisionObject : collisionObjects) {
            if (collisionObject.getUserObject() instanceof Spatial) {
                Spatial target = (Spatial) collisionObject.getUserObject();
                if (target == super.spatial) {
                    continue;
                }

                if (collisionObject.getCollisionGroup() == CollisionGroups.CHARACTERS) {
                    this.collided(target);
                }
                return false;
            }
        }

        CharacterPhysicsControl physics = super.spatial.getControl(CharacterPhysicsControl.class);
        InfluenceInterfaceControl influenceInterface = super.spatial.getControl(InfluenceInterfaceControl.class);
        influenceInterface.setCanControlMovement(false);

        if (!this.isCharging) {
            this.direction = physics.getTargetLocation().subtract(super.spatial.getLocalTranslation()).normalizeLocal();
            physics.setViewDirection(this.direction);
            direction.multLocal(this.chargeSpeed);
            physics.setDictatedDirection(direction);
            this.isCharging = true;
            influenceInterface.setSpeedConstant(true);
            return true;
        }

        physics.setViewDirection(this.direction);

        this.distanceMoved += this.chargeSpeed * tpf;
        if (this.distanceMoved >= this.range) {
            return false;
        }

        return true;
    }

    private void collided(Spatial target) {
        final Float damageFactor = super.spatial.getUserData(UserDataStrings.DAMAGE_FACTOR);
        final float rawDamage = 100f * damageFactor;

        InfluenceInterfaceControl targetInfluenceControl = target.getControl(InfluenceInterfaceControl.class);
        CharacterInteraction.harm(super.spatial.getControl(InfluenceInterfaceControl.class),
                targetInfluenceControl, rawDamage, this.buffs, true);
    }

    @Override
    public void end() {
        super.end();
        InfluenceInterfaceControl influenceInterface = super.spatial.getControl(InfluenceInterfaceControl.class);
        influenceInterface.setCanControlMovement(true);
        influenceInterface.setSpeedConstant(false);
        CharacterPhysicsControl physics = super.spatial.getControl(CharacterPhysicsControl.class);

        physics.getDictatedDirection().zero();
        physics.setWalkDirection(Vector3f.ZERO);
        physics.enqueueSetLinearVelocity(Vector3f.ZERO);

        ghost.getPhysicsSpace().remove(this.ghost);
        this.ghostNode.removeFromParent();
        this.ghostNode.removeControl(this.ghost);
    }
}

class BleedBuff extends AbstractBuff {

    private CharacterPhysicsControl physics = null;
    private Spatial spatial = null;
    private float dmgPerUnit = 2f;

    {
        super.name = "Deep Wounds";
        super.setTypeId(BuffTypeIds.DEEP_WOUNDS);
    }

    public BleedBuff(int buffGroupId, float duration) {
        super(buffGroupId, duration);
    }

    @Override
    public void attachToCharacter(InfluenceInterfaceControl targetInterface) {
        super.attachToCharacter(targetInterface);
        this.spatial = targetInterface.getSpatial();
        this.physics = this.spatial.getControl(CharacterPhysicsControl.class);
    }

    @Override
    public void update(float time) {
        super.update(time);
        if (this.physics.getWalkDirection().equals(Vector3f.ZERO)) {
            return;
        }
        Float dmg = ((Float) this.spatial.getUserData(UserDataStrings.SPEED_MOVEMENT)) * time * dmgPerUnit;
        CharacterInteraction.harm(super.getOwnerInterface(), super.targetInterface, dmg, null, true);
    }

    public void setDamagePerUnit(float dmgPerUnit) {
        this.dmgPerUnit = dmgPerUnit;
    }
}
