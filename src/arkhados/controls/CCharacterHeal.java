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
package arkhados.controls;

import arkhados.util.UserDataStrings;
import com.jme3.math.FastMath;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

public class CCharacterHeal extends AbstractControl {

    public float heal(float healing) {
        InfluenceInterfaceControl me =
                spatial.getControl(InfluenceInterfaceControl.class);
        if (me.isDead()) {
            return 0f;
        }
        // TODO: Healing mitigation from negative buff
        float maxHealth = spatial.getUserData(UserDataStrings.HEALTH_MAX);
        float healthBefore =
                spatial.getUserData(UserDataStrings.HEALTH_CURRENT);
        float health =
                FastMath.clamp(healthBefore + healing, healthBefore, maxHealth);
        spatial.setUserData(UserDataStrings.HEALTH_CURRENT, health);
        return health - healthBefore;
    }

    @Override
    protected void controlUpdate(float tpf) {
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }
}
