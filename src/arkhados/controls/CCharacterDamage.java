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

import arkhados.util.UserData;
import com.jme3.math.FastMath;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

public class CCharacterDamage extends AbstractControl {

    public float doDamage(float damage, boolean canBreakCC) {
        CInfluenceInterface me =
                spatial.getControl(CInfluenceInterface.class);
        if (me.isDead()) {
            return 0f;
        }

        float healthBefore =
                spatial.getUserData(UserData.HEALTH_CURRENT);

        damage = me.mitigateDamage(damage);

        float health = FastMath.clamp(healthBefore - damage, 0, healthBefore);
        spatial.setUserData(UserData.HEALTH_CURRENT, health);

        if (health == 0.0f) {
            me.death();
        }

        if (canBreakCC) {
            me.removeDamageSensitiveBuffs();
        }

        spatial.getControl(CResting.class).stopRegen();
        spatial.getControl(CCharacterHeal.class).tookDamage();

        return healthBefore - health;
    }

    @Override
    protected void controlUpdate(float tpf) {
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }
}
