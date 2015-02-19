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
package arkhados.systems;

import arkhados.CharacterInteraction;
import arkhados.controls.InfluenceInterfaceControl;
import arkhados.util.UserDataStrings;
import com.jme3.app.state.AbstractAppState;
import com.jme3.math.FastMath;
import com.jme3.scene.Spatial;

/**
 *
 * @author william
 */
public class SHeal extends AbstractAppState {
    {
        CharacterInteraction.sHeal = this;
    }

    public float heal(InfluenceInterfaceControl target, float healing) {
        if (target.isDead()) {
            return 0f;
        }

        Spatial spatial = target.getSpatial();

        // TODO: Healing mitigation from negative buff
        float maxHealth = spatial.getUserData(UserDataStrings.HEALTH_MAX);
        float healthBefore =
                spatial.getUserData(UserDataStrings.HEALTH_CURRENT);
        Float health =
                FastMath.clamp(healthBefore + healing, healthBefore, maxHealth);
        spatial.setUserData(UserDataStrings.HEALTH_CURRENT, health);
        return health - healthBefore;

    }
}
