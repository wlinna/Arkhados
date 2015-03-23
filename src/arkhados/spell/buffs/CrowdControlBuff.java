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

import arkhados.ServerFogManager;
import arkhados.controls.EntityVariableControl;
import arkhados.controls.InfluenceInterfaceControl;
import arkhados.messages.syncmessages.BuffCommand;
import arkhados.util.UserDataStrings;
import com.jme3.scene.Spatial;

/**
 * Base class for all buffs that somehow restricts or limits the entity that is
 * carrying the buff. Some examples of crowd control buffs are stun, silence,
 * incapacitate, slow, snare etc.
 *
 * @author william
 */
public abstract class CrowdControlBuff extends AbstractBuff {

    /**
     *
     * @param id buff group's id
     * @param duration how long buff is going to influence entity
     */
    public CrowdControlBuff(int id, float duration) {
        super(id, duration);
    }

    @Override
    public void attachToCharacter(InfluenceInterfaceControl influenceInterface) {
        targetInterface = influenceInterface;
        influenceInterface.addCrowdControlBuff(this);

        if (getTypeId() == -1) {
            return;
        }

        int entityId = targetInterface.getSpatial()
                .getUserData(UserDataStrings.ENTITY_ID);

        Spatial spatial = targetInterface.getSpatial();
        ServerFogManager fogManager = spatial
                .getControl(EntityVariableControl.class)
                .getAwareness().getFogManager();

        fogManager.addCommand(spatial, new BuffCommand(entityId,
                getTypeId(), getBuffId(), duration, true));
    }

    public boolean preventsCasting() {
        return false;
    }

    public boolean preventsMoving() {
        return false;
    }
}
