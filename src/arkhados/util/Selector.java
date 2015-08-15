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
package arkhados.util;

import arkhados.SpatialDistancePair;
import arkhados.World;
import arkhados.controls.CInfluenceInterface;
import com.jme3.math.Plane;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.Iterator;

public class Selector {

    private static World world;

    public static <T extends Collection<SpatialDistancePair>> T coneSelect(
            T collection,
            Predicate<SpatialDistancePair> predicate,
            Vector3f location,
            Vector3f forward,
            float range,
            float coneAngle) {
        if (coneAngle > 90f) {
            throw new InvalidParameterException("coneAngle higher than 90 degrees");
        }

        coneAngle = (float) Math.toRadians(coneAngle);

        Quaternion yaw = new Quaternion();
        yaw.fromAngleAxis(coneAngle, Vector3f.UNIT_Y);
        final Vector3f leftNormal = yaw.mult(forward);
        leftNormal.set(-leftNormal.z, 0, leftNormal.x);
        Plane leftPlane = new Plane(leftNormal, location.dot(leftNormal));

        yaw.fromAngleAxis(-coneAngle, Vector3f.UNIT_Y);
        final Vector3f rightNormal = yaw.mult(forward);
        rightNormal.set(rightNormal.z, 0, -rightNormal.x);
        Plane rightPlane = new Plane(rightNormal, location.dot(rightNormal));

        T spatialDistances = getSpatialsWithinDistance(collection,
                location, range, null);

        for (Iterator<SpatialDistancePair> it = spatialDistances.iterator();
                it.hasNext();) {
            SpatialDistancePair spatialDistancePair = it.next();

            if (!Selector.isInCone(leftPlane, rightPlane,
                    spatialDistancePair.spatial)) {
                it.remove();
                continue;
            }

            if (!predicate.test(spatialDistancePair)) {
                it.remove();
                continue;
            }
        }

        return collection;
    }

    public static boolean isInCone(Plane left, Plane right, Spatial spatial) {
        final Vector3f location = spatial.getLocalTranslation();
        if (left.whichSide(location) == Plane.Side.Negative) {
            return false;
        }
        if (right.whichSide(location) == Plane.Side.Negative) {
            return false;
        }
        return true;
    }

    public static <T extends Collection<SpatialDistancePair>> T getSpatialsWithinDistance(
            T collection,
            Spatial spatial,
            float distance,
            Predicate<Spatial> predicate) {

        return getSpatialsWithinDistance(collection,
                spatial.getWorldTranslation(), distance, predicate);
    }

    public static <T extends Collection<SpatialDistancePair>> T getSpatialsWithinDistance(
            T collection,
            Vector3f location,
            float distance,
            Predicate<Spatial> predicate) {
        Node worldRoot = world.getWorldRoot();

        for (Spatial child : worldRoot.getChildren()) {
            float distanceBetween = child.getWorldTranslation()
                    .distance(location);
            if (distanceBetween > distance) {
                continue;
            }

            if (predicate != null && !predicate.test(child)) {
                continue;
            }

            collection.add(new SpatialDistancePair(child, distanceBetween));
        }
        return collection;
    }

    public static <T extends Collection<SpatialDistancePair>> SpatialDistancePair giveClosest(T collection) {
        SpatialDistancePair smallest = null;
        for (SpatialDistancePair target : collection) {
            if (smallest == null) {
                smallest = target;
                continue;
            } else if (target.distance < smallest.distance) {
                smallest = target;
            }
        }

        return smallest;
    }

    public static void setWorldManager(World world) {
        Selector.world = world;
    }

    public static class IsCharacter implements Predicate<Spatial> {

        @Override
        public boolean test(Spatial value) {
            return value.getControl(CInfluenceInterface.class) != null;
        }        
    }
    
    public static class IsCharacterOfOtherTeam implements Predicate<Spatial> {

        private final int myTeam;

        public IsCharacterOfOtherTeam(int myTeam) {
            this.myTeam = myTeam;
        }

        @Override
        public boolean test(Spatial spatial) {
            if (spatial.getControl(CInfluenceInterface.class) == null) {
                return false;
            }

            return !spatial.getUserData(UserData.TEAM_ID).equals(myTeam);
        }
    }
    
    public static class IsAlliedCharacter implements Predicate<Spatial> {
        private final int myTeam;

        public IsAlliedCharacter(int myTeam) {
            this.myTeam = myTeam;
        }

        @Override
        public boolean test(Spatial value) {
            if (value.getControl(CInfluenceInterface.class) == null) {
                return false;
            }
            
            return value.getUserData(UserData.TEAM_ID).equals(myTeam);
        }        
    }
}
