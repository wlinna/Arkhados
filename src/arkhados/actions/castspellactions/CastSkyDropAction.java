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
package arkhados.actions.castspellactions;

import arkhados.WorldManager;
import arkhados.actions.EntityAction;
import arkhados.controls.CharacterPhysicsControl;
import arkhados.spell.Spell;
import arkhados.util.UserDataStrings;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

/**
 * CastSkyDropAction is proper casting action for spells that drop something
 * from sky like meteor-spells.
 *
 * @author william
 */
public class CastSkyDropAction extends EntityAction {

    private WorldManager worldManager;
    private Spell spell;
    private float height = 100f;

    public CastSkyDropAction(Spell spell, WorldManager worldManager) {
        this.spell = spell;
        this.worldManager = worldManager;

    }

    @Override
    public boolean update(float tpf) {
        CharacterPhysicsControl characterControl = super.spatial.getControl(CharacterPhysicsControl.class);
        Vector3f targetLocation = characterControl.getTargetLocation();
        Vector3f startLocation = targetLocation.add(Vector3f.UNIT_Y.mult(height));
        final Long playerId = super.spatial.getUserData(UserDataStrings.PLAYER_ID);
        long entityId = this.worldManager.addNewEntity(this.spell.getName(), startLocation, Quaternion.IDENTITY, playerId);
        Spatial skyDrop = this.worldManager.getEntity(entityId);

        // TODO: Use MotionPath instead of relying on physics

        return false;
    }
}
