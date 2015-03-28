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

import arkhados.controls.CCharacterBuff;
import arkhados.effects.BuffEffect;

/**
 *
 * @author william
 */
public class LikeAProInformation extends BuffInformation {
    {
        super.setIconPath("Interface/Images/SpellIcons/like_a_pro.png");
    }

    @Override
    public BuffEffect createBuffEffect(CCharacterBuff buffControl, float duration) {
        return new BuffEffect(duration);
    }
}
