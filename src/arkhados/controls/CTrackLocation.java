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
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

public class CTrackLocation extends AbstractControl {
    private final Vector3f relativePos = new Vector3f();
    private final Spatial target;
    private final Vector3f temp = new Vector3f();

    public CTrackLocation(Spatial target, Vector3f relativePos) {
        this.target = target;
        this.relativePos.set(relativePos);
    }

    @Override
    protected void controlUpdate(float tpf) {
        temp.set(target.getWorldTranslation());
        temp.addLocal(relativePos);
        spatial.setLocalTranslation(temp);
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }
    
}
