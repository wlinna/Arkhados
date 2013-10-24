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
package arkhados.spell.spells.embermage;

import arkhados.actions.EntityAction;
import arkhados.actions.castspellactions.CastSelfBuffAction;
import arkhados.spell.CastSpellActionBuilder;
import arkhados.spell.Spell;
import arkhados.spell.buffs.AbstractBuff;
import com.jme3.bullet.control.GhostControl;
import com.jme3.math.Vector3f;

/**
 *
 * @author william
 */
public class PurifyingFlame extends Spell {

    public PurifyingFlame(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static PurifyingFlame create() {
        final float cooldown = 12f;
        final float range = 200f;
        final float castTime = 0f;

        final PurifyingFlame spell = new PurifyingFlame("Purifying Flame", cooldown, range, castTime);

        spell.castSpellActionBuilder = new CastSpellActionBuilder() {
            public EntityAction newAction(Vector3f vec) {
                // TODO: When team play is implemented, make this targetable to
                // team mates!
                CastSelfBuffAction action = new CastSelfBuffAction();
                action.addBuff(new AbsorbingShieldBuff(-1, 3f));
                return action;
            }
        };

        return spell;
    }
}

class AbsorbingShieldBuff extends AbstractBuff {

    public AbsorbingShieldBuff(long buffGroupId, float duration) {
        super(buffGroupId, duration);
    }

    @Override
    public void update(float time) {
        super.update(time);
        super.influenceInterface.setImmuneToProjectiles(true);
    }
}