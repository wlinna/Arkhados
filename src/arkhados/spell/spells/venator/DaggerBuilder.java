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
import arkhados.util.UserDataStrings;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.scene.Node;

public class DaggerBuilder extends AbstractNodeBuilder {

    private boolean primary;

    public DaggerBuilder(boolean primary) {
        this.primary = primary;
    }

    @Override
    public Node build(BuildParameters params) {
        Node node =
                (Node) assetManager.loadModel("Models/DamagingDagger.j3o");
        node.setLocalTranslation(params.location);

        node.setUserData(UserDataStrings.SPEED_MOVEMENT, 170f);
        node.setUserData(UserDataStrings.MASS, 30f);
        node.setUserData(UserDataStrings.DAMAGE, 150f);
        node.setUserData(UserDataStrings.IMPULSE_FACTOR, 0f);

        SphereCollisionShape collisionShape = new SphereCollisionShape(4);
        RigidBodyControl physicsBody = new RigidBodyControl(collisionShape,
                (float) node.getUserData(UserDataStrings.MASS));

        physicsBody.setCollisionGroup(CollisionGroups.PROJECTILES);
        physicsBody.removeCollideWithGroup(CollisionGroups.PROJECTILES);

        physicsBody.addCollideWithGroup(CollisionGroups.CHARACTERS
                | CollisionGroups.WALLS);

        node.addControl(physicsBody);

        node.addControl(new CProjectile());
        CSpellBuff buffControl = new CSpellBuff();
        if (primary) {
            buffControl.addBuff(new SlowCC(-1, 6f, 0.33f));
        } else {
            buffControl.addBuff(new CastSpeedBuff(-1, 6f, -0.33f));
        }
        node.addControl(buffControl);

        return node;
    }
}
