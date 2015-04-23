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
import arkhados.actions.castspellactions.CastProjectileAction;
import arkhados.spell.CastSpellActionBuilder;
import arkhados.spell.Spell;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 *
 * @author william
 */
public class NumbingDagger extends Spell {
    {
        iconName = "damaging_dagger.png";
    }

    public NumbingDagger(String name, float cooldown, float range,
            float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Spell create() {
        final float cooldown = 6f;
        final float range = 100f;
        final float castTime = 0.35f;

        final NumbingDagger spell = new NumbingDagger("Numbing Dagger",
                cooldown, range, castTime);

        spell.castSpellActionBuilder = new CastSpellActionBuilder() {
            @Override
            public EntityAction newAction(Node caster, Vector3f vec) {
                return new CastProjectileAction(spell, worldManager);
            }
        };

        spell.nodeBuilder = new DaggerBuilder(false);

        return spell;
    }  
}