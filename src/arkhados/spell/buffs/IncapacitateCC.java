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
package arkhados.spell.buffs;

import arkhados.util.BuffTypeIds;

/**
 * Crow Control buff that causes entity to not be able to move or cast spells.
 * Damage from outside removes incapacitate however. Note: CC is acronym for
 * Crowd Control
 * @author william
 */
public class IncapacitateCC extends CrowdControlBuff {
    {
        name = "Incapacitate";
        setTypeId(BuffTypeIds.INCAPACITATE);
    }
    public IncapacitateCC(float duration, int id) {
        super(id, duration);
    }

    @Override
    public boolean interrupts() {
        return true;
    }

    @Override
    public boolean preventsCasting() {
        return true;
    }

    @Override
    public boolean preventsMoving() {
        return true;
    }
}
