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
package arkhados.spell.spells.embermage;

import arkhados.CollisionGroups;
import arkhados.actions.EntityAction;
import arkhados.actions.castspellactions.CastSelfBuffAction;
import arkhados.controls.AreaEffectControl;
import arkhados.controls.CharacterPhysicsControl;
import arkhados.controls.InfluenceInterfaceControl;
import arkhados.controls.TimedExistenceControl;
import arkhados.spell.CastSpellActionBuilder;
import arkhados.spell.Spell;
import arkhados.spell.buffs.AbstractBuff;
import arkhados.spell.influences.DamagOverTimeInfluence;
import arkhados.util.UserDataStrings;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 *
 * @author william
 */
public class PurifyingFlame extends Spell {
    {
        super.iconName = "purifying_flame.png";
    }

    public PurifyingFlame(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static PurifyingFlame create() {
        final float cooldown = 12f;
        final float range = 200f;
        final float castTime = 0f;

        final PurifyingFlame spell = new PurifyingFlame("Purifying Flame", cooldown, range, castTime);

        spell.castSpellActionBuilder = new CastSpellActionBuilder() {
            public EntityAction newAction(Node caster, Vector3f vec) {

                // TODO: Get this from BuffInformation
                final float duration = 3f;
                final CastSelfBuffAction action = new CastSelfBuffAction();
                final Node aoeContainer = new Node("purifying-flame");
                if (worldManager.isServer()) {
                    final Long playerId = caster.getUserData(UserDataStrings.PLAYER_ID);
                    aoeContainer.setUserData(UserDataStrings.PLAYER_ID, playerId);

                    final GhostControl ghost = new GhostControl(new SphereCollisionShape(8f));
                    ghost.setCollisionGroup(CollisionGroups.CHARACTERS);
                    ghost.setCollideWithGroups(CollisionGroups.CHARACTERS);
                    aoeContainer.addControl(ghost);

                    final AreaEffectControl areaEffectControl = new AreaEffectControl(ghost);
                    areaEffectControl.setOwnerInterface(caster.getControl(InfluenceInterfaceControl.class));
                    areaEffectControl.addEnterBuff(Ignite.ifNotCooldownCreateDamageOverTimeBuff(caster));

                    float baseDps = 100f;
                    final Float damageFactor = caster.getUserData(UserDataStrings.DAMAGE_FACTOR);
                    final float dps = baseDps * damageFactor;

                    areaEffectControl.addInfluence(new DamagOverTimeInfluence(dps));

                    aoeContainer.addControl(areaEffectControl);

                    action.addBuff(new AbsorbingShieldBuff(-1, duration));
                }
                aoeContainer.setLocalTranslation(0f, 0f, 0f);
                TimedExistenceControl timedExistence = new TimedExistenceControl(duration);
                aoeContainer.addControl(timedExistence);
                timedExistence.setSpace(caster.getControl(CharacterPhysicsControl.class).getPhysicsSpace());

                caster.attachChild(aoeContainer);

                return action;
            }
        };

        return spell;
    }}

class AbsorbingShieldBuff extends AbstractBuff {

    {
        super.friendly = true;
        super.name = "Purifying Flame";
    }
    public AbsorbingShieldBuff(long buffGroupId, float duration) {
        super(buffGroupId, duration);
    }

    @Override
    public void update(float time) {
        super.update(time);
        super.targetInterface.setImmuneToProjectiles(true);
    }
}
