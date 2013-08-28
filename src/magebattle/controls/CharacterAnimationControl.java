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

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
import com.jme3.bullet.control.BetterCharacterControl;
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
import magebattle.util.UserDataStrings;

/**
 *
 * @author william
 */
public class CharacterAnimationControl extends AbstractControl {

    private AnimControl animControl;
    private CharacterPhysicsControl characterControl;
    private CharacterMovementControl movementControl;
    private AnimChannel channel;

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        this.animControl = super.spatial.getControl(AnimControl.class);
        this.characterControl = super.spatial.getControl(CharacterPhysicsControl.class);
        this.movementControl = super.spatial.getControl(CharacterMovementControl.class);

        this.channel = this.animControl.createChannel();
        this.channel.setAnim("Walk");

    }

    @Override
    protected void controlUpdate(float tpf) {
        if (!this.characterControl.getWalkDirection().equals(Vector3f.ZERO)) {
            this.channel.setSpeed(1.0f);
        } else {
            this.channel.setSpeed(0.0f);
        }
    }
    public void death() {
        this.channel.setAnim("Die");
        this.channel.setLoopMode(LoopMode.DontLoop);
        super.setEnabled(false);
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        //Only needed for rendering-related operations,
        //not called when spatial is culled.
    }

    public Control cloneForSpatial(Spatial spatial) {
        CharacterAnimationControl control = new CharacterAnimationControl();
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
