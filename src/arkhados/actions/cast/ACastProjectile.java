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
package arkhados.actions.cast;

import arkhados.World;
import arkhados.actions.EntityAction;
import arkhados.actions.ASplash;
import arkhados.controls.CInfluenceInterface;
import arkhados.controls.CProjectile;
import arkhados.controls.CSpellBuff;
import arkhados.spell.Spell;
import arkhados.spell.buffs.AbstractBuffBuilder;
import arkhados.util.CastSpawn;
import arkhados.util.UserData;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.List;

/**
 * CastProjectileAction is proper action for basic projectile spells like Magma
 * Bash, Fireball etc.
 */
public class ACastProjectile extends EntityAction {

    private final Spell spell;
    private final World world;
    private final List<AbstractBuffBuilder> additionalBuffs = new ArrayList<>();
    private boolean detonateAtTarget;

    public ACastProjectile(Spell spell, World world) {
        this.spell = spell;
        this.world = world;
    }

    public void addBuff(AbstractBuffBuilder buff) {
        additionalBuffs.add(buff);
    }

    @Override
    public boolean update(float tpf) {
        CastSpawn.SpawnInfo spawn =  CastSpawn.spawn(spatial, spell);
        Spatial projectile = spawn.spatial;

        float damage = projectile.getUserData(UserData.DAMAGE);
        float damageFactor = spatial.getUserData(UserData.DAMAGE_FACTOR);
        projectile.setUserData(UserData.DAMAGE, damage * damageFactor);

        CProjectile cProjectile = projectile.getControl(CProjectile.class);

        if (detonateAtTarget) {
            cProjectile.setTarget(spawn.mouseTarget);
        } else {
            cProjectile.setDirection(spawn.viewDirection);
            cProjectile.setRange(spell.getRange());
        }

        CInfluenceInterface influenceInterface =
                spatial.getControl(CInfluenceInterface.class);

        cProjectile.setOwnerInterface(influenceInterface);

        ASplash aSplash = cProjectile.getSplashAction();
        if (aSplash != null) {
//            int teamId = projectile.getUserData(UserDataStrings.TEAM_ID);
//            splashAction.setExcludedTeam(teamId);
            aSplash.setCasterInterface(influenceInterface);
        }

        CSpellBuff cBuff = projectile.getControl(CSpellBuff.class);
        for (AbstractBuffBuilder buffBuilder : additionalBuffs) {
            cBuff.addBuff(buffBuilder);
        }
        
        cBuff.setOwnerInterface(influenceInterface);

        return false;
    }

    public void detonateAtTarget(boolean detonateAtTarget) {
        this.detonateAtTarget = detonateAtTarget;
    }
}
