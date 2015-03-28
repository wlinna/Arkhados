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

package arkhados.messages.syncmessages;

import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;
import com.jme3.scene.Spatial;
import arkhados.controls.CharacterAnimationControl;
import arkhados.controls.CharacterPhysicsControl;
import arkhados.controls.CharacterSoundControl;
import arkhados.controls.SpellCastControl;
import arkhados.messages.syncmessages.statedata.StateData;
import arkhados.spell.Spell;

/**
 *
 * @author william
 */
@Serializable
public class CmdStartCastingSpell extends StateData {
    private short spellId;
    private Vector3f direction = new Vector3f();

    public CmdStartCastingSpell() {
    }

    public CmdStartCastingSpell(int id, int spellId, Vector3f direction) {
        super(id);
        this.spellId = (short)spellId;
        this.direction.set(direction);
    }

    @Override
    public void applyData(Object target) {
        Spatial character = (Spatial) target;
        Spell spell =
                character.getControl(SpellCastControl.class).getSpell(spellId);
        character.getControl(CharacterAnimationControl.class).castSpell(spell);
        character.getControl(CharacterPhysicsControl.class)
                .setViewDirection(direction);
        
        character.getControl(CharacterSoundControl.class).castSound(spellId);
    }
}
