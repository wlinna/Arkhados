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
package arkhados.spell.spells.madblow;

import arkhados.CharacterInteraction;
import arkhados.SpatialDistancePair;
import arkhados.actions.ATrance;
import arkhados.actions.EntityAction;
import arkhados.controls.CActionQueue;
import arkhados.controls.CCharacterPhysics;
import arkhados.controls.CInfluenceInterface;
import arkhados.controls.CMovementForcer;
import arkhados.spell.Spell;
import arkhados.spell.buffs.AbstractBuffBuilder;
import arkhados.util.Selector;
import arkhados.util.UserData;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class CrushingBlow extends Spell {

    {
        iconName = "StoneFist.png";
    }

    public CrushingBlow(String name, float cooldown, float range,
            float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Spell create() {
        final float cooldown = 7.5f;
        final float range = 25f;
        final float castTime = 0.6f;

        CrushingBlow spell = new CrushingBlow("Crushing Blow",
                cooldown, range, castTime);

        spell.setCanMoveWhileCasting(true);
        spell.castSpellActionBuilder = (Node caster, Vector3f vec) -> {
            ABlow action = new ABlow(130, range);
            return action;
        };

        spell.nodeBuilder = null;

        return spell;
    }
}

class ABlow extends EntityAction {

    private final List<AbstractBuffBuilder> buffs = new ArrayList<>();
    private final float damage;
    private final float range;

    public ABlow(float damage, float range) {
        this.damage = damage;
        this.range = range;
    }

    @Override
    public boolean update(float tpf) {
        CCharacterPhysics physicsControl
                = spatial.getControl(CCharacterPhysics.class);
        final int myTeamId = spatial.getUserData(UserData.TEAM_ID);
        Vector3f hitDirection = physicsControl.calculateTargetDirection()
                .normalize().multLocal(range);

        physicsControl.setViewDirection(hitDirection);

        Predicate<SpatialDistancePair> pred = (SpatialDistancePair value) -> {
            if (value.spatial == spatial) {
                return false;
            }

            Integer nullableTeam = value.spatial.getUserData(UserData.TEAM_ID);
            if (nullableTeam == null) {
                return false;
            }

            CInfluenceInterface influenceInterface = value.spatial
                    .getControl(CInfluenceInterface.class);

            return influenceInterface != null && !nullableTeam.equals(myTeamId);
        };

        SpatialDistancePair closest = Selector.giveClosest(Selector.coneSelect(
                new ArrayList<>(), pred, spatial.getLocalTranslation(),
                hitDirection, range, (float) Math.toRadians(50f)));

        if (closest == null) {
            return false;
        }

        CInfluenceInterface targetInterface
                = closest.spatial.getControl(CInfluenceInterface.class);
        if (targetInterface != null) {
            EntityAction current = closest.spatial
                    .getControl(CActionQueue.class).getCurrent();

            if (current != null && current instanceof ATrance) {
                ((ATrance) current).activate(spatial);
                return false;
            }

            float damageFactor = spatial.getUserData(UserData.DAMAGE_FACTOR);
            float rawDamage = damage * damageFactor;
            // TODO: Calculate damage for possible Damage over Time -buffs
            CharacterInteraction.harm(
                    spatial.getControl(CInfluenceInterface.class),
                    targetInterface, rawDamage, buffs, true);

            pushTarget(targetInterface.getSpatial(), hitDirection);
        }

        return false;
    }

    private void pushTarget(Spatial target, Vector3f hitDirection) {
        Vector3f direction = hitDirection.normalize().multLocal(160f);
        CMovementForcer forcer = new CMovementForcer(40f, direction);
        forcer.setIgnored(spatial);
        target.addControl(forcer);
    }
}