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

import arkhados.controls.InfluenceInterfaceControl;
import arkhados.util.BuffTypeIds;
import arkhados.util.UserDataStrings;
import com.jme3.scene.Spatial;

/**
 *
 * @author william
 */
public class SlowCC extends CrowdControlBuff {
    {
        setTypeId(BuffTypeIds.SLOW);
    }

    private float slowFactor;
    private Spatial spatial;

    public SlowCC(int id, float duration, float slow) {
        super(id, duration);
        this.slowFactor = 1f - slow;
    }

    @Override
    public void attachToCharacter(InfluenceInterfaceControl influenceInterface) {
        super.attachToCharacter(influenceInterface);
        this.spatial = influenceInterface.getSpatial();
    }



    public float getSlowFactor() {
        return this.slowFactor;
    }

    @Override
    public void update(float time) {
        super.update(time);
        if (this.targetInterface.isSpeedConstant()) {
            return;
        }
        Float msCurrent = this.spatial.getUserData(UserDataStrings.SPEED_MOVEMENT);
        msCurrent *= this.slowFactor;
        this.spatial.setUserData(UserDataStrings.SPEED_MOVEMENT, msCurrent);
    }


}
