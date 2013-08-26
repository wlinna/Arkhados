/*    This file is part of JMageBattle.

 JMageBattle is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 JMageBattle is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with JMageBattle.  If not, see <http://www.gnu.org/licenses/>. */
package magebattle.controls;

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.FastMath;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import java.io.IOException;
import magebattle.util.UserDataStrings;

/**
 *
 * @author william
 */
public class InfluenceInterfaceControl extends AbstractControl {

    public void doDamage(float dmg) {
        Float health = super.spatial.getUserData(UserDataStrings.HEALTH_CURRENT);
        health = FastMath.clamp(health - dmg, 0, health);
        super.spatial.setUserData(UserDataStrings.HEALTH_CURRENT, health);
        if (health == 0) {
            // TODO: Implement death
        }

    }
    @Override
    protected void controlUpdate(float tpf) {
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public Control cloneForSpatial(Spatial spatial) {
        InfluenceInterfaceControl control = new InfluenceInterfaceControl();

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
}
