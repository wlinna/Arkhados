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

import arkhados.controls.CInfluenceInterface;
import arkhados.controls.CSpellCast;
import arkhados.spell.Spell;
import arkhados.spell.buffs.AbstractBuffBuilder;
import arkhados.spell.buffs.DamageOverTimeBuff;
import arkhados.util.BuffTypeIds;
import arkhados.util.UserData;
import com.jme3.scene.Node;

/**
 *
 * @author william
 */
public class Ignite extends Spell {

    public Ignite(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Ignite create() {
        final float cooldown = 5f;
        final float range = 0f;
        final float castTime = 0f;

        final Ignite spell = new Ignite("Ignite", cooldown, range, castTime);
        return spell;
    }

    public static AbstractBuffBuilder ifNotCooldownCreateDamageOverTimeBuff(
            Node caster) {
        CSpellCast castControl = caster.getControl(CSpellCast.class);
        if (castControl.isOnCooldown("Ignite")) {
            // TODO: Check if adding null causes problems
            return null;
        }

        float damagePerSecond = 20f;
        float damageFactor = caster.getUserData(UserData.DAMAGE_FACTOR);

        damagePerSecond *= damageFactor;
        
        castControl.putOnCooldown(Spell.getSpell("Ignite").getId());

        DamageOverTimeBuff.MyBuilder dotBuff =
                new DamageOverTimeBuff.MyBuilder(4f).dps(damagePerSecond);
        dotBuff.setName("Ignite");
        dotBuff.setTypeId(BuffTypeIds.IGNITE);
        CInfluenceInterface ownerInterface =
                caster.getControl(CInfluenceInterface.class);
        dotBuff.setOwnerInterface(ownerInterface);
        
        return dotBuff;
    }
}