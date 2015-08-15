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

import arkhados.Globals;
import arkhados.World;
import arkhados.actions.EntityAction;
import arkhados.controls.CGrenade;
import arkhados.controls.CInfluenceInterface;
import arkhados.controls.CSpellCast;
import arkhados.spell.Spell;
import arkhados.util.UserData;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

public class ACastGrenade extends EntityAction {

    private Spell spell;

    public ACastGrenade(Spell spell) {
        this.spell = spell;
    }

    @Override
    public boolean update(float tpf) {
        int playerId = spatial.getUserData(UserData.PLAYER_ID);

        CSpellCast castControl =
                spatial.getControl(CSpellCast.class);
        Vector3f adjustedTarget =
                castControl.getClosestPointToTarget(spell).setY(0.1f);

        Vector3f casterLocation = spatial.getLocalTranslation();

        Vector3f xz = adjustedTarget.subtract(casterLocation).setY(0f);

        float distance = xz.length();

        World world = Globals.app.getStateManager().getState(World.class);

        int entityId = world.addNewEntity(spell.getId(),
                casterLocation, Quaternion.IDENTITY, playerId);
        Spatial entity = world.getEntity(entityId);


        CGrenade cGrenade = entity.getControl(CGrenade.class);
        
        cGrenade.setOwnerInterface(spatial
                .getControl(CInfluenceInterface.class));
        
        int teamId = spatial.getUserData(UserData.TEAM_ID);
        cGrenade.getSplashAction().setExcludedTeam(teamId);

        cGrenade.setLaunchSpeedBasedOnRange(spell.getRange());

        float speed = cGrenade.getLaunchSpeed();

        RigidBodyControl body = entity.getControl(RigidBodyControl.class);
        float gravity = -body.getGravity().y / body.getMass();

        // Do we want to check this?
        float temp = distance * gravity
                / (speed * speed);

        float angle = FastMath.HALF_PI - FastMath.asin(temp) / 2f;

        xz.normalizeLocal().multLocal(FastMath.cos(angle));

        xz.setY(FastMath.sin(angle)).multLocal(speed);

        cGrenade.setDirection(xz);

        return false;
    }
}
