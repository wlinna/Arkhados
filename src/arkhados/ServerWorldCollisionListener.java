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
package arkhados;

import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import arkhados.controls.CharacterPhysicsControl;
import arkhados.controls.InfluenceInterfaceControl;
import arkhados.controls.ProjectileControl;
import arkhados.controls.SkyDropControl;
import arkhados.controls.SpellBuffControl;
import arkhados.spell.buffs.CrowdControlBuff;
import arkhados.util.UserDataStrings;

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

        if (event.getNodeA() == null || event.getNodeB() == null) {
            return;
        }
        InfluenceInterfaceControl characterA = event.getNodeA().getControl(InfluenceInterfaceControl.class);
        InfluenceInterfaceControl characterB = event.getNodeB().getControl(InfluenceInterfaceControl.class);

        ProjectileControl projectileA = event.getNodeA().getControl(ProjectileControl.class);
        ProjectileControl projectileB = event.getNodeB().getControl(ProjectileControl.class);

        SkyDropControl skyDrop = event.getNodeA().getControl(SkyDropControl.class);
        if (skyDrop == null) {
            skyDrop = event.getNodeB().getControl(SkyDropControl.class);
        }
        if (skyDrop != null) {
            skyDrop.onGroundCollision();
        }

        if (projectileA != null) {
            if (characterB != null) {
                this.projectileCharacterCollision(projectileA, characterB);
            }
//            this.worldManager.removeEntity((Long) projectileA.getSpatial().getUserData(UserDataStrings.ENTITY_ID), "collision");
        }
        if (projectileB != null) {
            if (characterA != null) {
                this.projectileCharacterCollision(projectileB, characterA);
            }
//            this.worldManager.removeEntity((Long) projectileB.getSpatial().getUserData(UserDataStrings.ENTITY_ID), "collision");
        }

    }

    private void projectileCharacterCollision(ProjectileControl projectile, InfluenceInterfaceControl character) {
        character.doDamage((Float) projectile.getSpatial().getUserData(UserDataStrings.DAMAGE));

        for (CrowdControlBuff cc : projectile.getSpatial().getControl(SpellBuffControl.class).getCrowdControlInfluences()) {
            character.addCrowdControlEffect(cc);
        }

        Float impulseFactor = projectile.getSpatial().getUserData(UserDataStrings.IMPULSE_FACTOR);
        Vector3f impulse = character.getSpatial().getLocalTranslation()
                .subtract(projectile.getRigidBodyControl().getPhysicsLocation().setY(0)).normalizeLocal().multLocal(impulseFactor);
        character.getSpatial().getControl(CharacterPhysicsControl.class).applyImpulse(impulse);

        this.worldManager.removeEntity((Long) projectile.getSpatial().getUserData(UserDataStrings.ENTITY_ID), "collision");

//        character.getSpatial().getControl(CharacterPhysicsControl.class).applyImpulse(Vector3f.UNIT_Y.mult(2000.0f));
    }
}
