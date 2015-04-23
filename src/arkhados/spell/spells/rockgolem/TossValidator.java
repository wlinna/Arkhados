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

import arkhados.controls.CSpellCast;
import arkhados.spell.Spell;
import arkhados.spell.SpellCastValidator;
import com.jme3.scene.Spatial;


public class TossValidator implements SpellCastValidator {
    private Spell toss;

    public TossValidator(Spell toss) {
        this.toss = toss;
    }

    @Override
    public boolean validateSpellCast(CSpellCast castControl, Spell spell) {
        if (spell != toss) {
            return true;
        }
        
        Spatial my = castControl.getSpatial();        
        Spatial closest = TossSelect.select(my);
                
        return closest != null;
    }

}
