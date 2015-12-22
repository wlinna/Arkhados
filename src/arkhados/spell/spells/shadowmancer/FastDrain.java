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
package arkhados.spell.spells.shadowmancer;

import arkhados.actions.cast.ACastProjectile;
import arkhados.spell.Spell;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

public class FastDrain extends Spell {

    static final float DURATION = 5f;

    {
        iconName = "Drain.png";
    }

    public FastDrain(String name, float cooldown, float range,
            float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Spell create() {
        final float cooldown = 7f;
        final float range = 100f;
        final float castTime = 0.25f;

        final FastDrain spell = 
                new FastDrain("Fast Drain", cooldown, range, castTime);

        spell.castSpellActionBuilder = (Node caster, Vector3f vec)
                -> new ACastProjectile(spell, world);

        spell.nodeBuilder = new DrainBuilder(false);

        return spell;
    }
}
