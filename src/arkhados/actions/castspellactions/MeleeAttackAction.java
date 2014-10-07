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

import arkhados.CharacterInteraction;
import arkhados.actions.EntityAction;
import arkhados.controls.CharacterPhysicsControl;
import arkhados.controls.InfluenceInterfaceControl;
import arkhados.controls.UserInputControl;
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
public class MeleeAttackAction extends EntityAction {

    private List<AbstractBuff> buffs = new ArrayList<>();
    private float damage;
    private float range;

    public MeleeAttackAction(float damage, float range) {
        this.damage = damage;
        this.range = range;
    }

    public void addBuff(AbstractBuff buff) {
        buffs.add(buff);
    }

    @Override
    public boolean update(float tpf) {
        final CharacterPhysicsControl physicsControl = spatial
                .getControl(CharacterPhysicsControl.class);
        Vector3f hitDirection = physicsControl.calculateTargetDirection().normalize()
                .multLocal(range);

        physicsControl.setViewDirection(hitDirection);
        final PhysicsSpace space = physicsControl.getPhysicsSpace();
        Vector3f to = spatial.getLocalTranslation().add(hitDirection);

        List<PhysicsRayTestResult> results = space.rayTest(spatial.getLocalTranslation().clone()
                .setY(3f), to.setY(3f));
        for (PhysicsRayTestResult result : results) {
            PhysicsCollisionObject collisionObject = result.getCollisionObject();
            Object userObject = collisionObject.getUserObject();
            if (!(userObject instanceof Node)) {
                continue;
            }
            final Node node = (Node) userObject;
            if (node == spatial) {
                continue;
            }
            final InfluenceInterfaceControl targetInfluenceControl = node
                    .getControl(InfluenceInterfaceControl.class);
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
}