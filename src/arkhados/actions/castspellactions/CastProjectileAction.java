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
import arkhados.controls.DebugControl;
import arkhados.controls.ProjectileControl;
import arkhados.spell.Spell;
import arkhados.util.UserDataStrings;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

/**
 * CastProjectileAction is proper action for basic projectile spells like Magma Bash, Fireball etc.
 * @author william
 */
public class CastProjectileAction extends EntityAction {

    private final Spell spell;
    private final WorldManager worldManager;

    public CastProjectileAction(Spell spell, WorldManager worldManager) {
        this.spell = spell;
        this.worldManager = worldManager;
    }

    @Override
    public boolean update(float tpf) {
        CharacterPhysicsControl physicsControl = super.spatial.getControl(CharacterPhysicsControl.class);
        Vector3f targetLocation = physicsControl.getTargetLocation();
        final Vector3f viewDirection = targetLocation.subtract(super.spatial.getLocalTranslation()).normalizeLocal();
        super.spatial.getControl(CharacterPhysicsControl.class).setViewDirection(viewDirection);

        float characterRadius = super.spatial.getUserData(UserDataStrings.RADIUS);

        long projectileId = this.worldManager.addNewEntity(this.spell.getName(),
                super.spatial.getLocalTranslation().add(viewDirection.mult(characterRadius)).addLocal(0f, 10.0f, 0.0f), Quaternion.IDENTITY);
        Spatial projectile = this.worldManager.getEntity(projectileId);

        final SphereCollisionShape collisionSphere = (SphereCollisionShape) projectile.getControl(RigidBodyControl.class).getCollisionShape();

        // FIXME: Get radius of BetterCharacterControl's capsule
        final float radius = collisionSphere.getRadius() * 1.0f;

        RigidBodyControl body = projectile.getControl(RigidBodyControl.class);
        body.setPhysicsLocation(body.getPhysicsLocation().add(viewDirection.multLocal(radius + characterRadius)).addLocal(0.0f, 10.0f, 0.0f));

        ProjectileControl projectileControl = projectile.getControl(ProjectileControl.class);
        projectileControl.setRange(this.spell.getRange());
        projectileControl.setTarget(targetLocation);

        return false;
    }
}
