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
import arkhados.actions.ASplash;
import arkhados.controls.CCharacterPhysics;
import arkhados.controls.CInfluenceInterface;
import arkhados.controls.CProjectile;
import arkhados.controls.CSpellBuff;
import arkhados.controls.CSpellCast;
import arkhados.controls.CUserInput;
import arkhados.spell.Spell;
import arkhados.spell.buffs.AbstractBuff;
import arkhados.spell.buffs.AbstractBuffBuilder;
import arkhados.util.UserDataStrings;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.List;

/**
 * CastProjectileAction is proper action for basic projectile spells like Magma
 * Bash, Fireball etc.
 *
 * @author william
 */
public class ACastProjectile extends EntityAction {

    private final Spell spell;
    private final WorldManager worldManager;
    private final List<AbstractBuffBuilder> additionalBuffs = new ArrayList<>();
    private boolean detonateAtTarget;

    public ACastProjectile(Spell spell, WorldManager worldManager) {
        this.spell = spell;
        this.worldManager = worldManager;
    }

    public void addBuff(AbstractBuffBuilder buff) {
        additionalBuffs.add(buff);
    }


    @Override
    public boolean update(float tpf) {
        CSpellCast cSpellCast = spatial.getControl(CSpellCast.class);
        Vector3f mouseTarget = cSpellCast.getClosestPointToTarget(spell);

        Vector3f viewDirection = mouseTarget.subtract(
                spatial.getLocalTranslation()).normalizeLocal();
        spatial.getControl(CCharacterPhysics.class)
                .setViewDirection(viewDirection);

        float characterRadius = spatial.getUserData(UserDataStrings.RADIUS);
        Vector3f spawnLocation = spatial.getLocalTranslation().add(
                viewDirection.mult(characterRadius / 1.5f))
                .addLocal(0f, 10.0f, 0.0f);

        int playerId = spatial.getUserData(UserDataStrings.PLAYER_ID);

        int projectileId = worldManager.addNewEntity(spell.getId(),
                spawnLocation, Quaternion.IDENTITY, playerId);
        Spatial projectile = worldManager.getEntity(projectileId);

        float damage = projectile.getUserData(UserDataStrings.DAMAGE);
        float damageFactor = spatial.getUserData(UserDataStrings.DAMAGE_FACTOR);
        projectile.setUserData(UserDataStrings.DAMAGE, damage * damageFactor);

        CProjectile projectileControl =
                projectile.getControl(CProjectile.class);

        if (detonateAtTarget) {
            projectileControl.setTarget(mouseTarget);
        } else {
            projectileControl.setDirection(viewDirection);
            projectileControl.setRange(spell.getRange());
        }

        CInfluenceInterface influenceInterface =
                spatial.getControl(CInfluenceInterface.class);

        projectileControl.setOwnerInterface(influenceInterface);

        ASplash splashAction = projectileControl.getSplashAction();
        if (splashAction != null) {
//            int teamId = projectile.getUserData(UserDataStrings.TEAM_ID);
//            splashAction.setExcludedTeam(teamId);
            splashAction.setCasterInterface(influenceInterface);
        }

        CSpellBuff buffControl =
                projectile.getControl(CSpellBuff.class);
        for (AbstractBuffBuilder buffBuilder : additionalBuffs) {
            buffControl.addBuff(buffBuilder);
        }

        return false;
    }

    public void detonateAtTarget(boolean detonateAtTarget) {
        this.detonateAtTarget = detonateAtTarget;
    }
}
