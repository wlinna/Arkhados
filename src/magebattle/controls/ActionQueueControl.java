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
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import magebattle.actions.EntityAction;

/**
 *
 * @author william
 */
public class ActionQueueControl extends AbstractControl {
    private Queue<EntityAction> actions = new LinkedList<EntityAction>();
    private EntityAction current = null;

    public void enqueueAction(EntityAction action) {
        action.setSpatial(super.getSpatial());
        if (this.current == null) {
            this.current = action;
        }
        else {
            this.actions.add(action);
        }
    }

    public void clear() {
        if (this.current != null) {
            this.current.end();
        }
        this.current = null;
        this.actions.clear();
    }

    @Override
    protected void controlUpdate(float tpf) {
        if (this.current == null) {
            return;
        }
        boolean active = this.current.update(tpf);
        if (!active) {
            this.current.end();
            this.current = this.actions.poll();
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public Control cloneForSpatial(Spatial spatial) {
        ActionQueueControl control = new ActionQueueControl();
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

    public EntityAction getCurrent() {
        return this.current;
    }
}
