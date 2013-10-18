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

import arkhados.actions.EntityAction;
import arkhados.actions.castspellactions.MeleeAttackAction;
import arkhados.controls.ActionQueueControl;
import arkhados.controls.CharacterAnimationControl;
import arkhados.controls.CharacterPhysicsControl;
import arkhados.controls.InfluenceInterfaceControl;
import arkhados.spell.CastSpellActionBuilder;
import arkhados.spell.Spell;
import arkhados.spell.buffs.AbstractBuff;
import arkhados.util.UserDataStrings;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

/**
 *
 * @author william
 */
public class DeepWounds extends Spell {

    public DeepWounds(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static DeepWounds create() {
        final float cooldown = 8f;
        final float range = 10f;
        final float castTime = 0.3f;

        final DeepWounds spell = new DeepWounds("Deep Wounds", cooldown, range, castTime);

        spell.castSpellActionBuilder = new CastSpellActionBuilder() {
            public EntityAction newAction(Vector3f vec) {
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
    }

    @Override
    public boolean update(float tpf) {
        CharacterPhysicsControl physics = super.spatial.getControl(CharacterPhysicsControl.class);
        ActionQueueControl actionQueue = super.spatial.getControl(ActionQueueControl.class);
        actionQueue.enqueueAction(new ChargeAction(this.spell));

        // Set animation for attack
        // CharacterAnimationControl animControl =  super.spatial.getControl(CharacterAnimationControl.class);
        MeleeAttackAction meleeAction = new MeleeAttackAction(100f, 15f);
        meleeAction.addBuff(new BleedBuff(-1, 3f));
        actionQueue.enqueueAction(meleeAction);
        physics.restoreWalking();
        return false;
    }
}

class ChargeAction extends EntityAction {

    private boolean isCharging = false;
    private final float chargeSpeed = 205f;
    private float distanceMoved = 0f;
    private final float range;
    private Vector3f direction;

    public ChargeAction(final DeepWounds spell) {
        this.range = spell.getRange();
    }

    @Override
    public boolean update(float tpf) {
        CharacterPhysicsControl physics = super.spatial.getControl(CharacterPhysicsControl.class);
        InfluenceInterfaceControl influenceInterface = super.spatial.getControl(InfluenceInterfaceControl.class);
        influenceInterface.setCanControlMovement(false);
        if (!this.isCharging) {
            super.spatial.setUserData(UserDataStrings.SPEED_MOVEMENT, this.chargeSpeed);
            this.direction = physics.getTargetLocation().subtract(super.spatial.getLocalTranslation()).normalizeLocal();
            direction.multLocal(this.chargeSpeed);
            physics.setWalkDirection(direction);
            this.isCharging = true;
            influenceInterface.setConstantSpeed(true);
            return true;
        }
        physics.setWalkDirection(direction);
        this.distanceMoved += this.chargeSpeed * tpf;
        if (this.distanceMoved >= this.range) {
            return false;
        }

        return true;
    }

    @Override
    public void end() {
        super.end();
        InfluenceInterfaceControl influenceInterface = super.spatial.getControl(InfluenceInterfaceControl.class);
        influenceInterface.setCanControlMovement(true);
        influenceInterface.setConstantSpeed(false);
        CharacterPhysicsControl physics = super.spatial.getControl(CharacterPhysicsControl.class);
        Float baseMs = super.spatial.getUserData(UserDataStrings.SPEED_MOVEMENT_BASE);
        super.spatial.setUserData(UserDataStrings.SPEED_MOVEMENT, baseMs);
        physics.setWalkDirection(Vector3f.ZERO);
    }
}

class BleedBuff extends AbstractBuff {

    private CharacterPhysicsControl physics = null;
    private Spatial spatial = null;
    private final static float dmgPerUnit = 2f;

    public BleedBuff(long buffGroupId, float duration) {
        super(buffGroupId, duration);
    }

    @Override
    public void attachToCharacter(InfluenceInterfaceControl influenceInterface) {
        super.attachToCharacter(influenceInterface);
        this.spatial = influenceInterface.getSpatial();
        this.physics = this.spatial.getControl(CharacterPhysicsControl.class);
    }

    @Override
    public void update(float time) {
        super.update(time);
        if (this.physics.getWalkDirection().equals(Vector3f.ZERO)) {
            return;
        }
        Float dmg = ((Float)this.spatial.getUserData(UserDataStrings.SPEED_MOVEMENT)) * time * dmgPerUnit;
        this.influenceInterface.doDamage(dmg);
    }
}
