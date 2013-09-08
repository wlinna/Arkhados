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

package magebattle.messages.usercommands;

import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;
import com.jme3.scene.Spatial;
import magebattle.controls.ActionQueueControl;
import magebattle.controls.SpellCastControl;
import magebattle.messages.syncmessages.AbstractSyncMessage;

/**
 *
 * @author william
 */
@Serializable
public class UcCastSpellMessage extends AbstractSyncMessage {
    private String spell;
    private Vector3f direction;

    public UcCastSpellMessage() {

    }

    public UcCastSpellMessage(String spell, Vector3f location) {
        this.spell = spell;
        this.direction = location;
    }

    @Override
    public void applyData(Object target) {
        Spatial character = (Spatial) target;
        character.getControl(ActionQueueControl.class).clear();
        character.getControl(SpellCastControl.class).cast(this.spell, this.direction);
    }

    public String getSpell() {
        return spell;
    }

    public Vector3f getLocation() {
        return direction;
    }
}
