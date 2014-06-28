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

import arkhados.WorldManager;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import java.util.LinkedList;
import java.util.Queue;
import arkhados.actions.EntityAction;
import arkhados.messages.syncmessages.ActionCommand;
import arkhados.net.Sender;
import arkhados.util.UserDataStrings;

/**
 *
 * @author william
 */
public class ActionQueueControl extends AbstractControl {

    private Queue<EntityAction> actions = new LinkedList<>();
    private EntityAction current = null;
    private boolean shouldSimulate = false;

    public void enqueueAction(EntityAction action) {
        if (!this.enabled) {
            return;
        }
        action.setSpatial(super.getSpatial());
        if (this.current == null) {
            this.current = action;
            this.shouldSimulate = true;
        } else {
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
        if (this.shouldSimulate) {
            this.simulateAction(this.current);
            this.shouldSimulate = false;
        }
        if (!active) {
            this.current.end();
            this.current = this.actions.poll();
            this.shouldSimulate = true;
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.clear();
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public EntityAction getCurrent() {
        return this.current;
    }

    private void simulateAction(final EntityAction action) {
        if (action.getTypeId() == -1) {
            return;
        }
        CharacterAnimationControl animationControl = super.spatial.getControl(CharacterAnimationControl.class);
        if (animationControl != null) {
            animationControl.animateAction(action.getTypeId());
        }

        final Sender sender = super.spatial.getControl(EntityVariableControl.class).getSender();
        if (sender.isServer()) {
            final Integer id = super.spatial.getUserData(UserDataStrings.ENTITY_ID);
            sender.addCommand(new ActionCommand(id, action.getTypeId()));
        }
    }
}
