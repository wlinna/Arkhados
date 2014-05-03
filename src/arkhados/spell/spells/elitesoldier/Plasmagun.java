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
package arkhados.spell.spells.elitesoldier;

import arkhados.WorldManager;
import arkhados.actions.DelayAction;
import arkhados.actions.EntityAction;
import arkhados.actions.SplashAction;
import arkhados.actions.castspellactions.CastProjectileAction;
import arkhados.controls.ActionQueueControl;
import arkhados.controls.EntityEventControl;
import arkhados.controls.ProjectileControl;
import arkhados.controls.SpellBuffControl;
import arkhados.controls.TimedExistenceControl;
import arkhados.entityevents.RemovalEventAction;
import arkhados.spell.CastSpellActionBuilder;
import arkhados.spell.Spell;
import arkhados.spell.buffs.SlowCC;
import arkhados.util.DistanceScaling;
import arkhados.util.NodeBuilder;
import arkhados.util.UserDataStrings;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioNode;
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
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;

/**
 * EliteSoldiers's Plasmagun (Q) spell. Projectile has moderate speed and deals
 * moderate damage and small amout of splash damage. Slows on hit.
 */
public class Plasmagun extends Spell {

    {
        this.iconName = "plasma.png";
    }

    public Plasmagun(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Spell create() {
        final float cooldown = 4f;
        final float range = 80f;
        final float castTime = 0.5f;

        final Plasmagun spell = new Plasmagun("Plasmagun", cooldown, range, castTime);

        spell.castSpellActionBuilder = new CastSpellActionBuilder() {
            public EntityAction newAction(Node caster, Vector3f location) {
                final TripleShootAction shoot = new TripleShootAction(spell, Spell.worldManager);
                return shoot;
            }
        };

        spell.nodeBuilder = new PlasmaBuilder();

        return spell;
    }
}

class TripleShootAction extends EntityAction {

    private Spell spell;
    private WorldManager worldManager;

    TripleShootAction(Plasmagun spell, WorldManager worldManager) {
        this.spell = spell;
        this.worldManager = worldManager;
    }

    @Override
    public boolean update(float tpf) {
        final ActionQueueControl queue = super.spatial.getControl(ActionQueueControl.class);
        final float delayTime = 0.12f;

        final CastProjectileAction plasma1 = new CastProjectileAction(spell, this.worldManager);
        final DelayAction delay2 = new DelayAction(delayTime);
        final CastProjectileAction plasma2 = new CastProjectileAction(spell, this.worldManager);
        final DelayAction delay3 = new DelayAction(delayTime);
        final CastProjectileAction plasma3 = new CastProjectileAction(spell, this.worldManager);

        queue.enqueueAction(plasma1);
        queue.enqueueAction(delay2);
        queue.enqueueAction(plasma2);
        queue.enqueueAction(delay3);
        queue.enqueueAction(plasma3);

        return false;
    }
}

class PlasmaBuilder extends NodeBuilder {

    private ParticleEmitter createPlasmaEmitter() {
        final ParticleEmitter plasma = new ParticleEmitter("plasma-emitter", ParticleMesh.Type.Triangle, 200);
        Material materialRed = new Material(NodeBuilder.assetManager, "Common/MatDefs/Misc/Particle.j3md");
        materialRed.setTexture("Texture", NodeBuilder.assetManager.loadTexture("Effects/plasma-particle.png"));
        plasma.setMaterial(materialRed);
        plasma.setImagesX(2);
        plasma.setImagesY(2);
        plasma.setSelectRandomImage(true);
        plasma.setStartColor(new ColorRGBA(0.8f, 0.350f, 0.9f, 1.0f));
        plasma.setEndColor(new ColorRGBA(0.80f, 0.30f, 0.9f, 0.95f));        
        plasma.getParticleInfluencer().setInitialVelocity(Vector3f.ZERO);
        plasma.setStartSize(5.5f);
        plasma.setEndSize(4.5f);
        plasma.setGravity(Vector3f.ZERO);
        plasma.setLowLife(0.05f);
        plasma.setHighLife(0.05f);
        plasma.setParticlesPerSec(100);

        plasma.setRandomAngle(true);
        return plasma;
    }

