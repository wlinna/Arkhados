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
import arkhados.actions.EntityAction;
import arkhados.characters.EliteSoldier;
import arkhados.controls.CharacterPhysicsControl;
import arkhados.controls.InfluenceInterfaceControl;
import arkhados.controls.ProjectileControl;
import arkhados.spell.CastSpellActionBuilder;
import arkhados.spell.PelletBuilder;
import arkhados.spell.Spell;
import arkhados.util.UserDataStrings;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * EliteSoldiers's Shotgun (M1) spell. Fires 6 pellets that spread out.
 */
public class Shotgun extends Spell {

    {
        iconName = "shotgun.png";
        setMoveTowardsTarget(false);
    }

    public Shotgun(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Spell create() {
        final float cooldown = 0.8f;
        final float range = 80f;
        final float castTime = 0.3f;

        final Shotgun spell = new Shotgun("Shotgun", cooldown, range, castTime);

        spell.castSpellActionBuilder = new CastSpellActionBuilder() {
            @Override
            public EntityAction newAction(Node caster, Vector3f location) {
                CastShotgunAction castShotgun = new CastShotgunAction(spell, Spell.worldManager);
                return castShotgun;
            }
        };

        spell.nodeBuilder = new PelletBuilder(35);

        return spell;
    }
}

class CastShotgunAction extends EntityAction {

    private static final int PELLETS = 6;
    private static final float SPREAD = 13f * FastMath.DEG_TO_RAD;
    private static final float STEP = SPREAD / (PELLETS - 1);
    private WorldManager world;
    private final Shotgun spell;

    CastShotgunAction(Shotgun spell, WorldManager world) {
        this.spell = spell;
        this.world = world;
        setTypeId(EliteSoldier.ACTION_SHOTGUN);
    }

    @Override
    public boolean update(float tpf) {
        CharacterPhysicsControl physicsControl = spatial.getControl(CharacterPhysicsControl.class);

        Vector3f targetLocation = physicsControl.getTargetLocation();
        final Vector3f viewDirection = targetLocation.subtract(spatial.getLocalTranslation())
                .normalizeLocal();
        spatial.getControl(CharacterPhysicsControl.class).setViewDirection(viewDirection);

        int playerId = spatial.getUserData(UserDataStrings.PLAYER_ID);

        Vector3f spawnLocation = spatial.getLocalTranslation();
        Quaternion currentRotation = new Quaternion();

        for (int i = 0; i < PELLETS; ++i) {
            currentRotation.fromAngleAxis(SPREAD / 2f - i * STEP, Vector3f.UNIT_Y);

            Vector3f pelletDirection = currentRotation.mult(viewDirection).normalizeLocal();

            int projectileId = world.addNewEntity(spell.getId(), spawnLocation, Quaternion.IDENTITY,
                    playerId);
            Spatial projectile = world.getEntity(projectileId);

            final float damage = projectile.getUserData(UserDataStrings.DAMAGE);
            final float damageFactor = spatial.getUserData(UserDataStrings.DAMAGE_FACTOR);
            projectile.setUserData(UserDataStrings.DAMAGE, damage * damageFactor);

            ProjectileControl projectileControl = projectile.getControl(ProjectileControl.class);
            projectileControl.setRange(spell.getRange());
            projectileControl.setDirection(pelletDirection);
            projectileControl.setOwnerInterface(spatial.getControl(InfluenceInterfaceControl.class));
        }
        
        return false;
    }
}