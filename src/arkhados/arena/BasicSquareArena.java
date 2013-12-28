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
package arkhados.arena;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
/**
 *
 * @author william
 */
public class BasicSquareArena extends AbstractArena {
    private float radius;

    @Override
    public void readWorld(Node worldRoot) {
        super.readWorld(worldRoot);
        Vector3f extent = ((BoundingBox) worldRoot.getWorldBound()).getExtent(new Vector3f());
        this.radius = extent.x;
    }



    @Override
    public boolean validateLocation(Vector3f location) {
        if (location.x < -radius || location.x > radius || location.z > radius || location.z < -radius) {
            return false;
        }

        return true;
    }
}
