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
package arkhados.spell.spells.rockgolem;

import arkhados.SpatialDistancePair;
import arkhados.controls.CCharacterPhysics;
import arkhados.controls.CInfluenceInterface;
import arkhados.util.Predicate;
import arkhados.util.Selector;
import arkhados.util.UserDataStrings;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.List;

public class TossSelect {

    public static Spatial select(Spatial my) {
        float range = Toss.PICK_RANGE;
        Predicate predicate = new TossPredicate(my);

        CCharacterPhysics physicsControl =
                my.getControl(CCharacterPhysics.class);
        Vector3f hitDirection = physicsControl.calculateTargetDirection()
                .normalize().multLocal(range);

        List<SpatialDistancePair> targets = Selector.coneSelect(
                new ArrayList<SpatialDistancePair>(), predicate,
                my.getLocalTranslation(), hitDirection, range, 90f);

        Spatial closest = null;
        float smallestDistance = 9999f;

        for (SpatialDistancePair target : targets) {
            if (target.distance < smallestDistance) {
                smallestDistance = target.distance;
                closest = target.spatial;
            }
        }

        return closest;
    }
}

class TossPredicate implements Predicate<SpatialDistancePair> {

    private Spatial spatial;

    public TossPredicate(Spatial spatial) {
        this.spatial = spatial;
    }

    @Override
    public boolean test(SpatialDistancePair value) {
        CInfluenceInterface targetInfluenceControl = value.spatial
                .getControl(CInfluenceInterface.class);
        if (targetInfluenceControl == null) {
            SpiritStonePhysicsControl stone =
                    value.spatial
                    .getControl(SpiritStonePhysicsControl.class);
            if (stone != null) {
                int myTeamId = spatial
                        .getUserData(UserDataStrings.TEAM_ID);
                if (value.spatial.getUserData(UserDataStrings.TEAM_ID)
                        .equals(myTeamId)) {
                    return true;
                }
            }
            return false;
        }

        if (value.spatial == spatial) {
            return false;
        }

        return true;
    }
}