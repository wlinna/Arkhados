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
package magebattle;

import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import magebattle.controls.InfluenceInterfaceControl;
import magebattle.controls.ProjectileControl;
import magebattle.util.UserDataStrings;

/**
 *
 * @author william
 */
public class ServerWorldCollisionListener implements PhysicsCollisionListener {

    private WorldManager worldManager;
    private SyncManager syncManager;

    public ServerWorldCollisionListener(WorldManager worldManager, SyncManager syncManager) {
        this.worldManager = worldManager;
        this.syncManager = syncManager;
    }

    public void collision(PhysicsCollisionEvent event) {


        Spatial staticA = null;
        Spatial staticB = null;

        InfluenceInterfaceControl characterA = event.getNodeA().getControl(InfluenceInterfaceControl.class);
        InfluenceInterfaceControl characterB = event.getNodeB().getControl(InfluenceInterfaceControl.class);

        ProjectileControl projectileA = event.getNodeA().getControl(ProjectileControl.class);
        ProjectileControl projectileB = event.getNodeB().getControl(ProjectileControl.class);

        if (projectileA != null) {
            if (characterB != null) {
                this.projectileCharacterCollision(projectileA, characterB);
            }
            this.worldManager.removeEntity((Long) projectileA.getSpatial().getUserData(UserDataStrings.ENTITY_ID), "collision");
        }
        if (projectileB != null) {
            if (characterA != null) {
                this.projectileCharacterCollision(projectileB, characterA);
            }
            this.worldManager.removeEntity((Long) projectileB.getSpatial().getUserData(UserDataStrings.ENTITY_ID), "collision");
        }

    }

    private void projectileCharacterCollision(ProjectileControl projectile, InfluenceInterfaceControl character) {
        character.doDamage((Float) projectile.getSpatial().getUserData(UserDataStrings.DAMAGE));
    }
}
