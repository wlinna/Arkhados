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

import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

/**
 *
 * @author Teemu
 */
public class RestingControl extends AbstractControl {

    private float idleTime = 0;
    private Vector3f location = new Vector3f();

    public void stopRegen() {
        idleTime = 0;
    }

    private void regenerate(float tpf) {
        InfluenceInterfaceControl control = getSpatial().getControl(InfluenceInterfaceControl.class);
        control.heal(2f * idleTime * tpf);
    }

    @Override
    protected void controlUpdate(float tpf) {
        Vector3f newLocation = getSpatial().getLocalTranslation();
        if (newLocation.distanceSquared(location) > 0.1) {
            idleTime = 0;
        }
        idleTime += tpf;
        if (idleTime >= 3.5f) {
            regenerate(tpf);
        }
        location.set(newLocation);
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }
}