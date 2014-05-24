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
package arkhados.actions;

import arkhados.CharacterInteraction;
import arkhados.SpatialDistancePair;
import arkhados.WorldManager;
import arkhados.controls.CharacterPhysicsControl;
import arkhados.controls.InfluenceInterfaceControl;
import arkhados.controls.SpellBuffControl;
import arkhados.spell.buffs.AbstractBuff;
import arkhados.util.DistanceScaling;
import arkhados.util.UserDataStrings;
import com.jme3.math.Vector3f;
import java.util.List;

/**
 *
 * @author william
 */
public class SplashAction extends EntityAction {

    private float radius;
    private float baseDamage;
    private Float customImpulse;
    private DistanceScaling damageDistance;
    private List<AbstractBuff> splashBuffs;
    private boolean splashBuffsOnly = false;

    public SplashAction(float radius, float baseDamage, DistanceScaling damageDistanceScaling, List<AbstractBuff> buffsToApply) {
        this.radius = radius;
        this.baseDamage = baseDamage;
        this.damageDistance = damageDistanceScaling;
        this.splashBuffs = buffsToApply;

        this.customImpulse = null;
    }

    public SplashAction(float radius, float baseDamage, float impulse, DistanceScaling damageDistance, List<AbstractBuff> splashBuffs) {
        this.radius = radius;
        this.baseDamage = baseDamage;
        this.customImpulse = impulse;
        this.damageDistance = damageDistance;
        this.splashBuffs = splashBuffs;
    }

    @Override
    public boolean update(float tpf) {
        final List<SpatialDistancePair> spatialsOnDistance = WorldManager.getSpatialsWithinDistance(super.spatial, this.radius);
        if (spatialsOnDistance == null) {
            return false;
        }

        final InfluenceInterfaceControl casterInterface = super.spatial.getControl(InfluenceInterfaceControl.class);

        for (SpatialDistancePair pair : spatialsOnDistance) {
            final InfluenceInterfaceControl targetInterface = pair.spatial.getControl(InfluenceInterfaceControl.class);
            if (targetInterface == null) {
                continue;
            }

            // TODO: Determine base damage somewhere else so that we can apply damage modifier to it

            float distanceFactor = 1f - (pair.distance / this.radius);
            float damageDistanceFactor = 1f;

            if (this.damageDistance == DistanceScaling.LINEAR) {
                damageDistanceFactor = distanceFactor;
            }
            final float damage = baseDamage * damageDistanceFactor;


            List<AbstractBuff> buffsToApply = null;
            if (this.splashBuffsOnly) {
                buffsToApply = this.splashBuffs;
            } else {
                final SpellBuffControl buffControl = super.spatial.getControl(SpellBuffControl.class);
                if (buffControl != null) {
                    buffsToApply = buffControl.getBuffs();
                    if (this.splashBuffs != null) {
                        buffControl.getBuffs().addAll(this.splashBuffs);
                    }
                }
            }

            CharacterInteraction.harm(casterInterface, targetInterface, damage, buffsToApply, true);

            final CharacterPhysicsControl physics = pair.spatial.getControl(CharacterPhysicsControl.class);
            Float impulseFactor;
            if (this.customImpulse == null) {
                impulseFactor = super.spatial.getUserData(UserDataStrings.IMPULSE_FACTOR);
            } else {
                impulseFactor = this.customImpulse;
            }
            final Vector3f impulse = pair.spatial.getLocalTranslation().subtract(super.spatial.getLocalTranslation())
                    .normalizeLocal().multLocal(impulseFactor).multLocal(distanceFactor);

            physics.applyImpulse(impulse);
        }

        return false;
    }
}
