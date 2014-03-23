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

import arkhados.CharacterInteraction;
import arkhados.WorldManager;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
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
public class EntityVariableControl extends AbstractControl {

    private WorldManager worldManager;

    public EntityVariableControl(WorldManager worldManager) {
        this.worldManager = worldManager;        
    }

    @Override
    protected void controlUpdate(float tpf) {
        // TODO: Move this feature to other control (or rename this)
        boolean validLoc = this.worldManager.validateLocation(super.spatial.getLocalTranslation());
        if (!validLoc) {
            final InfluenceInterfaceControl influenceInterface = super.spatial.getControl(InfluenceInterfaceControl.class);
            if (influenceInterface != null) {
                CharacterInteraction.harm(null, influenceInterface, 200f * tpf, null, true);
            }
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public Control cloneForSpatial(Spatial spatial) {
        EntityVariableControl control = new EntityVariableControl(this.worldManager);
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

    public WorldManager getWorldManager() {
        return this.worldManager;
    }
}