    @Override
    public Node build() {
        final Sphere sphere = new Sphere(32, 32, 1.0f);

        final Geometry projectileGeom = new Geometry("projectile-geom", sphere);
        projectileGeom.setCullHint(Spatial.CullHint.Always);

        final Node node = new Node("projectile");
        node.attachChild(projectileGeom);

        final Material material = new Material(NodeBuilder.assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", ColorRGBA.Yellow);
        node.setMaterial(material);

        node.setUserData(UserDataStrings.SPEED_MOVEMENT, 140f);
        node.setUserData(UserDataStrings.MASS, 0.30f);
        node.setUserData(UserDataStrings.DAMAGE, 70f);
        node.setUserData(UserDataStrings.IMPULSE_FACTOR, 0f);

        if (NodeBuilder.worldManager.isClient()) {
            final ParticleEmitter plasma = this.createPlasmaEmitter();
            node.attachChild(plasma);


            node.addControl(new EntityEventControl());
            /**
             * Here we specify what happens on client side when plasmaball is
             * removed. In this case we want explosion effect.
             */
            final PlasmaRemovalAction removalAction = new PlasmaRemovalAction(assetManager);
            removalAction.setPlasmaEmitter(plasma);


            node.getControl(EntityEventControl.class).setOnRemoval(removalAction);
        }

        final SphereCollisionShape collisionShape = new SphereCollisionShape(8.0f);
        final RigidBodyControl physicsBody = new RigidBodyControl(collisionShape, (Float) node.getUserData(UserDataStrings.MASS));
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

        final ProjectileControl projectileControl = new ProjectileControl();        
        final SplashAction splash = new SplashAction(23f, 20f, DistanceScaling.CONSTANT, null);
        splash.setSpatial(node);
        projectileControl.setSplashAction(splash);
        node.addControl(projectileControl);
        final SpellBuffControl buffControl = new SpellBuffControl();
        node.addControl(buffControl);
        
        buffControl.addBuff(new SlowCC(-1, 1f, 0.3f));
        return node;
    }
}

class PlasmaRemovalAction implements RemovalEventAction {
    private ParticleEmitter plasma;    
    private AudioNode sound;

    public PlasmaRemovalAction(AssetManager assetManager) {
        this.sound = new AudioNode(assetManager, "Effects/Sound/FireballExplosion.wav");
        this.sound.setPositional(true);
        this.sound.setReverbEnabled(false);
        this.sound.setVolume(1f);
    }

    public void setPlasmaEmitter(ParticleEmitter plasma) {
        this.plasma = plasma;
    }   
    
    @Override
    public void exec(WorldManager worldManager, String reason) {
        Vector3f worldTranslation = plasma.getParent().getLocalTranslation();

        plasma.removeFromParent();
        worldManager.getWorldRoot().attachChild(plasma);
        plasma.setLocalTranslation(worldTranslation);
        plasma.addControl(new TimedExistenceControl(1f));
        plasma.setStartColor(new ColorRGBA(0.5f, 0.150f, 0.9f, 1.0f));
        plasma.setEndColor(new ColorRGBA(0.60f, 0.10f, 0.9f, 0.8f));
        plasma.setLowLife(0.1f);
        plasma.setHighLife(0.3f);
        plasma.setNumParticles(15);
        plasma.setStartSize(5.5f);
        plasma.setEndSize(10.0f);
        plasma.getParticleInfluencer().setInitialVelocity(Vector3f.UNIT_X.mult(.0f));
        plasma.getParticleInfluencer().setVelocityVariation(1f);

        plasma.setShape(new EmitterSphereShape(Vector3f.ZERO, 2.0f));
        plasma.emitAllParticles();
        plasma.setParticlesPerSec(0.0f);

        this.sound.setLocalTranslation(worldTranslation);
        this.sound.play();
    }
}