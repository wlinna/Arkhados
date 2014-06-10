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

import arkhados.controls.CharacterPhysicsControl;
import arkhados.controls.InfluenceInterfaceControl;
import arkhados.controls.ProjectileControl;
import arkhados.controls.SkyDropControl;
import arkhados.controls.SpellBuffControl;
import arkhados.spell.buffs.AbstractBuff;
import arkhados.util.PlayerDataStrings;
import arkhados.util.UserDataStrings;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

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

    @Override
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
        }
        if (projectileB != null) {
            if (characterA != null) {
                this.projectileCharacterCollision(projectileB, characterA);
            }
        }

    }

    private void projectileCharacterCollision(ProjectileControl projectile, InfluenceInterfaceControl target) {

        final Integer projectilePlayerId = projectile.getSpatial().getUserData(UserDataStrings.PLAYER_ID);
        final Integer projectileTeamId = PlayerData.getIntData(projectilePlayerId, PlayerDataStrings.TEAM_ID);
        final Integer targetPlayerId = target.getSpatial().getUserData(UserDataStrings.PLAYER_ID);
        final Integer targetTeamId = PlayerData.getIntData(targetPlayerId, PlayerDataStrings.TEAM_ID);
        
        if (targetTeamId == projectileTeamId) {
            return;
        }

        String removalReason = "collision";
        if (target.isImmuneToProjectiles()) {
            removalReason = "absorbed";
        } else {
            final Float damage = projectile.getSpatial().getUserData(UserDataStrings.DAMAGE);
            final SpellBuffControl buffControl = projectile.getSpatial().getControl(SpellBuffControl.class);
            final boolean canBreakCC = damage > 0f ? true : false;
            CharacterInteraction.harm(projectile.getOwnerInterface(), target, damage, buffControl.getBuffs(), canBreakCC);

            Float impulseFactor = projectile.getSpatial().getUserData(UserDataStrings.IMPULSE_FACTOR);
            Vector3f impulse = target.getSpatial().getLocalTranslation()
                    .subtract(projectile.getRigidBodyControl().getPhysicsLocation().setY(0)).normalizeLocal().multLocal(impulseFactor);
            target.getSpatial().getControl(CharacterPhysicsControl.class).applyImpulse(impulse);
        }

        this.worldManager.removeEntity((Integer) projectile.getSpatial().getUserData(UserDataStrings.ENTITY_ID), removalReason);
    }
}
