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
import arkhados.actions.EntityAction;
import arkhados.actions.castspellactions.CastSelfBuffAction;
import arkhados.controls.InfluenceInterfaceControl;
import arkhados.spell.CastSpellActionBuilder;
import arkhados.spell.Spell;
import arkhados.spell.buffs.ArmorBuff;
import arkhados.util.BuffTypeIds;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 *
 * @author william
 */
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
        spell.castSpellActionBuilder = new CastSpellActionBuilder() {
            @Override
            public EntityAction newAction(Node caster, Vector3f vec) {
                CastSelfBuffAction action = new CastSelfBuffAction();
                MineralArmorBuff armor =
                        new MineralArmorBuff(200f, 0.75f, -1, 4f);
                armor.setOwnerInterface(caster
                        .getControl(InfluenceInterfaceControl.class));
                action.addBuff(armor);
                return action;
            }
        };

        spell.nodeBuilder = null;

        return spell;
    }
}

class MineralArmorBuff extends ArmorBuff {

    public MineralArmorBuff(float amount, float protectionPercent,
            int buffGroupId, float duration) {
        super(amount, protectionPercent, buffGroupId, duration);
        setTypeId(BuffTypeIds.MINERAL_ARMOR);
    }

    @Override
    public void update(float time) {
        super.update(time);

        // Healing amount is based on assumption that duration is 4s.
        CharacterInteraction.heal(getOwnerInterface(), targetInterface,
                0.2f * getAmount() * time);
    }
}