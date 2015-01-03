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
package arkhados.actions;

import arkhados.controls.ActionQueueControl;
import arkhados.controls.CharacterPhysicsControl;
import arkhados.controls.InfluenceInterfaceControl;
import arkhados.spell.Spell;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

/**
 *
 * @author william
 */
public class ChannelingSpellAction extends EntityAction {
    private Spell spell;
    private float timer = 0;
    private float timeLeft;
    private int repeatsLeft;
    private float actionFrequency;
    private EntityAction action;
    private CharacterPhysicsControl physics;
    private InfluenceInterfaceControl inluenceControl;
    private ActionQueueControl actionQueue;

    public ChannelingSpellAction(Spell spell, float maxTime, float actionFrequency, EntityAction action) {
        timeLeft = maxTime;
        this.actionFrequency = actionFrequency;
        timer = actionFrequency; // Do first action immediately
        this.action = action;
        this.spell = spell;
        repeatsLeft = 0;
    }

    public ChannelingSpellAction(Spell spell, int repeatCount, float actionFrequency, EntityAction action, boolean whatever) {
        this.spell = spell;
        repeatsLeft = repeatCount;
        this.actionFrequency = actionFrequency;
        this.action = action;
        timeLeft = -1000;
    }

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial); //To change body of generated methods, choose Tools | Templates.
        action.setSpatial(spatial);
        physics = spatial.getControl(CharacterPhysicsControl.class);
        inluenceControl = spatial.getControl(InfluenceInterfaceControl.class);
        actionQueue = spatial.getControl(ActionQueueControl.class);
    }

    @Override
    public boolean update(float tpf) {
        timeLeft -= tpf;
        timer += tpf;
        if (timer >= actionFrequency) {
            timer = 0;
            action.update(tpf); // tpf should not matter for action
            actionQueue.simulateAction(action);
            --repeatsLeft;
        }

        if (repeatsLeft <= 0 && timeLeft <= 0) {
            return false;
        }
        if (!inluenceControl.isAbleToCastWhileMoving()) {
            physics.setWalkDirection(Vector3f.ZERO);
        }
        return true;
    }
    
    public void signalEnd() {
        repeatsLeft = 0;
        timeLeft = 0;
    }

    public Spell getSpell() {
        return spell;
    }
}
