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

import arkhados.WorldManager;
import arkhados.actions.EntityAction;
import arkhados.actions.castspellactions.CastProjectileAction;
import arkhados.controls.EntityEventControl;
import arkhados.controls.ProjectileControl;
import arkhados.controls.SpellBuffControl;
import arkhados.controls.TimedExistenceControl;
import arkhados.entityevents.RemovalEventAction;
import arkhados.spell.CastSpellActionBuilder;
import arkhados.spell.Spell;
import arkhados.util.NodeBuilder;
import arkhados.util.UserDataStrings;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.shapes.EmitterSphereShape;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;

/**
 * Embermage's Fireball (M1) spell. Projectile has moderate speed and deals
 * moderate damage. Has small knockback effect on hit.
 */
public class Fireball extends Spell {

    public Fireball(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Spell create() {
        final float cooldown = 1.0f;
        final float range = 40f;
        final float castTime = 0.2f;

        final Fireball spell = new Fireball("Fireball", cooldown, range, castTime);

        spell.castSpellActionBuilder = new CastSpellActionBuilder() {
            public EntityAction newAction(Vector3f location) {
                return new CastProjectileAction(spell, Spell.worldManager);
            }
        };

        spell.nodeBuilder = new FireballBuilder();

        return spell;
    }
}

class FireballBuilder extends NodeBuilder {

    private ParticleEmitter createFireEmitter() {
        final ParticleEmitter fire = new ParticleEmitter("fire-emitter", ParticleMesh.Type.Triangle, 100);
        Material materialRed = new Material(NodeBuilder.assetManager, "Common/MatDefs/Misc/Particle.j3md");
        materialRed.setTexture("Texture", NodeBuilder.assetManager.loadTexture("Effects/flame.png"));
        fire.setMaterial(materialRed);
        fire.setImagesX(2);
        fire.setImagesY(2);
        fire.setSelectRandomImage(true);
        fire.setStartColor(new ColorRGBA(0.95f, 0.150f, 0.0f, 1.0f));
        fire.setEndColor(new ColorRGBA(1.0f, 1.0f, 0.0f, 0.5f));
        fire.getParticleInfluencer().setInitialVelocity(Vector3f.ZERO);
        fire.setStartSize(6.5f);
        fire.setEndSize(0.5f);
        fire.setGravity(Vector3f.ZERO);
        fire.setLowLife(0.2f);
        fire.setHighLife(0.3f);
        fire.setParticlesPerSec(40);
        fire.getParticleInfluencer().setVelocityVariation(0.5f);
        fire.setRandomAngle(true);
        return fire;
    }

    public Node build() {
        Sphere sphere = new Sphere(32, 32, 1.0f);

        Geometry projectileGeom = new Geometry("projectile-geom", sphere);
        Node node = new Node("projectile");
        node.attachChild(projectileGeom);

        // TODO: Give at least bit better material
        Material material = new Material(NodeBuilder.assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", ColorRGBA.Yellow);
        node.setMaterial(material);

        node.setUserData(UserDataStrings.SPEED_MOVEMENT, 70f);
        node.setUserData(UserDataStrings.MASS, 30f);
        node.setUserData(UserDataStrings.DAMAGE, 150f);
        node.setUserData(UserDataStrings.IMPULSE_FACTOR, 10000f);

        if (NodeBuilder.worldManager.isClient()) {
            final ParticleEmitter fire = this.createFireEmitter();

            node.attachChild(fire);

            node.addControl(new EntityEventControl());
            /**
             * Here we specify what happens on client side when fireball is
             * removed. In this case we want explosion effect.
             */
            FireballRemovalAction removalAction = new FireballRemovalAction();
            removalAction.setEmitter(fire);


            node.getControl(EntityEventControl.class).setOnRemoval(removalAction);
        }

        SphereCollisionShape collisionShape = new SphereCollisionShape(5.0f);
        RigidBodyControl physicsBody = new RigidBodyControl(collisionShape, (Float) node.getUserData(UserDataStrings.MASS));
        /**
         * We don't want projectiles to collide with each other so we give them
         * their own collision group and prevent them from colliding with that
         * group.
         */
        physicsBody.setCollisionGroup(RigidBodyControl.COLLISION_GROUP_16);
        physicsBody.removeCollideWithGroup(RigidBodyControl.COLLISION_GROUP_16);

        /**
         * Add collision group of characters
         */
        physicsBody.addCollideWithGroup(RigidBodyControl.COLLISION_GROUP_02);

        node.addControl(physicsBody);

        node.addControl(new ProjectileControl());
        SpellBuffControl buffControl = new SpellBuffControl();
        node.addControl(buffControl);

//                node.getControl(RigidBodyControl.class).setGravity(Vector3f.ZERO);

        return node;
    }
}

class FireballRemovalAction implements RemovalEventAction {

    private ParticleEmitter fire;

    public void setEmitter(ParticleEmitter fire) {
        this.fire = fire;
    }

    public void exec(WorldManager worldManager, String reason) {
        if (!"collision".equals(reason)) {
            return;
        }
        Vector3f worldTranslation = fire.getParent().getLocalTranslation();
        fire.removeFromParent();
        worldManager.getWorldRoot().attachChild(fire);
        fire.setLocalTranslation(worldTranslation);
        fire.addControl(new TimedExistenceControl(0.3f));
        fire.getParticleInfluencer().setInitialVelocity(Vector3f.UNIT_X.mult(15.0f));
        fire.setShape(new EmitterSphereShape(Vector3f.ZERO, 6.0f));
        fire.emitAllParticles();
        fire.setParticlesPerSec(0.0f);

        // TODO: Add soundeffect too!
    }
}
