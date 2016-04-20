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
package arkhados.spell.spells.venator;

import arkhados.CollisionGroups;
import arkhados.controls.CProjectile;
import arkhados.controls.CSpellBuff;
import arkhados.spell.buffs.CastSpeedBuff;
import arkhados.spell.buffs.SlowCC;
import arkhados.util.AbstractNodeBuilder;
import arkhados.util.BuildParameters;
import arkhados.util.UserData;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.scene.Node;

public class DaggerBuilder extends AbstractNodeBuilder {

    private boolean primary;

    public DaggerBuilder(boolean primary) {
        this.primary = primary;
    }

    @Override
    public Node build(BuildParameters params) {
        Node node
                = (Node) assets.loadModel("Models/DamagingDagger.j3o");
        node.setLocalTranslation(params.location);

        node.setUserData(UserData.SPEED_MOVEMENT, 170f);
        node.setUserData(UserData.MASS, 30f);
        node.setUserData(UserData.DAMAGE, 150f);
        node.setUserData(UserData.IMPULSE_FACTOR, 0f);

        SphereCollisionShape collisionShape = new SphereCollisionShape(4);

        GhostControl characterCollision = new GhostControl(collisionShape);
        characterCollision.setCollideWithGroups(CollisionGroups.CHARACTERS);
        characterCollision.setCollisionGroup(CollisionGroups.PROJECTILES);
        node.addControl(characterCollision);

        RigidBodyControl physicsBody = new RigidBodyControl(collisionShape,
                (float) node.getUserData(UserData.MASS));
        physicsBody.setCollisionGroup(CollisionGroups.PROJECTILES);
        physicsBody.removeCollideWithGroup(CollisionGroups.PROJECTILES);
        node.addControl(physicsBody);

        node.addControl(new CProjectile());
        CSpellBuff buffControl = new CSpellBuff();
        if (primary) {
            buffControl.addBuff(new SlowCC.MyBuilder(6f, 0.33f));
        } else {
            buffControl.addBuff(new CastSpeedBuff.MyBuilder(6f, -0.50f));
        }
        node.addControl(buffControl);

        return node;
    }
}
