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

import arkhados.Globals;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

public class PathCheck {

    private static final Vector3f _tempVec = new Vector3f();

    public static Vector3f closestNonColliding(Spatial walls, Vector3f start,
            Vector3f target, float room) {
        _tempVec.set(target).setY(1000f);

        Ray down = new Ray(_tempVec, Globals.DOWN);
        CollisionResults insideCheck = new CollisionResults();
        int collisionAmount = walls.collideWith(down, insideCheck);

        if (collisionAmount == 0) {
            return target;
        }

        Vector3f dir = target.subtract(start).normalizeLocal();

        Ray fromChar = new Ray(start, dir);

        CollisionResults collisionResults = new CollisionResults();
        insideCheck.getCollision(0).getGeometry()
                .collideWith(fromChar, collisionResults);

        CollisionResult collision = collisionResults.getClosestCollision();

        return collision == null ? target
                : collision.getContactPoint()
                .add(dir.negateLocal().multLocal(room));
    }
}
