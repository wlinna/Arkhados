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
package arkhados;

import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

/**
 *
 * @author william
 */

public class ServerPlayerInputState {
    public Spatial currentActiveSpatial = null;
    public int previousRight = 0;
    public int previousDown = 0;
    public Vector3f mouseTarget = null;
}
