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

import arkhados.actions.EntityAction;
import arkhados.actions.castspellactions.CastProjectileAction;
import arkhados.controls.ProjectileControl;
import arkhados.controls.SpellBuffControl;
import arkhados.spell.CastSpellActionBuilder;
import arkhados.spell.Spell;
import arkhados.spell.buffs.SlowCC;
import arkhados.util.NodeBuilder;
import arkhados.util.UserDataStrings;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;

/**
 *
 * @author william
 */
public class Dagger extends Spell {

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

    private static class DaggerBuilder extends NodeBuilder {

        @Override
        public Node build() {
            Sphere sphere = new Sphere(32, 32, 1f);
            Geometry daggerGeom = new Geometry("projectile-geom", sphere);
            Node node = new Node("projectile");
            node.attachChild(daggerGeom);


            Material material = new Material(NodeBuilder.assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            material.setColor("Color", ColorRGBA.Red);
            node.setMaterial(material);

            node.setUserData(UserDataStrings.SPEED_MOVEMENT, 170f);
            node.setUserData(UserDataStrings.MASS, 30f);
            node.setUserData(UserDataStrings.DAMAGE, 100f);
            node.setUserData(UserDataStrings.IMPULSE_FACTOR, 0f);

            SphereCollisionShape collisionShape = new SphereCollisionShape(5.0f);
            RigidBodyControl physicsBody = new RigidBodyControl(collisionShape, (Float) node.getUserData(UserDataStrings.MASS));

            physicsBody.setCollisionGroup(RigidBodyControl.COLLISION_GROUP_16);
            physicsBody.removeCollideWithGroup(RigidBodyControl.COLLISION_GROUP_16);

            physicsBody.addCollideWithGroup(RigidBodyControl.COLLISION_GROUP_02);

            node.addControl(physicsBody);

            node.addControl(new ProjectileControl());
            SpellBuffControl buffControl = new SpellBuffControl();
            buffControl.addBuff(new SlowCC(-1, 6f, 0.2f));
            node.addControl(buffControl);

            return node;
        }
    }
}