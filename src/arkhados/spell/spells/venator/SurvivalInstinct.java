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
import arkhados.actions.castspellactions.CastSelfBuffAction;
import arkhados.controls.InfluenceInterfaceControl;
import arkhados.spell.CastSpellActionBuilder;
import arkhados.spell.Spell;
import arkhados.spell.buffs.AbstractBuff;
import arkhados.util.UserDataStrings;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 *
 * @author william
 */
public class SurvivalInstinct extends Spell {

    public SurvivalInstinct(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static SurvivalInstinct create() {
        final float cooldown = 12f;
        final float range = 0f;
        final float castTime = 0f;

        final SurvivalInstinct spell = new SurvivalInstinct("Survival Instinct", cooldown, range, castTime);

        spell.castSpellActionBuilder = new CastSpellActionBuilder() {
            public EntityAction newAction(Node caster, Vector3f vec) {
                CastSelfBuffAction buffAction = new CastSelfBuffAction();
                buffAction.addBuff(new DamagePerHealthPercentBuff(-1, 5f));
                buffAction.addBuff(new MovementSpeedPerHealthMissingBuff(-1, 5f));
                return buffAction;
            }
        };

        spell.nodeBuilder = null;
        return spell;
    }
}

class DamagePerHealthPercentBuff extends AbstractBuff {

    private Spatial spatial = null;

    public DamagePerHealthPercentBuff(long buffGroupId, float duration) {
        super(buffGroupId, duration);
    }

    @Override
    public void attachToCharacter(InfluenceInterfaceControl influenceInterface) {
        super.attachToCharacter(influenceInterface);
        this.spatial = influenceInterface.getSpatial();
    }

    @Override
    public void update(float time) {
        super.update(time);
        Float healthCurrent = this.spatial.getUserData(UserDataStrings.HEALTH_CURRENT);
        Float healthMax = this.spatial.getUserData(UserDataStrings.HEALTH_MAX);
        Float damageFactor = this.spatial.getUserData(UserDataStrings.DAMAGE_FACTOR);

        float healthPercent = healthCurrent / healthMax;
        damageFactor *= 1 + healthPercent / 10f;

        this.spatial.setUserData(UserDataStrings.DAMAGE_FACTOR, damageFactor);
    }
}

class MovementSpeedPerHealthMissingBuff extends AbstractBuff {

    private Spatial spatial;
    private float originalHealth;

    public MovementSpeedPerHealthMissingBuff(long buffGroupId, float duration) {
        super(buffGroupId, duration);
    }

    @Override
    public void attachToCharacter(InfluenceInterfaceControl influenceInterface) {
        super.attachToCharacter(influenceInterface);
        this.spatial = influenceInterface.getSpatial();
        this.originalHealth = this.spatial.getUserData(UserDataStrings.HEALTH_CURRENT);
    }

    @Override
    public void update(float time) {
        super.update(time);
        if (super.influenceInterface.isSpeedConstant()) {
            return;
        }

        Float msCurrent = this.spatial.getUserData(UserDataStrings.SPEED_MOVEMENT);
        Float healthMax = this.spatial.getUserData(UserDataStrings.HEALTH_MAX);
        float inverseHealthPercent = 1f - (this.originalHealth / healthMax);
        msCurrent *= 1 + inverseHealthPercent / 10;
        msCurrent += 5f;
        this.spatial.setUserData(UserDataStrings.SPEED_MOVEMENT, msCurrent);
    }
}