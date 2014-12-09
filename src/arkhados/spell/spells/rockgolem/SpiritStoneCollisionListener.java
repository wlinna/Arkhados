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
package arkhados.spell.spells.rockgolem;

import arkhados.CollisionGroups;
import arkhados.WorldManager;
import arkhados.util.RemovalReasons;
import arkhados.util.UserDataStrings;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 *
 * @author william
 */
public class SpiritStoneCollisionListener implements PhysicsCollisionListener {

    private Node myStone;
    private WorldManager worldManager;

    public SpiritStoneCollisionListener(Node myStone, WorldManager worldManager) {
        this.myStone = myStone;
        this.worldManager = worldManager;
    }

    @Override
    public void collision(PhysicsCollisionEvent event) {
        boolean isA = myStone == event.getNodeA();
        boolean isB = myStone == event.getNodeB();
        if (!isA && !isB) {
            return;
        }

        Spatial other = isA ? event.getNodeB() : event.getNodeA();
        PhysicsCollisionObject otherPhysics = isA ? event.getObjectB() : event.getObjectA();
        int otherCollisionGroup = otherPhysics.getCollisionGroup();

        SpiritStonePhysicsControl stonePhysics =
                myStone.getControl(SpiritStonePhysicsControl.class);
        
        int stoneId = myStone.getUserData(UserDataStrings.ENTITY_ID);

        Integer otherTeamId = other.getUserData(UserDataStrings.TEAM_ID);
        if (otherTeamId == null) {
            if (stonePhysics.isPunched()) {
                worldManager.removeEntity(stoneId, RemovalReasons.COLLISION);
            } else {
                System.out.println("Spirit stone collided with something that doesn't have team id");
            }
            return;
        }
        int myTeamId = myStone.getUserData(UserDataStrings.TEAM_ID);



        BetterCharacterControl characterControl = other.getControl(BetterCharacterControl.class);
        if (characterControl != null && stonePhysics.isPunched() && !otherTeamId.equals(myTeamId)) {
            worldManager.removeEntity(stoneId, RemovalReasons.COLLISION);
        } else if (stonePhysics.isPunched() && otherCollisionGroup == CollisionGroups.WALLS) {
            worldManager.removeEntity(stoneId, RemovalReasons.COLLISION);
        }
    }

    public void setWorldManager(WorldManager worldManager) {
        this.worldManager = worldManager;
    }
}
