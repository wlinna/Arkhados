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
import arkhados.controls.CCharacterPhysics;
import arkhados.controls.CInfluenceInterface;
import com.jme3.math.FastMath;
import com.jme3.math.Plane;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;

public class Selector {

    private static World world;

    public static <T extends Collection<SpatialDistancePair>> T coneSelect(
            T collection,
            Predicate<SpatialDistancePair> predicate,
            Vector3f location,
            Vector3f forward,
            float range,
            float coneAngle) {
        if (coneAngle > FastMath.HALF_PI) {
            throw new InvalidParameterException("coneAngle higher than half pi");
        }

        T spatialDistances = getSpatialsWithinDistance(collection,
                location, range, null);

        for (Iterator<SpatialDistancePair> it = spatialDistances.iterator();
                it.hasNext();) {
            SpatialDistancePair spatialDistancePair = it.next();

            if (!Selector.isInCone(location, forward, coneAngle, 
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
    
    public static boolean isInCone(Vector3f origin, Vector3f forward,
            float angle, Spatial spatial) {
        Vector3f dir = spatial.getLocalTranslation().subtract(origin)
                .normalizeLocal();
        return FastMath.abs(dir.angleBetween(forward)) <= FastMath.abs(angle);
    }

    public static <T extends Collection<SpatialDistancePair>> T getSpatialsWithinDistance(
            T collection,
            Spatial spatial,
            float distance,
            Predicate<Spatial> predicate) {

        return getSpatialsWithinDistance(collection,
                spatial.getWorldTranslation(), distance, predicate);
    }
    
    private static float determineRadDistanceTo(Spatial spatial, Vector3f loc) {
        CCharacterPhysics character = 
                spatial.getControl(CCharacterPhysics.class);
        if (character != null) {
            float dist = spatial.getLocalTranslation().distance(loc);
            float radius = character.getCapsuleShape().getRadius();
            return Math.max(dist - radius, 0f);
        }
        
        return spatial.getWorldBound().distanceToEdge(loc);
    }

    public static <T extends Collection<SpatialDistancePair>> T getSpatialsWithinDistance(
            T collection,
            Vector3f location,
            float distance,
            Predicate<Spatial> predicate) {
        Node worldRoot = world.getWorldRoot();

        for (Spatial child : worldRoot.getChildren()) {
            float distanceBetween = determineRadDistanceTo(child, location);       
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

    public static void setWorld(World world) {
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
