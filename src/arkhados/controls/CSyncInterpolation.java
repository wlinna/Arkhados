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

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

/**
 *
 * @author william
 */
public class CSyncInterpolation extends AbstractControl {

    private Vector3f oldLocation = new Vector3f();
    private Vector3f targetLocation = new Vector3f();
//    private Vector3f velocity;
    private float timeBetween = 0f;
    private float timeSinceLast = 0f;
    private float timeInterpolated = 0f;
    private boolean ignoreNext = false;

    @Override
    protected void controlUpdate(float tpf) {
        timeSinceLast += tpf;
        timeInterpolated += tpf;
        if (timeBetween == 0f) {
            return;
        }
        float factor = FastMath.clamp(timeInterpolated / timeBetween,
                0f, 1f);
        Vector3f trans = spatial.getLocalTranslation()
                .interpolateLocal(oldLocation, targetLocation, factor);
        spatial.setLocalTranslation(trans);
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public void interpolate(Vector3f newLocation) {
        if (ignoreNext) {
            oldLocation.set(newLocation);
        } else {
            oldLocation.set(spatial.getLocalTranslation());
        }
        targetLocation.set(newLocation);

        timeBetween = timeSinceLast;
        timeSinceLast = 0f;
        timeInterpolated = 0f;

        ignoreNext = false;
    }

    public void ignoreNext() {
        ignoreNext = true;
    }
}
