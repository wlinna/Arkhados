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
package arkhados.actions.castspellactions;

import arkhados.CharacterInteraction;
import arkhados.SpatialDistancePair;
import arkhados.actions.EntityAction;
import arkhados.controls.CCharacterPhysics;
import arkhados.controls.CInfluenceInterface;
import arkhados.spell.buffs.AbstractBuff;
import arkhados.util.Predicate;
import arkhados.util.Selector;
import arkhados.util.UserDataStrings;
import com.jme3.math.Vector3f;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author william
 */
public class MeleeAttackAction extends EntityAction {

    private List<AbstractBuff> buffs = new ArrayList<>();
    private float damage;
    private float range;

    public MeleeAttackAction(float damage, float range) {
        this.damage = damage;
        this.range = range;
    }

    public void addBuff(AbstractBuff buff) {
        buffs.add(buff);
    }

    @Override
    public boolean update(float tpf) {
        final CCharacterPhysics physicsControl = spatial
                .getControl(CCharacterPhysics.class);
        Vector3f hitDirection = physicsControl.calculateTargetDirection()
                .normalize().multLocal(range);

        final int myTeamId = spatial.getUserData(UserDataStrings.TEAM_ID);

        physicsControl.setViewDirection(hitDirection);
        
        Predicate<SpatialDistancePair> pred =
                new Predicate<SpatialDistancePair>() {
            @Override
            public boolean test(SpatialDistancePair value) {
                if (value.spatial == spatial) {
                    return false;
                }

                Integer nullableTeamId =
                        value.spatial.getUserData(UserDataStrings.TEAM_ID);
                if (nullableTeamId == null) {
                    return false;
                }

                CInfluenceInterface influenceInterface = value.spatial
                        .getControl(CInfluenceInterface.class);

                if (influenceInterface != null
                        && !nullableTeamId.equals(myTeamId)) {
                    return true;
                }

                return false;
            }
        };
        
        SpatialDistancePair closest = Selector.giveClosest(
                Selector.coneSelect(new ArrayList<SpatialDistancePair>(), pred,
                spatial.getLocalTranslation(), hitDirection, range, 30f));
        
        if (closest == null) {
            return false;
        }

        CInfluenceInterface targetInterface =
                closest.spatial.getControl(CInfluenceInterface.class);
        if (targetInterface != null) {
            final float damageFactor =
                    spatial.getUserData(UserDataStrings.DAMAGE_FACTOR);
            final float rawDamage = damage * damageFactor;
            // TODO: Calculate damage for possible Damage over Time -buffs
            CharacterInteraction.harm(
                    spatial.getControl(CInfluenceInterface.class),
                    targetInterface, rawDamage, buffs, true);
        }
        
        return false;
    }
}