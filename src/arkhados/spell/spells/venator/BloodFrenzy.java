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
import arkhados.actions.castspellactions.ACastSelfBuff;
import arkhados.spell.CastSpellActionBuilder;
import arkhados.spell.Spell;
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
        final BloodFrenzy spell = new BloodFrenzy("Blood Frenzy",
                SurvivalInstinct.COOLDOWN, 0f, SurvivalInstinct.CAST_TIME);

        spell.castSpellActionBuilder = new CastSpellActionBuilder() {
            @Override
            public EntityAction newAction(Node caster, Vector3f vec) {
                ACastSelfBuff buffAction = new ACastSelfBuff();
                LifeStealBuff lifeStealBuff =
                        new LifeStealBuff(1f, -1, SurvivalInstinct.DURATION);
                lifeStealBuff.setTypeId(BuffTypeIds.BLOOD_FRENZY);
                DamageBuff damageBuff =
                        new DamageBuff(-1, SurvivalInstinct.DURATION, -0.5f);
                
                buffAction.addBuff(lifeStealBuff);
                buffAction.addBuff(damageBuff);
                return buffAction;
            }
        };

        return spell;
    }
}
