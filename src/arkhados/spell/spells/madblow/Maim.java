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
package arkhados.spell.spells.madblow;

import arkhados.actions.cast.AMeleeAttack;
import arkhados.characters.Venator;
import arkhados.spell.Spell;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

public class Maim extends Spell {

    {
        iconName = "rend.png";
    }
    public Maim(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Spell create() {
        final float cooldown = 0.8f;
        final float range = 13.5f;
        final float castTime = 0.4f;

        final Maim spell = new Maim("Maim", cooldown, range, castTime);
        spell.setCanMoveWhileCasting(true);

        spell.castSpellActionBuilder = (Node caster, Vector3f vec) -> {            
            AMeleeAttack melee = new AMeleeAttack(160f, range);
            melee.setTypeId(Venator.ANIM_SWIPE_LEFT);
            return melee;
        };

        spell.nodeBuilder = null;
        return spell;
    }
}