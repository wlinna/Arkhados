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
package arkhados.spell.spells.elitesoldier;

import arkhados.WorldManager;
import arkhados.actions.ChannelingSpellAction;
import arkhados.actions.EntityAction;
import arkhados.controls.ActionQueueControl;
import arkhados.controls.CharacterPhysicsControl;
import arkhados.controls.EliteSoldierAmmunitionControl;
import arkhados.controls.InfluenceInterfaceControl;
import arkhados.controls.ProjectileControl;
import arkhados.spell.CastSpellActionBuilder;
import arkhados.spell.PelletBuilder;
import arkhados.spell.Spell;
import arkhados.util.UserDataStrings;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 *
 * @author william
 */
public class Machinegun extends Spell {
    {
        this.iconName = "machine_gun.png";
        this.setMoveTowardsTarget(false);
    }

    public Machinegun(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Spell create() {
        final float cooldown = 2f;
        final float range = 100f;
        final float castTime = 0.3f;

        final Machinegun spell = new Machinegun("Machinegun", cooldown, range, castTime);

        spell.castSpellActionBuilder = new CastSpellActionBuilder() {
            @Override
            public EntityAction newAction(Node caster, Vector3f vec) {
                ShootBulletAction shoot = new ShootBulletAction(spell, worldManager);                
                ChannelingSpellAction channeling = new ChannelingSpellAction(spell, 10, 0.2f, shoot);
                return channeling;
            }
        };

        spell.nodeBuilder = new PelletBuilder(15);
        return spell;
    }
}

class ShootBulletAction extends EntityAction {

    private Spell spell;
    private WorldManager worldManager;

    public ShootBulletAction(Spell spell, WorldManager worldManager) {
        this.spell = spell;
        this.worldManager = worldManager;
    }

    @Override
    public boolean update(float tpf) {
        final EliteSoldierAmmunitionControl ammunitionControl = super.spatial.getControl(EliteSoldierAmmunitionControl.class);
        if (!ammunitionControl.validateSpellCast(null, spell)) {
            EntityAction current = super.spatial.getControl(ActionQueueControl.class).getCurrent();
            if (current instanceof ChannelingSpellAction) {
                ((ChannelingSpellAction) current).signalEnd();
            }            
            return false;
        }
        
        ammunitionControl.spellCasted(null, spell);
        final CharacterPhysicsControl physicsControl = super.spatial.getControl(CharacterPhysicsControl.class);

        Vector3f targetLocation = physicsControl.getTargetLocation();
        final Vector3f viewDirection = targetLocation.subtract(super.spatial.getLocalTranslation()).normalizeLocal();
        super.spatial.getControl(CharacterPhysicsControl.class).setViewDirection(viewDirection);

        final Long playerId = super.spatial.getUserData(UserDataStrings.PLAYER_ID);
        final Vector3f pelletDirection = viewDirection.clone();
        Vector3f spawnLocation = super.spatial.getLocalTranslation();

        final long projectileId = this.worldManager.addNewEntity("Shotgun",
                spawnLocation, Quaternion.IDENTITY, playerId);
        final Spatial projectile = this.worldManager.getEntity(projectileId);

        final Float damage = projectile.getUserData(UserDataStrings.DAMAGE);
        final Float damageFactor = super.spatial.getUserData(UserDataStrings.DAMAGE_FACTOR);
        projectile.setUserData(UserDataStrings.DAMAGE, damage * damageFactor);

        final ProjectileControl projectileControl = projectile.getControl(ProjectileControl.class);
        projectileControl.setRange(this.spell.getRange());
        projectileControl.setDirection(pelletDirection);
        projectileControl.setOwnerInterface(super.spatial.getControl(InfluenceInterfaceControl.class));
        return false;
    }
}
