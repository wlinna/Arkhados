/*
 * Copyright (c) 2009-2011 William Linna
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'Arkhados' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package arkhados.controls;

import arkhados.ClientSettings;
import com.jme3.input.InputManager;
import com.jme3.math.Plane;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

public class CFreeCamera extends AbstractControl {

    private Spatial spatialToFollow;
    private final Camera cam;
    private final InputManager inputManager;
    private final Vector3f intersectionPoint = new Vector3f();
    private final Plane floorPlane = new Plane(Vector3f.UNIT_Y, 0f);
    private Vector3f relativePosition;
    private final Vector3f targetDestination = new Vector3f();
    private final float timeToReach = 20f;
    private final float timeMoved = timeToReach;

    public CFreeCamera(Camera cam, InputManager inputManager) {
        this.cam = cam;
        this.inputManager = inputManager;
    }

    @Override
    protected void controlUpdate(float tpf) {
        if (spatialToFollow == null) {
            return;
        }

        calculateMouseLocation();
        Vector3f midPoint = intersectionPoint
                .subtract(spatialToFollow.getLocalTranslation())
                .multLocal(0.5f);
        midPoint.addLocal(relativePosition);
        midPoint.addLocal(spatialToFollow.getLocalTranslation());
        targetDestination.set(midPoint);

        float distance = cam.getLocation().distance(targetDestination);

        float factor = tpf * ClientSettings.getFreeCameraSpeed() / distance;
        if (factor > 1f) {
            factor = 1f;
        }

        cam.setLocation(cam.getLocation()
                .interpolateLocal(targetDestination, factor));
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    private void calculateMouseLocation() {
        Vector2f mouse2dPosition = inputManager.getCursorPosition();
        Vector3f mouse3dPosition =
                cam.getWorldCoordinates(mouse2dPosition, 0.0f);

        Vector3f rayDirection = cam.getWorldCoordinates(mouse2dPosition, 1.0f)
                .subtractLocal(mouse3dPosition).normalizeLocal();

        Ray ray = new Ray(mouse3dPosition, rayDirection);
        ray.intersectsWherePlane(floorPlane, intersectionPoint);
    }

    public void setRelativePosition(Vector3f relativePosition) {
        this.relativePosition = relativePosition;
    }

    public void setCharacter(Spatial spatial) {
        this.spatialToFollow = spatial;
    }
}