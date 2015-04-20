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

package arkhados.spell.buffs.buffinformation;

import arkhados.effects.BuffEffect;


public class BedrockInformation extends BuffInformation {

    {
        // TODO: Find / make unique icon for Petrify
        setIconPath("Interface/Images/SpellIcons/MineralArmor.png");
    }

    @Override
    public BuffEffect createBuffEffect(BuffInfoParameters params) {
        PetrifyEffect effect = new PetrifyEffect(params.duration);
        effect.addToCharacter(params);
        return effect;
    }
}
