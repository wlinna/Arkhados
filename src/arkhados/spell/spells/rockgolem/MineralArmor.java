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

import arkhados.CharacterInteraction;
import arkhados.actions.cast.ACastBuff;
import arkhados.spell.Spell;
import arkhados.spell.buffs.AbstractBuff;
import arkhados.spell.buffs.AbstractBuffBuilder;
import arkhados.spell.buffs.ArmorBuff;
import arkhados.util.BuffTypeIds;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

public class MineralArmor extends Spell {

    {
        iconName = "MineralArmor.png";
    }

    public MineralArmor(String name, float cooldown, float range,
            float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Spell create() {
        final float cooldown = 13f;
        final float range = 100f;
        final float castTime = 0f;

        final MineralArmor spell = new MineralArmor("MineralArmor", cooldown,
                range, castTime);
        spell.castSpellActionBuilder = (Node caster, Vector3f vec) -> {
            ACastBuff action = new ACastBuff(spell, range);
            AbstractBuffBuilder armor =
                    new MineralArmorBuff.MyBuilder(200f, 0.75f, 4f);
            action.addBuff(armor);
            
            return action;
        };

        spell.nodeBuilder = null;

        return spell;
    }
}

class MineralArmorBuff extends ArmorBuff {

    private MineralArmorBuff(float amount, float protectionPercent,
            float duration) {
        super(amount, protectionPercent, duration);
    }

    @Override
    public void update(float time) {
        super.update(time);

        // Healing amount is based on assumption that duration is 4s.
        CharacterInteraction.help(getOwnerInterface(), targetInterface,
                0.2f * getAmount() * time, null);
    }

    static class MyBuilder extends AbstractBuffBuilder {

        private final float amount;
        private final float protectionPercent;

        MyBuilder(float amount, float protectionPercent, float duration) {
            super(duration);
            setTypeId(BuffTypeIds.MINERAL_ARMOR);
            this.amount = amount;
            this.protectionPercent = protectionPercent;
        }

        @Override
        public AbstractBuff build() {
            return new MineralArmorBuff(amount, protectionPercent, duration);
        }
    }
}