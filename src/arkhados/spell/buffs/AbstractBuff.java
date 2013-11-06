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
package arkhados.spell.buffs;

import arkhados.controls.InfluenceInterfaceControl;
import com.jme3.scene.Spatial;

/**
 * Base class for all buffs, negative or positive.
 *
 * @author william
 */
public abstract class AbstractBuff {

    private long buffGroupId;
    protected float duration;
    protected InfluenceInterfaceControl targetInterface = null;
    private InfluenceInterfaceControl ownerInterface = null;

    /**
     * @param buffGroupId identifies group of buffs so that they can be removed
     * with single dispel. Not used currently
     */
    public AbstractBuff(long buffGroupId, float duration) {
        this.buffGroupId = buffGroupId;
        this.duration = duration;
    }

    public void attachToCharacter(InfluenceInterfaceControl targetInterface) {
        this.targetInterface = targetInterface;
        targetInterface.addOtherBuff(this);
    }

    /**
     *
     * @return Id of buff group that buff belongs to.
     */
    public long getBuffGroupId() {
        return this.buffGroupId;
    }

    public void update(float time) {
        this.duration -= time;
    }

    /**
     * Method for checking from buff's internal state whether it should be
     * removed or not
     *
     * @return true if buff should continue. false, if it should be removed
     */
    public boolean shouldContinue() {
        if (this.duration <= 0f) {
            return false;
        }
        return true;
    }

    public InfluenceInterfaceControl getOwnerInterface() {
        return ownerInterface;
    }

    public void setOwnerInterface(InfluenceInterfaceControl ownerInterface) {
        this.ownerInterface = ownerInterface;
    }
}
