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

import arkhados.CollisionGroups;
import arkhados.World;
import arkhados.actions.AChannelingSpell;
import arkhados.actions.EntityAction;
import arkhados.actions.ASplash;
import arkhados.actions.cast.ACastProjectile;
import arkhados.characters.EliteSoldier;
import arkhados.controls.CEntityEvent;
import arkhados.controls.CProjectile;
import arkhados.controls.CSpellBuff;
import arkhados.controls.CTimedExistence;
import arkhados.entityevents.ARemovalEvent;
import arkhados.spell.CastSpellActionBuilder;
import arkhados.spell.Spell;
import arkhados.spell.buffs.SlowCC;
import arkhados.util.DistanceScaling;
import arkhados.util.AbstractNodeBuilder;
import arkhados.util.BuildParameters;
import arkhados.util.UserData;
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
    public static final float COOLDOWN = 1.5f;
    public static final float RANGE = 80f;
    public static final float CAST_TIME = 0.4f;

    {
        iconName = "plasma.png";
        setMoveTowardsTarget(false);
    }

    public Plasmagun(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Spell create() {
        final Plasmagun spell =
                new Plasmagun("Plasmagun", COOLDOWN, RANGE, CAST_TIME);

        spell.castSpellActionBuilder = new CastSpellActionBuilder() {
            @Override
            public EntityAction newAction(Node caster, Vector3f location) {
                ACastProjectile projectileAction = 
                        new ACastProjectile(spell, world);
                projectileAction.setTypeId(EliteSoldier.ACTION_PLASMAGUN);
                AChannelingSpell channel = new AChannelingSpell(spell,
                        3, 0.12f, projectileAction, true);
                return channel;
            }
        };

        spell.nodeBuilder = new PlasmaBuilder();

        return spell;
    }
}

class PlasmaBuilder extends AbstractNodeBuilder {

    private ParticleEmitter createPlasmaEmitter() {
        ParticleEmitter plasma = new ParticleEmitter("plasma-emitter",
                ParticleMesh.Type.Triangle, 200);
        Material materialRed = new Material(assetManager,
                "Common/MatDefs/Misc/Particle.j3md");
        materialRed.setTexture("Texture",
                assetManager.loadTexture("Effects/plasma-particle.png"));
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
    public Node build(BuildParameters params) {
        Sphere sphere = new Sphere(32, 32, 1.0f);

        Geometry projectileGeom = new Geometry("projectile-geom", sphere);
        projectileGeom.setCullHint(Spatial.CullHint.Always);

        Node node = new Node("projectile");
        node.setLocalTranslation(params.location);
        node.attachChild(projectileGeom);

        Material material = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", ColorRGBA.Yellow);
        node.setMaterial(material);

        node.setUserData(UserData.SPEED_MOVEMENT, 140f);
        node.setUserData(UserData.MASS, 0.30f);
        node.setUserData(UserData.DAMAGE, 60f);
        node.setUserData(UserData.IMPULSE_FACTOR, 0f);

        if (world.isClient()) {
            ParticleEmitter plasma = createPlasmaEmitter();
            node.attachChild(plasma);

            node.addControl(new CEntityEvent());
            /**
             * Here we specify what happens on client side when plasmaball is
             * removed. In this case we want explosion effect.
             */
            APlasmaRemoval removalAction =
                    new APlasmaRemoval(assetManager);
            removalAction.setPlasmaEmitter(plasma);

            node.getControl(CEntityEvent.class)
                    .setOnRemoval(removalAction);
        }

        SphereCollisionShape collisionShape = new SphereCollisionShape(5);
        RigidBodyControl physicsBody = new RigidBodyControl(collisionShape,
                (float) node.getUserData(UserData.MASS));
        /**
         * We don't want projectiles to collide with each other so we give them
         * their own collision group and prevent them from colliding with that
         * group.
         */
        physicsBody.setCollisionGroup(CollisionGroups.PROJECTILES);
        physicsBody.removeCollideWithGroup(CollisionGroups.PROJECTILES);

        /**
         * Add collision group of characters
         */
        physicsBody.addCollideWithGroup(CollisionGroups.CHARACTERS);

        node.addControl(physicsBody);

        CProjectile projectileControl = new CProjectile();
        ASplash splash = new ASplash(23f, 23f,
                DistanceScaling.CONSTANT, null);
        splash.setSpatial(node);
        projectileControl.setSplashAction(splash);
        node.addControl(projectileControl);
        CSpellBuff buffControl = new CSpellBuff();
        node.addControl(buffControl);

        buffControl.addBuff(new SlowCC.MyBuilder(1f, 0.3f));
        return node;
    }
}

class APlasmaRemoval implements ARemovalEvent {
    private ParticleEmitter plasma;
    private AudioNode sound;

    public APlasmaRemoval(AssetManager assetManager) {
        sound = new AudioNode(assetManager,
                "Effects/Sound/FireballExplosion.wav");
        sound.setPositional(true);
        sound.setReverbEnabled(false);
        sound.setVolume(1f);
    }

    public void setPlasmaEmitter(ParticleEmitter plasma) {
        this.plasma = plasma;
    }

    @Override
    public void exec(World world, int reason) {
        Vector3f worldTranslation = plasma.getParent().getLocalTranslation();

        plasma.removeFromParent();
        world.getWorldRoot().attachChild(plasma);
        plasma.setLocalTranslation(worldTranslation);
        plasma.addControl(new CTimedExistence(1f));
        plasma.setStartColor(new ColorRGBA(0.5f, 0.150f, 0.9f, 1.0f));
        plasma.setEndColor(new ColorRGBA(0.60f, 0.10f, 0.9f, 0.8f));
        plasma.setLowLife(0.1f);
        plasma.setHighLife(0.3f);
        plasma.setNumParticles(15);
        plasma.setStartSize(5.5f);
        plasma.setEndSize(10.0f);
        plasma.getParticleInfluencer()
                .setInitialVelocity(Vector3f.UNIT_X.mult(.0f));
        plasma.getParticleInfluencer().setVelocityVariation(1f);

        plasma.setShape(new EmitterSphereShape(Vector3f.ZERO, 2.0f));
        plasma.emitAllParticles();
        plasma.setParticlesPerSec(0.0f);

        sound.setLocalTranslation(worldTranslation);
        sound.play();
    }
}