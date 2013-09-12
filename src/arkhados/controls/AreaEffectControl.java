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

import com.bulletphysics.dynamics.RigidBody;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import arkhados.spells.influences.Influence;

/**
 *
 * @author william
 */
public class AreaEffectControl extends AbstractControl {

    private GhostControl ghostControl;

    private List<Influence> influences = new ArrayList<Influence>();

    public AreaEffectControl() {
    }

    public AreaEffectControl(GhostControl ghostControl) {
        this.ghostControl = ghostControl;
    }

    @Override
    protected void controlUpdate(float tpf) {
        List<PhysicsCollisionObject> collisionObjects =  this.ghostControl.getOverlappingObjects();

        for (PhysicsCollisionObject collisionObject : collisionObjects) {
            if (collisionObject.getUserObject() instanceof Spatial) {
                Spatial spatial = (Spatial) collisionObject.getUserObject();
                for (Influence influence : this.influences) {
                    influence.affect(spatial, tpf);
                }
            }
        }
    }

    public void addInfluence(Influence influence) {
        this.influences.add(influence);
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public Control cloneForSpatial(Spatial spatial) {
        AreaEffectControl control = new AreaEffectControl();
//        control.ghostControl = this.ghostControl.cloneForSpatial(spatial);
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
