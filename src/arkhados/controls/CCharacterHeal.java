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
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

public class CCharacterHeal extends AbstractControl {

    private static final float HEALING_CAP_CONST = 400f;
    private float recordLowHealth;

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        recordLowHealth = spatial.getUserData(UserDataStrings.HEALTH_MAX);
    }

    private float getHealingCap() {
        float max = spatial.getUserData(UserDataStrings.HEALTH_MAX);
        return FastMath.clamp(recordLowHealth + HEALING_CAP_CONST,
                HEALING_CAP_CONST, max);
    }

    public void regenerate(float healing) {
        float max = spatial.getUserData(UserDataStrings.HEALTH_MAX);
        recordLowHealth =
                FastMath.clamp(recordLowHealth + healing, recordLowHealth, max);
        heal(healing);
    }

    public void tookDamage() {
        float currentHealth = 
                spatial.getUserData(UserDataStrings.HEALTH_CURRENT);
        if (currentHealth < recordLowHealth) {
            recordLowHealth = currentHealth;
        }
    }

    public float heal(float healing) {
        CInfluenceInterface me =
                spatial.getControl(CInfluenceInterface.class);
        if (me.isDead()) {
            return 0f;
        }
        // TODO: Healing mitigation from negative buff
        float healthBefore = 
                spatial.getUserData(UserDataStrings.HEALTH_CURRENT);
        float health = FastMath.clamp(healthBefore + healing, healthBefore,
                getHealingCap());
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
