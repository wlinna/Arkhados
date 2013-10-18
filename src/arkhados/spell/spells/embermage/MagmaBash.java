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

import arkhados.actions.EntityAction;
import arkhados.actions.castspellactions.CastProjectileAction;
import arkhados.controls.ProjectileControl;
import arkhados.controls.SpellBuffControl;
import arkhados.spell.CastSpellActionBuilder;
import arkhados.spell.Spell;
import arkhados.spell.buffs.IncapacitateCC;
import arkhados.util.NodeBuilder;
import arkhados.util.UserDataStrings;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;

/**
 * Embermage's Magma Bash (M2) spell. Fast flying projectile with no damage or
 * knockback but incapacitates enemy for specified time.
 */
public class MagmaBash extends Spell {

    public MagmaBash(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static MagmaBash create() {
        final float cooldown = 5f;
        final float range = 120f;
        final float castTime = 0.3f;

        final MagmaBash spell = new MagmaBash("Magma Bash", cooldown, range, castTime);

        spell.castSpellActionBuilder = new CastSpellActionBuilder() {
            public EntityAction newAction(Vector3f vec) {
                return new CastProjectileAction(spell, worldManager);
            }
        };

        spell.nodeBuilder = new MagmaBashBuilder();

        return spell;
    }
}

class MagmaBashBuilder extends NodeBuilder {

    public Node build() {
        Sphere sphere = new Sphere(32, 32, 1.0f);
        Geometry projectileGeom = new Geometry("projectile-geom", sphere);
        Node node = new Node("projectile");
        node.attachChild(projectileGeom);

        // TODO: Give at least bit better material
        Material material = new Material(NodeBuilder.assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", ColorRGBA.Black);
        node.setMaterial(material);
        node.setUserData(UserDataStrings.SPEED_MOVEMENT, 180f);
        node.setUserData(UserDataStrings.MASS, 10f);
        node.setUserData(UserDataStrings.DAMAGE, 0f);
        node.setUserData(UserDataStrings.IMPULSE_FACTOR, 0f);
        node.setUserData(UserDataStrings.INCAPACITATE_LENGTH, 0.4f);

        if (NodeBuilder.worldManager.isClient()) {
            final ParticleEmitter fire = new ParticleEmitter("fire-emitter", ParticleMesh.Type.Triangle, 80);
            Material materialRed = new Material(NodeBuilder.assetManager, "Common/MatDefs/Misc/Particle.j3md");
            materialRed.setTexture("Texture", NodeBuilder.assetManager.loadTexture("Effects/flame.png"));
            fire.setMaterial(materialRed);
            fire.setImagesX(2);
            fire.setImagesY(2);
            fire.setSelectRandomImage(true);
            fire.setStartColor(new ColorRGBA(0.95f, 0.850f, 0.0f, 1.0f));
            fire.setEndColor(new ColorRGBA(1.0f, 1.0f, 0.80f, 0.5f));
            fire.getParticleInfluencer().setInitialVelocity(Vector3f.ZERO);
            fire.setStartSize(1.5f);
            fire.setEndSize(0.5f);
            fire.setGravity(Vector3f.ZERO);
            fire.setLowLife(0.2f);
            fire.setHighLife(0.3f);
            fire.setParticlesPerSec(60);
            fire.getParticleInfluencer().setVelocityVariation(0.2f);
            fire.setRandomAngle(true);
            node.attachChild(fire);
        }

        SphereCollisionShape collisionShape = new SphereCollisionShape(5.0f);
        RigidBodyControl physicsBody = new RigidBodyControl(collisionShape, (Float) node.getUserData(UserDataStrings.MASS));
        physicsBody.setCollisionGroup(RigidBodyControl.COLLISION_GROUP_16);
        physicsBody.removeCollideWithGroup(RigidBodyControl.COLLISION_GROUP_16);
        physicsBody.addCollideWithGroup(RigidBodyControl.COLLISION_GROUP_02);
        node.addControl(physicsBody);

        node.addControl(new ProjectileControl());
        SpellBuffControl buffControl = new SpellBuffControl();
        node.addControl(buffControl);
        buffControl.addBuff(new IncapacitateCC(1.6f, -1));

        node.getControl(RigidBodyControl.class).setGravity(Vector3f.ZERO);
        return node;
    }
}
