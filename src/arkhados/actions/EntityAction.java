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

import com.jme3.scene.Spatial;

/**
 *
 * @author william
 */
public abstract class EntityAction {

    protected String name = null;
    protected Spatial spatial;
    /**
     *
     * @param tpf
     * @return true if action is still active and should not be removed, false if
     * action is completed and should be removed
     */
    public abstract boolean update(float tpf);
    public void setSpatial(Spatial spatial) {
        this.spatial = spatial;
    }

    /**
     * ActionQueueControl calls end method when it's removed from queue if it's active
     */
    public void end() {
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
