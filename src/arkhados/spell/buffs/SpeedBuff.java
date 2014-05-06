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

import arkhados.util.UserDataStrings;

/**
 *
 * @author william
 */
public class SpeedBuff extends AbstractBuff {
    private boolean factorBased;
    private float amount;

    public SpeedBuff(boolean factorBased, float amount, long buffGroupId, float duration) {
        super(buffGroupId, duration);
        this.factorBased = factorBased;
        if (factorBased) {
            assert amount <= 1 && amount >= 0;
            this.amount = 1 + amount; // It is factor now
        } else {
            this.amount = amount;
        }        
    }

    @Override
    public void update(float time) {
        super.update(time);
        if (this.targetInterface.isSpeedConstant()) {
            return;
        }
        
        Float msCurrent = this.targetInterface.getSpatial().getUserData(UserDataStrings.SPEED_MOVEMENT);
        if (this.factorBased) {
            msCurrent *= this.amount;
        } else {
            msCurrent += this.amount;
        }
        
        this.targetInterface.getSpatial().setUserData(UserDataStrings.SPEED_MOVEMENT, msCurrent);        
    }       
}
