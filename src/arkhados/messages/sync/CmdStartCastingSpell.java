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

package arkhados.messages.sync;

import arkhados.controls.CActionPlayer;
import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;
import com.jme3.scene.Spatial;
import arkhados.controls.CCharacterAnimation;
import arkhados.controls.CCharacterPhysics;
import arkhados.controls.CCharacterSound;
import arkhados.controls.CSpellCast;
import arkhados.messages.sync.statedata.StateData;
import arkhados.spell.Spell;

@Serializable
public class CmdStartCastingSpell extends StateData {
    private short spellId;
    private Vector3f direction = new Vector3f();
    private float castSpeedFactor;

    public CmdStartCastingSpell() {
    }

    public CmdStartCastingSpell(int id, int spellId, Vector3f direction,
            float castSpeedFactor) {
        super(id);
        this.spellId = (short)spellId;
        this.direction.set(direction);
        this.castSpeedFactor = castSpeedFactor;
    }

    @Override
    public void applyData(Object target) {
        Spatial character = (Spatial) target;
        Spell spell = character.getControl(CSpellCast.class).getSpell(spellId);
        character.getControl(CCharacterAnimation.class)
                .castSpell(spell, castSpeedFactor);
        CActionPlayer cAction = character.getControl(CActionPlayer.class);
        if (cAction != null) {
            cAction.playCastEffect(spell.getId());
        }
        character.getControl(CCharacterPhysics.class)
                .setViewDirection(direction);
        
        character.getControl(CCharacterSound.class).castSound(spellId);
    }
}
