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
package arkhados.spell.spells.rockgolem;

import arkhados.CharacterInteraction;
import arkhados.CollisionGroups;
import arkhados.actions.EntityAction;
import arkhados.controls.CharacterPhysicsControl;
import arkhados.controls.InfluenceInterfaceControl;
import arkhados.spell.CastSpellActionBuilder;
import arkhados.spell.Spell;
import arkhados.spell.buffs.AbstractBuff;
import arkhados.util.UserDataStrings;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author william
 */
public class StoneFist extends Spell {
    {
        iconName = "StoneFist.png";
    }

    public StoneFist(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Spell create() {
        final float cooldown = 0.5f;
        final float range = 18f;
        final float castTime = 0.6f;
        
        StoneFist spell = new StoneFist("StoneFist", cooldown, range, castTime);
        
        spell.setCanMoveWhileCasting(true);
        spell.castSpellActionBuilder = new CastSpellActionBuilder() {

            @Override
            public EntityAction newAction(Node caster, Vector3f vec) {
                StoneFistAction action = new StoneFistAction(130, range);
                return action;
            }
        };
        
        spell.nodeBuilder = null;
        
        return spell;
    }
}

class StoneFistAction extends EntityAction {

    private List<AbstractBuff> buffs = new ArrayList<>();
    private float damage;
    private float range;

    public StoneFistAction(float damage, float range) {
        this.damage = damage;
        this.range = range;
    }

    public void addBuff(AbstractBuff buff) {
        buffs.add(buff);
    }

    @Override
    public boolean update(float tpf) {
        CharacterPhysicsControl physicsControl = spatial.getControl(CharacterPhysicsControl.class);
        int myPlayerId = spatial.getUserData(UserDataStrings.PLAYER_ID);
        Vector3f hitDirection = physicsControl.calculateTargetDirection().normalize()
                .multLocal(range);

        physicsControl.setViewDirection(hitDirection);
        PhysicsSpace space = physicsControl.getPhysicsSpace();
        Vector3f to = spatial.getLocalTranslation().add(hitDirection);

        List<PhysicsRayTestResult> results = space.rayTest(spatial.getLocalTranslation().clone()
                .setY(3f), to.setY(3f));
        for (PhysicsRayTestResult result : results) {
            PhysicsCollisionObject collisionObject = result.getCollisionObject();
            
            Object userObject = collisionObject.getUserObject();
            if (!(userObject instanceof Node)) {
                continue;
            }
            
            Node node = (Node) userObject;
            if (node == spatial) {
                continue;
            }
            

            Integer nullableTargetPlayerId = node.getUserData(UserDataStrings.PLAYER_ID);
            if (nullableTargetPlayerId == null) {
                continue;
            }
            
            int targetPlayerId = nullableTargetPlayerId.intValue();
            
            if (collisionObject.getCollisionGroup() == CollisionGroups.SPIRIT_STONE) {
                if (targetPlayerId == myPlayerId) {
                    pushSpiritStone(node);
                    break;
                } else {
                    continue;
                }
            }
            
            InfluenceInterfaceControl targetInfluenceControl =
                    node.getControl(InfluenceInterfaceControl.class);
            if (targetInfluenceControl == null) {
                continue;
            }

            final float damageFactor = spatial.getUserData(UserDataStrings.DAMAGE_FACTOR);
            final float rawDamage = damage * damageFactor;
            // TODO: Calculate damage for possible Damage over Time -buffs
            CharacterInteraction.harm(spatial.getControl(InfluenceInterfaceControl.class),
                    targetInfluenceControl, rawDamage, buffs, true);

            // TODO: Add mechanism that allows melee attack to knock enemy back
            break;
        }        
        
        return false;
    }
    
    private void pushSpiritStone(Node stone) {                
        SpiritStonePhysicsControl physics = stone.getControl(SpiritStonePhysicsControl.class);
        
        Vector3f direction = physics.getLocation().subtract(spatial.getLocalTranslation())
                .setY(0f).normalizeLocal();
        physics.punch(direction.multLocal(80f));
        physics.addCollideWithGroup(CollisionGroups.WALLS);
    }
}