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
package arkhados.controls;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import arkhados.WorldManager;
import arkhados.util.RemovalReasons;
import arkhados.util.UserData;
import com.jme3.bullet.PhysicsSpace;

/**
 *
 * @author william
 */
public class CTimedExistence extends AbstractControl {

    private static WorldManager worldManager;
    private float timeOut;
    private float age = 0.0f;
    private boolean removeEntity;
    private PhysicsSpace space = null;

    public CTimedExistence(float timeOut) {
        this.timeOut = timeOut;
        removeEntity = false;
    }

    public CTimedExistence(float timeOut, boolean removeEntity) {
        this.timeOut = timeOut;
        this.removeEntity = removeEntity;
    }

    @Override
    protected void controlUpdate(float tpf) {
        age += tpf;
        if (age >= timeOut) {
            if (removeEntity) {
                if (worldManager.isServer()) {
                    worldManager.removeEntity((Integer) getSpatial()
                            .getUserData(UserData.ENTITY_ID),
                            RemovalReasons.EXPIRED);
                }
            } else {
                if (space != null) {
                    space.removeAll(spatial);
                }
                spatial.removeFromParent();
            }
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public static void setWorldManager(WorldManager worldManager) {
        CTimedExistence.worldManager = worldManager;
    }

    public void setSpace(PhysicsSpace space) {
        if (this.space != null) {
            this.space.removeAll(spatial);
        }
        this.space = space;
        if (space == null) {
            return;
        }
        space.addAll(spatial);

    }
}
