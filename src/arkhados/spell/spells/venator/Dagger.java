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
import arkhados.actions.EntityAction;
import arkhados.actions.castspellactions.CastProjectileAction;
import arkhados.controls.ProjectileControl;
import arkhados.controls.SpellBuffControl;
import arkhados.spell.CastSpellActionBuilder;
import arkhados.spell.Spell;
import arkhados.spell.buffs.SlowCC;
import arkhados.util.AbstractNodeBuilder;
import arkhados.util.UserDataStrings;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 *
 * @author william
 */
public class Dagger extends Spell {
    {
        super.iconName = "damaging_dagger.png";
    }

    public Dagger(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Spell create() {
        final float cooldown = 6f;
        final float range = 100f;
        final float castTime = 0.4f;

        final Dagger spell = new Dagger("Damaging Dagger", cooldown, range, castTime);

        spell.castSpellActionBuilder = new CastSpellActionBuilder() {
            public EntityAction newAction(Node caster, Vector3f vec) {
                return new CastProjectileAction(spell, worldManager);
            }
        };

        spell.nodeBuilder = new DaggerBuilder();

        return spell;
    }

    private static class DaggerBuilder extends AbstractNodeBuilder {

        @Override
        public Node build() {
            Node node = (Node) assetManager.loadModel("Models/DamagingDagger.j3o");

            node.setUserData(UserDataStrings.SPEED_MOVEMENT, 170f);
            node.setUserData(UserDataStrings.MASS, 30f);
            node.setUserData(UserDataStrings.DAMAGE, 150f);
            node.setUserData(UserDataStrings.IMPULSE_FACTOR, 0f);

            SphereCollisionShape collisionShape = new SphereCollisionShape(4);
            RigidBodyControl physicsBody = new RigidBodyControl(collisionShape,
                    (Float) node.getUserData(UserDataStrings.MASS));

            physicsBody.setCollisionGroup(CollisionGroups.PROJECTILES);
            physicsBody.removeCollideWithGroup(CollisionGroups.PROJECTILES);

            physicsBody.addCollideWithGroup(CollisionGroups.CHARACTERS |
                    CollisionGroups.WALLS);

            node.addControl(physicsBody);

            node.addControl(new ProjectileControl());
            SpellBuffControl buffControl = new SpellBuffControl();
            buffControl.addBuff(new SlowCC(-1, 6f, 0.3f));
            node.addControl(buffControl);

            return node;
        }
    }
}