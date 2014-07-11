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

import arkhados.ServerFogManager;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import java.util.LinkedList;
import java.util.Queue;
import arkhados.actions.EntityAction;
import arkhados.messages.syncmessages.ActionCommand;
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
        if (!enabled) {
            return;
        }
        action.setSpatial(getSpatial());
        if (current == null) {
            current = action;
            shouldSimulate = true;
        } else {
            actions.add(action);
        }
    }

    public void clear() {
        if (current != null) {
            current.end();
        }
        current = null;
        actions.clear();
    }

    @Override
    protected void controlUpdate(float tpf) {
        if (current == null) {
            return;
        }
        boolean active = current.update(tpf);
        if (shouldSimulate) {
            simulateAction(current);
            shouldSimulate = false;
        }
        if (!active) {
            current.end();
            current = actions.poll();
            shouldSimulate = true;
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        clear();
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public EntityAction getCurrent() {
        return current;
    }

    private void simulateAction(EntityAction action) {
        if (action.getTypeId() == -1) {
            return;
        }
        CharacterAnimationControl animationControl = spatial.getControl(CharacterAnimationControl.class);
        if (animationControl != null) {
            animationControl.animateAction(action.getTypeId());
        }
        PlayerEntityAwareness awareness = spatial.getControl(EntityVariableControl.class).getAwareness();
        if (awareness != null) {
            final Integer id = spatial.getUserData(UserDataStrings.ENTITY_ID);
            awareness.getFogManager().addCommand(spatial, new ActionCommand(id, action.getTypeId()));
        }
    }
}
