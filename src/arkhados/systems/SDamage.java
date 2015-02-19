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
import arkhados.controls.ComponentAccessor;
import arkhados.controls.InfluenceInterfaceControl;
import arkhados.util.UserDataStrings;
import com.jme3.app.state.AbstractAppState;
import com.jme3.math.FastMath;
import com.jme3.scene.Spatial;

/**
 *
 * @author william
 */
public class SDamage extends AbstractAppState {
    {
        CharacterInteraction.sDamage = this;
    }

    public float doDamage(InfluenceInterfaceControl target, float damage,
            boolean canBreakCC) {
        if (target.isDead()) {
            return 0f;
        }

        Spatial spatial = target.getSpatial();

        float healthBefore = spatial.getUserData(UserDataStrings.HEALTH_CURRENT);

        damage = target.mitigateDamage(damage);

        float health = FastMath.clamp(healthBefore - damage, 0, healthBefore);
        spatial.setUserData(UserDataStrings.HEALTH_CURRENT, health);

        if (health == 0.0f) {
            target.death();
        }

        if (canBreakCC) {
            target.removeDamageSensitiveBuffs();
        }

        // TODO: Consider if this should be done through System instead
        spatial.getControl(ComponentAccessor.class).resting.stopRegen();

        return healthBefore - health;
    }
}
