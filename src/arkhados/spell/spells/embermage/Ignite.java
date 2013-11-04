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

import arkhados.spell.Spell;

/**
 *
 * @author william
 */
public class Ignite extends Spell{

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

}
