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
import arkhados.util.PlayerDataStrings;
import arkhados.util.RemovalReasons;
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
        if (event.getNodeA() == null || event.getNodeB() == null) {
            return;
        }

        Spatial wallA = null;
        Spatial wallB = null;

        if (event.getObjectA().getCollisionGroup() == CollisionGroups.WALLS) {
            wallA = event.getNodeA();
        }

        if (event.getObjectB().getCollisionGroup() == CollisionGroups.WALLS) {
            wallB = event.getNodeB();
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
            if (projectileA.getSpatial().getParent() == null) {
                return;
            }

            if (characterB != null) {
                projectileCharacterCollision(projectileA, characterB);
            } else if (wallB != null) {
                projectileWallCollision(projectileA, wallB);
            }
        }
        if (projectileB != null) {
            if (projectileB.getSpatial().getParent() == null) {
                return;
            }

            if (characterA != null) {
                projectileCharacterCollision(projectileB, characterA);
            } else if (wallA != null) {
                projectileWallCollision(projectileB, wallA);
            }
        }

    }

    private void projectileCharacterCollision(ProjectileControl projectile,
            InfluenceInterfaceControl target) {
        
        int projectileTeamId = projectile.getSpatial().getUserData(UserDataStrings.TEAM_ID);
        int targetPlayerId = target.getSpatial().getUserData(UserDataStrings.PLAYER_ID);
        int targetTeamId = PlayerData.getIntData(targetPlayerId, PlayerDataStrings.TEAM_ID);

        if (targetTeamId == projectileTeamId) {
            return;
        }
        
        if (projectile.getHurted().contains(target.getSpatial())) {
            return;
        }

        int removalReason = RemovalReasons.COLLISION;
        if (target.isImmuneToProjectiles() && projectile.isProjectile()) {
            removalReason = RemovalReasons.ABSORBED;
        } else {
            final float damage = projectile.getSpatial().getUserData(UserDataStrings.DAMAGE);
            final SpellBuffControl buffControl = projectile.getSpatial()
                    .getControl(SpellBuffControl.class);

            final boolean canBreakCC = damage > 0f ? true : false;

            CharacterInteraction.harm(projectile.getOwnerInterface(), target,
                    damage, buffControl.getBuffs(), canBreakCC);

            Float impulseFactor = projectile.getSpatial()
                    .getUserData(UserDataStrings.IMPULSE_FACTOR);

            Vector3f impulse = target.getSpatial().getLocalTranslation()
                    .subtract(projectile.getRigidBodyControl()
                    .getPhysicsLocation().setY(0)).normalizeLocal()
                    .multLocal(impulseFactor);

            target.getSpatial().getControl(CharacterPhysicsControl.class)
                    .applyImpulse(impulse);

            if (projectile.getSplashAction() != null) {
                projectile.getSplashAction().excludeSpatial(target.getSpatial());
                projectile.getSplashAction().update(0);
            }
        }

        int entityId = projectile.getSpatial().getUserData(UserDataStrings.ENTITY_ID);

        if (projectile.isProjectile()) {
            worldManager.removeEntity(entityId, removalReason);
        } else {
            projectile.getHurted().add(target.getSpatial());
        }
    }

    private void projectileWallCollision(ProjectileControl projectile, Spatial wall) {
        if (projectile.getSplashAction() != null) {
            projectile.getSplashAction().update(0);
        }

        int entityId = projectile.getSpatial().getUserData(UserDataStrings.ENTITY_ID);
        worldManager.removeEntity(entityId, RemovalReasons.COLLISION);
    }
}