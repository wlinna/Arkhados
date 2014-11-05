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
import arkhados.PlayerData;
import arkhados.SpatialDistancePair;
import arkhados.WorldManager;
import arkhados.controls.CharacterPhysicsControl;
import arkhados.controls.InfluenceInterfaceControl;
import arkhados.controls.SpellBuffControl;
import arkhados.spell.buffs.AbstractBuff;
import arkhados.util.DistanceScaling;
import arkhados.util.PlayerDataStrings;
import arkhados.util.UserDataStrings;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author william TODO: Currently SplashAction seems to be meant for negative things only. There
 * could be healing splash too or something like that.
 */
public class SplashAction extends EntityAction {

    private float radius;
    private float baseDamage;
    private Float customImpulse;
    private DistanceScaling damageDistance;
    private List<AbstractBuff> splashBuffs;
    private boolean splashBuffsOnly = false;
    private List<Spatial> excluded = new ArrayList<>();
    private Integer excludedTeam = null;
    
    private InfluenceInterfaceControl casterInterface;

    public SplashAction(float radius, float baseDamage, DistanceScaling damageDistanceScaling,
            List<AbstractBuff> splashBuffs) {
        this.radius = radius;
        this.baseDamage = baseDamage;
        this.damageDistance = damageDistanceScaling;
        this.splashBuffs = splashBuffs;

        this.customImpulse = null;
    }

    public SplashAction(float radius, float baseDamage, float impulse,
            DistanceScaling damageDistance, List<AbstractBuff> splashBuffs) {
        this.radius = radius;
        this.baseDamage = baseDamage;
        this.customImpulse = impulse;
        this.damageDistance = damageDistance;
        this.splashBuffs = splashBuffs;
    }

    @Override
    public boolean update(float tpf) {
        List<SpatialDistancePair> spatialsOnDistance =
                WorldManager.getSpatialsWithinDistance(spatial, radius);

        if (spatialsOnDistance == null) {
            return false;
        }

        for (SpatialDistancePair pair : spatialsOnDistance) {
            InfluenceInterfaceControl targetInterface =
                    pair.spatial.getControl(InfluenceInterfaceControl.class);
            if (targetInterface == null) {
                continue;
            }

            if (excluded.contains(pair.spatial)) {
                continue;
            }

            if (excludedTeam != null) {
                int teamId = pair.spatial.getUserData(UserDataStrings.TEAM_ID);
                if (teamId == excludedTeam) {
                    continue;
                }
            }

            // TODO: Determine base damage somewhere else so that we can apply damage modifier to it

            float distanceFactor = 1f - (pair.distance / radius);
            float damageDistanceFactor = 1f;

            if (damageDistance == DistanceScaling.LINEAR) {
                damageDistanceFactor = distanceFactor;
            }
            final float damage = baseDamage * damageDistanceFactor;

            List<AbstractBuff> buffsToApply;
            if (splashBuffsOnly) {
                buffsToApply = splashBuffs;
            } else {
                final SpellBuffControl buffControl = spatial.getControl(SpellBuffControl.class);
                if (buffControl != null) {
                    buffsToApply = buffControl.getBuffs();
                    if (splashBuffs != null) {
                        buffControl.getBuffs().addAll(splashBuffs);
                    }
                } else {
                    buffsToApply = splashBuffs;
                }
            }

            CharacterInteraction.harm(casterInterface, targetInterface, damage, buffsToApply, true);

            CharacterPhysicsControl physics =
                    pair.spatial.getControl(CharacterPhysicsControl.class);
            Float impulseFactor;
            if (customImpulse == null) {
                impulseFactor = spatial.getUserData(UserDataStrings.IMPULSE_FACTOR);
            } else {
                impulseFactor = customImpulse;
            }
            Vector3f impulse;

            RigidBodyControl colliderPhysics = spatial.getControl(RigidBodyControl.class);

            if (colliderPhysics != null) {
                impulse = pair.spatial.getLocalTranslation()
                        .subtract(colliderPhysics.getPhysicsLocation().setY(0)).normalizeLocal()
                        .multLocal(impulseFactor);
            } else {
                impulse = pair.spatial.getLocalTranslation().subtract(spatial.getLocalTranslation())
                        .normalizeLocal().multLocal(impulseFactor).multLocal(distanceFactor);
            }

            physics.applyImpulse(impulse);
        }

        return false;
    }

    public void setExcludedTeam(int teamId) {
        excludedTeam = teamId;
    }

    public void excludeSpatial(Spatial oneSpatial) {
        excluded.add(oneSpatial);
    }

    public InfluenceInterfaceControl getCasterInterface() {
        return casterInterface;
    }

    public void setCasterInterface(InfluenceInterfaceControl casterInterface) {
        this.casterInterface = casterInterface;
    }
}
