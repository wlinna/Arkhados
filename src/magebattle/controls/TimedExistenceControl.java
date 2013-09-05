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

import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import java.io.IOException;
import magebattle.WorldManager;
import magebattle.util.UserDataStrings;

/**
 *
 * @author william
 */
public class TimedExistenceControl extends AbstractControl {
    private static WorldManager worldManager;

    private float timeOut;
    private float age = 0.0f;
    private boolean removeEntity;


    public TimedExistenceControl(float timeOut) {
        this.timeOut = timeOut;
        this.removeEntity = false;
    }

    public TimedExistenceControl(float timeOut, boolean removeEntity) {
        this.timeOut = timeOut;
        this.removeEntity = removeEntity;
    }

    @Override
    protected void controlUpdate(float tpf) {
        this.age += tpf;
        if (this.age >= this.timeOut) {
            if (this.removeEntity) {
                if (worldManager.isServer()) {
                    worldManager.removeEntity((Long)super.getSpatial().getUserData(UserDataStrings.ENTITY_ID), "expired");
                }
            } else {
                super.spatial.removeFromParent();
            }


        }
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

    public static void setWorldManager(WorldManager worldManager) {
        TimedExistenceControl.worldManager = worldManager;
    }
}
