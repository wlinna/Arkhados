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
import magebattle.controls.ProjectileControl;

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
        Node projectileA = null;
        Node projectileB = null;

        Spatial staticA = null;
        Spatial staticB = null;

        Spatial characterA = null;
        Spatial characterB = null;

        if (event.getNodeA().getControl(ProjectileControl.class) != null) {
            projectileA = (Node) event.getNodeA();
            this.worldManager.removeEntity((Long) projectileA.getUserData("entity-id"), "collision");
        }
        if (event.getNodeB().getControl(ProjectileControl.class) != null) {
            projectileB = (Node) event.getNodeB();
            this.worldManager.removeEntity((Long) projectileB.getUserData("entity-id"), "collision");
        }

    }
}
