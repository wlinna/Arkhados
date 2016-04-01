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

import arkhados.World;
import arkhados.actions.EntityAction;
import arkhados.actions.cast.ACastProjectile;
import arkhados.controls.CInfluenceInterface;
import arkhados.spell.CastSpellActionBuilder;
import arkhados.spell.Spell;
import arkhados.spell.buffs.AbstractBuff;
import arkhados.spell.buffs.AbstractBuffBuilder;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

public class Dagger extends Spell {

    {
        iconName = "damaging_dagger.png";
    }

    public Dagger(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Spell create() {
        final float cooldown = 6f;
        final float range = 100f;
        final float castTime = 0.35f;

        final Dagger spell = new Dagger("Damaging Dagger", cooldown,
                range, castTime);

        spell.castSpellActionBuilder = new CastDagger(spell, world);
        spell.nodeBuilder = new DaggerBuilder(true);

        return spell;
    }
}

class CastDagger implements CastSpellActionBuilder {

    private final Dagger spell;
    private final World world;

    public CastDagger(Dagger spell, World world) {
        this.spell = spell;
        this.world = world;
    }

    @Override
    public EntityAction newAction(Node caster, Vector3f vec) {
        ACastProjectile action = new ACastProjectile(spell, world);        
        
        AbstractBuffBuilder trigger = Backlash.giveTriggerIfValid(caster);        
        if (trigger != null) {
            action.addBuff(trigger);
        }

        return action;
    }
}
