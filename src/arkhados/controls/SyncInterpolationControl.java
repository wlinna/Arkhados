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

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import java.io.IOException;

/**
 *
 * @author william
 */
public class SyncInterpolationControl extends AbstractControl {

    private Vector3f oldLocation = new Vector3f();
    private Vector3f targetLocation = new Vector3f();
//    private Vector3f velocity;
    private float timeBetween = 0f;
    private float timeSinceLast = 0f;
    private float timeInterpolated = 0f;
    private boolean ignoreNext = false;

    @Override
    protected void controlUpdate(float tpf) {
        this.timeSinceLast += tpf;
        this.timeInterpolated += tpf;
        if (this.timeBetween == 0f) {
            return;
        }
        float factor = FastMath.clamp(this.timeInterpolated / this.timeBetween, 0f, 1f);
        Vector3f trans = super.spatial.getLocalTranslation().interpolate(this.oldLocation,
                this.targetLocation, factor);
        super.spatial.setLocalTranslation(trans);
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public Control cloneForSpatial(Spatial spatial) {
        SyncInterpolationControl control = new SyncInterpolationControl();
        return control;
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule in = im.getCapsule(this);
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule out = ex.getCapsule(this);
    }

    public void interpolate(Vector3f newLocation) {
        if (this.ignoreNext) {
            this.oldLocation.set(newLocation);
        } else {
            this.oldLocation.set(super.spatial.getLocalTranslation());
        }
        this.targetLocation.set(newLocation);
//        this.velocity.set(newLocation.subtract(super.spatial.getLocalTranslation()).divide(this.timeBetween));

        this.timeBetween = this.timeSinceLast;
        this.timeSinceLast = 0f;
        this.timeInterpolated = 0f;

        this.ignoreNext = false;
    }

    public void ignoreNext() {
        this.ignoreNext = true;
    }
}
