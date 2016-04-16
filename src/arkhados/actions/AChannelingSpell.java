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

import arkhados.controls.CActionQueue;
import arkhados.controls.CCharacterMovement;
import arkhados.controls.CInfluenceInterface;
import arkhados.controls.CSpellCast;
import arkhados.spell.Spell;
import com.jme3.scene.Spatial;

public class AChannelingSpell extends EntityAction {

    private final Spell spell;
    private float timer = 0;
    private float timeLeft;
    private int repeatsLeft;
    private final float actionFrequency;
    private final EntityAction action;
    private CCharacterMovement cMovement;
    private CInfluenceInterface inluenceControl;
    private CActionQueue actionQueue;
    private CSpellCast cSpellCast;

    public AChannelingSpell(Spell spell, float maxTime,
            float actionFrequency, EntityAction action) {
        timeLeft = maxTime;
        this.actionFrequency = actionFrequency;
        timer = actionFrequency; // Do first action immediately
        this.action = action;
        this.spell = spell;
        repeatsLeft = 0;
    }

    public AChannelingSpell(Spell spell, int repeatCount,
            float actionFrequency, EntityAction action, boolean whatever) {
        this.spell = spell;
        repeatsLeft = repeatCount;
        this.actionFrequency = actionFrequency;
        this.action = action;
        timeLeft = -1000;
    }

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        action.setSpatial(spatial);
        cMovement = spatial.getControl(CCharacterMovement.class);
        inluenceControl = spatial.getControl(CInfluenceInterface.class);
        actionQueue = spatial.getControl(CActionQueue.class);
        cSpellCast = spatial.getControl(CSpellCast.class);
    }

    @Override
    public boolean update(float tpf) {
        timeLeft -= tpf;
        timer += tpf * cSpellCast.getCastSpeedFactor();
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
            cMovement.stop();
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
