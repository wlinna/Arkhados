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

import arkhados.World;
import arkhados.actions.AChannelingSpell;
import arkhados.actions.EntityAction;
import arkhados.controls.CActionQueue;
import arkhados.controls.CCharacterPhysics;
import arkhados.controls.CEliteSoldierAmmunition;
import arkhados.controls.CInfluenceInterface;
import arkhados.controls.CProjectile;
import arkhados.spell.PelletBuilder;
import arkhados.spell.Spell;
import arkhados.util.UserData;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class Machinegun extends Spell {

    {
        iconName = "machine_gun.png";
        setMoveTowardsTarget(false);
    }

    public Machinegun(String name, float cooldown, float range,
            float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Spell create() {
        final float cooldown = 2f;
        final float range = 105f;
        final float castTime = 0.3f;

        final Machinegun spell = new Machinegun("Machinegun", cooldown,
                range, castTime);

        spell.castSpellActionBuilder = (Node caster, Vector3f vec) -> {
            AShootBullet shoot = new AShootBullet(spell, world);
            AChannelingSpell channeling =
                    new AChannelingSpell(spell, 10, 0.16f, shoot);
            return channeling;
        };

        spell.nodeBuilder = new PelletBuilder(40);
        return spell;
    }
}

class AShootBullet extends EntityAction {

    private Spell spell;
    private World world;

    public AShootBullet(Spell spell, World world) {
        this.spell = spell;
        this.world = world;
    }

    @Override
    public boolean update(float tpf) {
        CEliteSoldierAmmunition cAmmunition =
                spatial.getControl(CEliteSoldierAmmunition.class);
        if (!cAmmunition.validateSpellCast(null, spell)) {
            EntityAction current =
                    spatial.getControl(CActionQueue.class).getCurrent();
            if (current instanceof AChannelingSpell) {
                ((AChannelingSpell) current).signalEnd();
            }
            return false;
        }

        cAmmunition.spellCasted(null, spell);
        CCharacterPhysics physicsControl =
                spatial.getControl(CCharacterPhysics.class);

        Vector3f targetLocation = physicsControl.getTargetLocation();
        Vector3f viewDirection = targetLocation
                .subtract(spatial.getLocalTranslation()).normalizeLocal();
        spatial.getControl(CCharacterPhysics.class)
                .setViewDirection(viewDirection);

        Integer playerId = spatial.getUserData(UserData.PLAYER_ID);
        Vector3f pelletDirection = viewDirection.clone();
        Vector3f spawnLocation = spatial.getLocalTranslation();

        int projectileId = world.addNewEntity(spell.getId(),
                spawnLocation, Quaternion.IDENTITY, playerId);
        Spatial projectile = world.getEntity(projectileId);

        Float damage = projectile.getUserData(UserData.DAMAGE);
        Float damageFactor = spatial.getUserData(UserData.DAMAGE_FACTOR);
        projectile.setUserData(UserData.DAMAGE, damage * damageFactor);

        CProjectile projectileControl =
                projectile.getControl(CProjectile.class);
        projectileControl.setRange(spell.getRange());
        projectileControl.setDirection(pelletDirection);
        projectileControl.setOwnerInterface(spatial
                .getControl(CInfluenceInterface.class));
        return false;
    }
}
