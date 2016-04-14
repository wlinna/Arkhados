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

import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;


public class CCircleVisibility extends AbstractControl implements CVisibility {

    private final float radius;
    private final float radiusSquared;

    public CCircleVisibility(float radius) {
        this.radius = radius;
        this.radiusSquared = radius * radius;
    }

    @Override
    protected void controlUpdate(float tpf) {
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    @Override
    public Vector3f giveClosestPoint(Vector3f lookerLocation) {
        if (lookerLocation.distanceSquared(spatial.getLocalTranslation()) <= radiusSquared) {
            return lookerLocation;
        }
        Vector3f vec = new Vector3f(lookerLocation);
        vec.subtractLocal(spatial.getLocalTranslation());
        vec.normalizeLocal().multLocal(radius);
        return vec.addLocal(spatial.getLocalTranslation());
    }
}