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

import arkhados.controls.SpellCastControl;
import com.jme3.network.serializing.Serializable;
import com.jme3.scene.Spatial;

/**
 *
 * @author william
 */
@Serializable
public class SetCooldownMessage extends AbstractSyncMessage {
    private String spellName;
    private float cooldown;
    private boolean globalCooldown;

    public SetCooldownMessage() {
    }

    public SetCooldownMessage(int id, String spellName, float cooldown, boolean globalCooldown) {
        super(id);
        this.spellName = spellName;
        this.cooldown = cooldown;
        this.globalCooldown = globalCooldown;
    }

    @Override
    public void applyData(Object target) {
        final Spatial character = (Spatial) target;
        final SpellCastControl castControl = character.getControl(SpellCastControl.class);
        castControl.setCooldown(this.spellName, this.cooldown);
        if (this.globalCooldown) {
            castControl.globalCooldown();
        }
    }
}
