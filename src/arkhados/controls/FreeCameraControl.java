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

import com.jme3.collision.CollisionResults;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.input.InputManager;
import com.jme3.math.Plane;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import java.io.IOException;

/**
 *
 * @author william
 */
public class FreeCameraControl extends AbstractControl {

    private Node character;
    private Camera cam;
    private InputManager inputManager;
    private Vector3f intersectionPoint = new Vector3f();
    private Plane floorPlane = new Plane(Vector3f.UNIT_Y, 0f);
    private Vector3f relativePosition;
    private Vector3f targetDestination = new Vector3f();

    private float timeToReach = 20f;
    private float timeMoved = this.timeToReach;

    private float camSpeed = 500f;

    public FreeCameraControl(Node character, Camera cam, InputManager inputManager) {
        this.character = character;
        this.cam = cam;
        this.inputManager = inputManager;
    }

    @Override
    protected void controlUpdate(float tpf) {
//        if (this.timeMoved < this.timeToReach) {
//            this.timeMoved += tpf;
//        }
        this.calculateMouseLocation();
        Vector3f midPoint = this.intersectionPoint.subtract(character.getLocalTranslation()).multLocal(0.5f);
        midPoint.addLocal(this.relativePosition);
        midPoint.addLocal(character.getLocalTranslation());
        this.targetDestination.set(midPoint);

//        this.cam.setLocation(this.targetDestination);
//        this.cam.
        float distance = this.cam.getLocation().distance(this.targetDestination);

        float factor = tpf * this.camSpeed / distance;
        if (factor > 1f) {
            factor = 1f;
        }
        this.cam.setLocation(this.cam.getLocation().interpolate(this.targetDestination, factor));
//
//        if (this.timeMoved < this.timeToReach) {
//            this.timeMoved += tpf;
//            this.cam.setLocation(Vector3f.NAN.interpolate(this.cam.getLocation(), this.targetDestination, this.timeMoved / this.timeToReach));
//        } else {
//            this.timeMoved = 0f;
//            this.cam.setLocation(this.targetDestination);
//    }
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

    private void calculateMouseLocation() {
        final Vector2f mouse2dPosition = this.inputManager.getCursorPosition();
        final Vector3f mouse3dPosition = this.cam
                .getWorldCoordinates(mouse2dPosition, 0.0f);


        final Vector3f rayDirection = this.cam
                .getWorldCoordinates(mouse2dPosition, 1.0f)
                .subtractLocal(mouse3dPosition).normalizeLocal();


        Ray ray = new Ray(mouse3dPosition, rayDirection);
        boolean intersects = ray.intersectsWherePlane(this.floorPlane, this.intersectionPoint);
    }

    public void setRelativePosition(Vector3f relativePosition) {
        this.relativePosition = relativePosition;
    }
}
