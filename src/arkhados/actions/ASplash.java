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
import arkhados.controls.CCharacterPhysics;
import arkhados.controls.CInfluenceInterface;
import arkhados.controls.CSpellBuff;
import arkhados.spell.buffs.AbstractBuffBuilder;
import arkhados.util.DistanceScaling;
import arkhados.util.Selector;
import arkhados.util.UserData;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ASplash extends EntityAction {

    private float radius;
    private float baseDamage;
    private Float customImpulse;
    private DistanceScaling distanceScaling;
    private List<AbstractBuffBuilder> splashBuffs;
    private boolean splashBuffsOnly = false;
    private List<Spatial> excluded = new ArrayList<>();
    private Integer excludedTeam = null;
    private CInfluenceInterface casterInterface;
    private float baseHeal;

    public ASplash(float radius, float baseDamage,
            DistanceScaling distanceScaling,
            List<AbstractBuffBuilder> splashBuffs) {
        this.radius = radius;
        this.baseDamage = baseDamage;
        this.distanceScaling = distanceScaling;
        this.splashBuffs = splashBuffs;

        this.customImpulse = null;
    }

    public ASplash(float radius, float baseDamage, float impulse,
            DistanceScaling damageDistance,
            List<AbstractBuffBuilder> splashBuffs) {
        this.radius = radius;
        this.baseDamage = baseDamage;
        this.customImpulse = impulse;
        this.distanceScaling = damageDistance;
        this.splashBuffs = splashBuffs;
    }

    public ASplash(float radius) {
        this.radius = radius;
    }

    public ASplash damage(float amount) {
        baseDamage = amount;
        return this;
    }

    public ASplash heal(float amount) {
        baseHeal = amount;
        return this;
    }

    public ASplash impulse(float impulse) {
        customImpulse = impulse;
        return this;
    }

    public ASplash distanceScaling(DistanceScaling type) {
        distanceScaling = type;
        return this;
    }
    
    public ASplash addBuff(AbstractBuffBuilder buff) {
        if (splashBuffs == null) {
            splashBuffs = new ArrayList<>();
        }
        
        splashBuffs.add(buff);
        return this;
    }

    @Override
    public boolean update(float tpf) {
        Predicate<Spatial> predicate = excludedTeam == null
                ? new Selector.IsCharacter()
                : new Selector.IsCharacterOfOtherTeam(excludedTeam);

        List<SpatialDistancePair> spatialsOnDistance = Selector
                .getSpatialsWithinDistance(new ArrayList<SpatialDistancePair>(),
                spatial, radius, predicate);

        int myTeam = spatial.getUserData(UserData.TEAM_ID);

        for (SpatialDistancePair pair : spatialsOnDistance) {
            CInfluenceInterface targetInterface =
                    pair.spatial.getControl(CInfluenceInterface.class);

            List<AbstractBuffBuilder> buffsToApply;
            if (splashBuffsOnly) {
                buffsToApply = splashBuffs;
            } else {
                CSpellBuff buffControl = spatial.getControl(CSpellBuff.class);
                if (buffControl != null) {
                    buffsToApply = buffControl.getBuffs();
                    if (splashBuffs != null) {
                        buffControl.getBuffs().addAll(splashBuffs);
                    }
                } else {
                    buffsToApply = splashBuffs;
                }
            }

            if (excluded.contains(pair.spatial)) {
                continue;
            }

            if (pair.spatial.getUserData(UserData.TEAM_ID).equals(myTeam)) {
                positive(pair, targetInterface, buffsToApply);
            } else {
                negative(pair, targetInterface, buffsToApply);
            }
        }

        return false;
    }

    private void positive(SpatialDistancePair target,
            CInfluenceInterface targetInterface,
            List<AbstractBuffBuilder> buffs) {
        float distanceFactor = 1f - (target.distance / radius);
        float distanceScaler = 1f;

        if (distanceScaling == DistanceScaling.LINEAR) {
            distanceScaler = distanceFactor;
        }

        final float healing = baseHeal * distanceScaler;

        CharacterInteraction.help(casterInterface, targetInterface, healing,
                buffs);
    }

    private void negative(SpatialDistancePair pair,
            CInfluenceInterface targetInterface,
            List<AbstractBuffBuilder> buffs) {

        float distanceFactor = 1f - (pair.distance / radius);
        float distanceScaler = 1f;

        if (distanceScaling == DistanceScaling.LINEAR) {
            distanceScaler = distanceFactor;
        }

        final float damage = baseDamage * distanceScaler;

        CharacterInteraction.harm(casterInterface, targetInterface, damage,
                buffs, true);

        CCharacterPhysics physics =
                pair.spatial.getControl(CCharacterPhysics.class);

        Float impulseFactor;
        if (customImpulse == null) {
            impulseFactor =
                    spatial.getUserData(UserData.IMPULSE_FACTOR);
        } else {
            impulseFactor = customImpulse;
        }
        Vector3f impulse;

        RigidBodyControl colliderPhysics =
                spatial.getControl(RigidBodyControl.class);

        if (colliderPhysics != null && !colliderPhysics.isKinematic()) {
            impulse = pair.spatial.getLocalTranslation()
                    .subtract(colliderPhysics.getPhysicsLocation()).setY(0)
                    .normalizeLocal().multLocal(impulseFactor);
        } else {
            Vector3f from = new Vector3f(spatial.getLocalTranslation());
            impulse = pair.spatial.getLocalTranslation().subtract(from)
                    .setY(0).normalizeLocal().multLocal(impulseFactor)
                    .multLocal(distanceFactor);
        }

        physics.applyImpulse(impulse);
    }

    public void setExcludedTeam(int teamId) {
        excludedTeam = teamId;
    }

    public void excludeSpatial(Spatial oneSpatial) {
        excluded.add(oneSpatial);
    }

    public CInfluenceInterface getCasterInterface() {
        return casterInterface;
    }

    public void setCasterInterface(CInfluenceInterface casterInterface) {
        this.casterInterface = casterInterface;
    }
}
