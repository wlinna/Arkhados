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
package arkhados.spell.spells.rockgolem;

import arkhados.actions.cast.ACastSelfBuff;
import arkhados.spell.Spell;
import arkhados.spell.buffs.ArmorBuff;
import arkhados.spell.buffs.SlowCC;
import arkhados.util.BuffTypeIds;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

public class Bedrock extends Spell {

    {
        iconName = "MineralArmor.png";
    }

    public Bedrock(String name, float cooldown, float range,
            float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Spell create() {
        final float cooldown = 13f;
        final float range = 0f;
        final float castTime = 0f;

        final Bedrock spell = new Bedrock("Bedrock", cooldown,
                range, castTime);

        final ArmorBuff.MyBuilder armorBuilder =
                new ArmorBuff.MyBuilder(4f, 500, 0.85f);
        armorBuilder.setTypeId(BuffTypeIds.BEDROCK);
        
        final SlowCC.MyBuilder slowBuilder = new SlowCC.MyBuilder(4f, 0.33f);
        slowBuilder.setTypeId(-1);

        spell.castSpellActionBuilder = (Node caster, Vector3f vec) -> {
            ACastSelfBuff action = new ACastSelfBuff();
            action.addBuff(armorBuilder);
            action.addBuff(slowBuilder);
            return action;
        };

        spell.nodeBuilder = null;

        return spell;
    }
}
