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
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

/**
 *
 * @author william
 */
public class CFollowCharacter extends AbstractControl {
    private Node character;
    private Camera cam;

    private Vector3f relativePosition;

    public CFollowCharacter(Node character, Camera cam) {
        this.character = character;
        this.cam = cam;
    }

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
    }

    @Override
    protected void controlUpdate(float tpf) {
        cam.setLocation(character.getWorldTranslation().add(relativePosition));
        cam.lookAt(character.getWorldTranslation(), Vector3f.UNIT_Y);
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {

    }

    public void setRelativePosition(Vector3f relativePosition) {
        this.relativePosition = relativePosition;
    }
}
