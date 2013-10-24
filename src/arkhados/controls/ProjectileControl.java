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
import arkhados.WorldManager;
import arkhados.util.UserDataStrings;

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
    private float range = 0f;
    private float speed = 0f;

    public void setTarget(Vector3f target) {
        this.direction = target.subtract(this.rigidBodyControl.getPhysicsLocation()).setY(0.0f)
                .normalizeLocal().multLocal((Float)super.getSpatial().getUserData(UserDataStrings.SPEED_MOVEMENT));

        this.rigidBodyControl.setLinearVelocity(this.direction);
        this.speed = this.direction.length();
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
            this.rigidBodyControl.setGravity(Vector3f.ZERO);
            return;
        }
        this.age += tpf;

        if (this.range > 0f && this.age * this.speed >= this.range) {
            ProjectileControl.worldManager.removeEntity((Long) super.spatial.getUserData(UserDataStrings.ENTITY_ID), "expiration");
        }

        if (this.age > ProjectileControl.timeToLive) {
            ProjectileControl.worldManager.removeEntity((Long) super.spatial.getUserData(UserDataStrings.ENTITY_ID), "expiration");
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public Control cloneForSpatial(Spatial spatial) {
        ProjectileControl control = new ProjectileControl();
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

    public RigidBodyControl getRigidBodyControl() {
        return this.rigidBodyControl;
    }

    public static void setWorldManager(WorldManager worldManager) {
        ProjectileControl.worldManager = worldManager;
    }

    public void setRange(float range) {
        this.range = range;
    }
}