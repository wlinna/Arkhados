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

import com.jme3.bullet.control.RigidBodyControl;
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
import magebattle.WorldManager;

/**
 *
 * @author william
 */
public class ProjectileControl extends AbstractControl {

    private Vector3f direction = null;
    private RigidBodyControl rigidBodyControl;
    private Vector3f startingLocation = null;
    private float age = 0.0f;

    private static WorldManager worldManager;



    private static final float timeToLive = 3.0f;



    public void setTarget(Vector3f target) {
        this.direction = target.subtract(this.rigidBodyControl.getPhysicsLocation()).normalizeLocal().multLocal((Float)super.getSpatial().getUserData("speed-movement"));
        this.direction.y = 0.0f;
        this.rigidBodyControl.setLinearVelocity(this.direction);
        this.rigidBodyControl.setGravity(Vector3f.ZERO);
        if (this.startingLocation == null) {

        }
    }

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        this.rigidBodyControl = spatial.getControl(RigidBodyControl.class);
    }

    @Override
    protected void controlUpdate(float tpf) {
        if (this.direction == null) {
            return;
        }
        this.age += tpf;
        if (this.age > ProjectileControl.timeToLive) {
            ProjectileControl.worldManager.removeEntity((Long)super.spatial.getUserData("entity-id"), "expiration");
        }
        Vector3f v = this.rigidBodyControl.getLinearVelocity();
        System.out.println(String.format("%f %f %f", v.x, v.y, v.z));

    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        //Only needed for rendering-related operations,
        //not called when spatial is culled.
    }

    public Control cloneForSpatial(Spatial spatial) {
        ProjectileControl control = new ProjectileControl();
        //TODO: copy parameters to new Control
        return control;
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule in = im.getCapsule(this);
        //TODO: load properties of this Control, e.g.
        //this.value = in.readFloat("name", defaultValue);
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule out = ex.getCapsule(this);
        //TODO: save properties of this Control, e.g.
        //out.write(this.value, "name", defaultValue);
    }

    public RigidBodyControl getRigidBodyControl() {
        return this.rigidBodyControl;
    }

    public static void setWorldManager(WorldManager worldManager) {
        ProjectileControl.worldManager = worldManager;
    }
}
