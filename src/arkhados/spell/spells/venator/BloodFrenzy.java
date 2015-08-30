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

import arkhados.actions.cast.ACastSelfBuff;
import arkhados.spell.Spell;
import arkhados.spell.buffs.AbstractBuffBuilder;
import arkhados.spell.buffs.DamageBuff;
import arkhados.spell.buffs.LifeStealBuff;
import arkhados.util.BuffTypeIds;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

public class BloodFrenzy extends Spell {

    public BloodFrenzy(String name, float cooldown, float range,
            float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Spell create() {
        BloodFrenzy spell = new BloodFrenzy("Blood Frenzy",
                SurvivalInstinct.COOLDOWN, 0f, SurvivalInstinct.CAST_TIME);

        final AbstractBuffBuilder lifeStealBuff = new LifeStealBuff.MyBuilder(
                1.7f, SurvivalInstinct.DURATION);
        lifeStealBuff.setTypeId(BuffTypeIds.BLOOD_FRENZY);


        final AbstractBuffBuilder damageBuff =
                new DamageBuff.MyBuilder(SurvivalInstinct.DURATION, -0.5f);

        spell.castSpellActionBuilder = (Node caster, Vector3f vec) -> {
            ACastSelfBuff buffAction = new ACastSelfBuff();
            buffAction.addBuff(lifeStealBuff);
            buffAction.addBuff(damageBuff);
            return buffAction;
        };

        return spell;
    }
}
