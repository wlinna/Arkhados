/*    This file is part of <project>.

 <project> is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 <project> is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with <project>.  If not, see <http://www.gnu.org/licenses/>. */
package arkhados.actions.castspellactions;

import arkhados.WorldManager;
import arkhados.actions.EntityAction;
import arkhados.controls.CharacterPhysicsControl;
import arkhados.spell.Spell;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

/**
 *
 * @author william
 */
public class CastOnGroundAction extends EntityAction {

    private WorldManager worldManager;
    private Spell spell;

    public CastOnGroundAction(WorldManager worldManager, Spell spell) {
        this.worldManager = worldManager;
        this.spell = spell;
    }

    @Override
    public boolean update(float tpf) {
        Vector3f targetLocation = super.spatial.getControl(CharacterPhysicsControl.class).getTargetLocation();
        worldManager.addNewEntity(spell.getName(), targetLocation.setY(0.1f), Quaternion.IDENTITY);
        return false;
    }
}
