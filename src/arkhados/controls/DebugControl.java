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

import com.jme3.asset.AssetManager;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import com.jme3.scene.debug.Arrow;
import java.io.IOException;

/**
 *
 * @author william
 */
public class DebugControl extends AbstractControl {

    private AssetManager assetManager;

    public DebugControl(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    @Override
    protected void controlUpdate(float tpf) {
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
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

    public void drawArrow(Vector3f from, Vector3f to) {
        Vector3f direction = to.subtract(from);
        Arrow arrow = new Arrow(direction);

        arrow.setLineWidth(4f);
        Geometry g = new Geometry("arrow", arrow);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.getAdditionalRenderState().setWireframe(true);
        mat.setColor("Color", ColorRGBA.Yellow);
        g.setMaterial(mat);
        super.spatial.getParent().attachChild(g);
        g.setLocalTranslation(from);
    }

    public void pointToLocation(Vector3f location) {
        Vector3f from = location.clone().setY(2f);
        Vector3f direction = location.subtract(from);
        Arrow arrow = new Arrow(direction);

        arrow.setLineWidth(6f);
        Geometry g = new Geometry("arrow", arrow);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.getAdditionalRenderState().setWireframe(true);
        mat.setColor("Color", ColorRGBA.Blue);
        g.setMaterial(mat);
        super.spatial.getParent().attachChild(g);
        g.setLocalTranslation(from);
    }
}