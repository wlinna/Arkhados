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
import arkhados.actions.cast.ACastSelfBuff;
import arkhados.controls.CAreaEffect;
import arkhados.controls.CCharacterPhysics;
import arkhados.controls.CInfluenceInterface;
import arkhados.controls.CTimedExistence;
import arkhados.spell.Spell;
import arkhados.spell.buffs.AbsorbingShieldBuff;
import arkhados.spell.buffs.AbstractBuffBuilder;
import arkhados.spell.influences.DamageOverTimeInfluence;
import arkhados.util.PhysicsWorkaround;
import arkhados.util.UserData;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

public class PurifyingFlame extends Spell {

    public static final float COOLDOWN = 12f;

    {
        iconName = "purifying_flame.png";
    }

    public PurifyingFlame(String name, float cooldown, float range,
            float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static PurifyingFlame create() {
        final float range = 200f; // TODO: Does this make sense?
        final float castTime = 0f;

        final PurifyingFlame spell = new PurifyingFlame("Purifying Flame",
                COOLDOWN, range, castTime);

        spell.castSpellActionBuilder = (Node caster, Vector3f vec) -> {
            // TODO: Get this from BuffInformation
            float duration = 3f;
            ACastSelfBuff action = new ACastSelfBuff();
            Node aoeContainer = new Node("purifying-flame");
            
            if (world.isServer()) {
                int playerId =
                        caster.getUserData(UserData.PLAYER_ID);
                aoeContainer
                        .setUserData(UserData.PLAYER_ID, playerId);
                
                int teamId = caster.getUserData(UserData.TEAM_ID);
                aoeContainer.setUserData(UserData.TEAM_ID, teamId);
                
                GhostControl ghost =
                        new GhostControl(new SphereCollisionShape(8f));
                ghost.setCollisionGroup(CollisionGroups.CHARACTERS);
                ghost.setCollideWithGroups(CollisionGroups.CHARACTERS);
                aoeContainer.addControl(ghost);
                
                CAreaEffect areaEffectControl =
                        new CAreaEffect(ghost);
                areaEffectControl.setOwnerInterface(caster
                        .getControl(CInfluenceInterface.class));
                AbstractBuffBuilder ignite = Ignite
                        .ifNotCooldownCreateDamageOverTimeBuff(caster);
                
                if (ignite != null) {
                    areaEffectControl.addEnterBuff(ignite);
                }
                
                float baseDps = 100f;
                float damageFactor =
                        caster.getUserData(UserData.DAMAGE_FACTOR);
                float dps = baseDps * damageFactor;
                DamageOverTimeInfluence damageOverTime =
                        new DamageOverTimeInfluence(dps);
                damageOverTime.setBreaksCrowdControl(false);
                areaEffectControl.addInfluence(damageOverTime);
                
                CInfluenceInterface casterInfluenceInterface =
                        caster.getControl(CInfluenceInterface.class);
                areaEffectControl
                        .setOwnerInterface(casterInfluenceInterface);
                
                aoeContainer.addControl(areaEffectControl);
                
                action.addBuff(new AbsorbingShieldBuff.MyBuilder(duration));
            }
            
            aoeContainer.setLocalTranslation(0f, 0f, 0f);
            CTimedExistence timedExistence =
                    new CTimedExistence(duration);
            aoeContainer.addControl(timedExistence);
            
            PhysicsSpace physicsSpace = caster.getControl(
                    CCharacterPhysics.class).getPhysicsSpace();
            
            timedExistence.setSpace(physicsSpace);
            
            caster.attachChild(aoeContainer);
            PhysicsWorkaround.addAll(physicsSpace, aoeContainer);
            
            return action;
        };

        return spell;
    }
}
