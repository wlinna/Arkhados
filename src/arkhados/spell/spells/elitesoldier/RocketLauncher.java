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
import arkhados.Globals;
import arkhados.WorldManager;
import arkhados.actions.EntityAction;
import arkhados.actions.ASplash;
import arkhados.actions.castspellactions.ACastProjectile;
import arkhados.characters.EliteSoldier;
import arkhados.controls.CEntityEvent;
import arkhados.controls.CProjectile;
import arkhados.controls.CSpellBuff;
import arkhados.controls.CTimedExistence;
import arkhados.entityevents.ARemovalEvent;
import arkhados.spell.CastSpellActionBuilder;
import arkhados.spell.Spell;
import arkhados.util.DistanceScaling;
import arkhados.util.AbstractNodeBuilder;
import arkhados.util.BuildParameters;
import arkhados.util.UserDataStrings;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.shapes.EmitterSphereShape;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;

/**
 * EliteSoldiers's RocketLauncher (E) spell. Projectile has moderate speed and
 * deals high damage and small splash damage. Has big knockback effect on hit.
 */
public class RocketLauncher extends Spell {

    public static final float SPLASH_RADIUS = 25f;

    {
        iconName = "rocket_launcher.png";
        setMoveTowardsTarget(false);
    }

    public RocketLauncher(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Spell create() {
        final float cooldown = 1.5f;
        final float range = 80f;
        final float castTime = 0.4f;

        final RocketLauncher spell = new RocketLauncher("Rocket Launcher",
                cooldown, range, castTime);

        spell.castSpellActionBuilder = new CastSpellActionBuilder() {
            @Override
            public EntityAction newAction(Node caster, Vector3f location) {
                ACastProjectile castProjectile =
                        new ACastProjectile(spell, worldManager);
                castProjectile.setTypeId(EliteSoldier.ACTION_ROCKET_LAUNCHER);
                return castProjectile;
            }
        };

        spell.nodeBuilder = new RocketBuilder();

        return spell;
    }
}

class RocketBuilder extends AbstractNodeBuilder {

    private ParticleEmitter createSmokeEmitter() {
        ParticleEmitter smoke = new ParticleEmitter("smoke-emitter",
                ParticleMesh.Type.Triangle, 300);
        Material material = new Material(assetManager,
                "Common/MatDefs/Misc/Particle.j3md");
        material.setTexture("Texture",
                assetManager.loadTexture("Effects/flame_alpha.png"));
        material.getAdditionalRenderState()
                .setBlendMode(RenderState.BlendMode.Alpha);
        smoke.setMaterial(material);
        smoke.setImagesX(2);
        smoke.setImagesY(2);
        smoke.setSelectRandomImage(true);
        smoke.setStartColor(new ColorRGBA(0.4f, 0.4f, 0.4f, 1.0f));
        smoke.setStartColor(new ColorRGBA(0.4f, 0.4f, 0.4f, 0.1f));
        smoke.getParticleInfluencer().setInitialVelocity(Vector3f.ZERO);
//        fire.getParticleInfluencer().setInitialVelocity(Vector3f.UNIT_Z.mult(10));
//        fire.getParticleInfluencer().setVelocityVariation(0.5f);
        smoke.setStartSize(2.0f);
        smoke.setEndSize(6.0f);
        smoke.setGravity(Vector3f.ZERO);
        smoke.setLowLife(1f);
        smoke.setHighLife(1.3f);
        smoke.setParticlesPerSec(100);

        smoke.setRandomAngle(true);
        return smoke;
    }

    private ParticleEmitter createSmokePuff() {
        ParticleEmitter smokePuff = createSmokeEmitter();
        smokePuff.setParticlesPerSec(0);

        smokePuff.getParticleInfluencer()
                .setInitialVelocity(Vector3f.UNIT_Y.mult(15f));
        smokePuff.getParticleInfluencer().setVelocityVariation(0.6f);

        smokePuff.setShape(new EmitterSphereShape(Vector3f.ZERO, 3f));

        return smokePuff;
    }

    private ParticleEmitter createFireEmitter() {
        ParticleEmitter fire = new ParticleEmitter("fire-emitter",
                ParticleMesh.Type.Triangle, 200);
        Material materialRed = new Material(assetManager,
                "Common/MatDefs/Misc/Particle.j3md");
        materialRed.setTexture("Texture",
                assetManager.loadTexture("Effects/flame.png"));
        fire.setMaterial(materialRed);
        fire.setImagesX(2);
        fire.setImagesY(2);
        fire.setSelectRandomImage(true);
        fire.setStartColor(new ColorRGBA(0.95f, 0.150f, 0.0f, 1.0f));
        fire.setEndColor(new ColorRGBA(1.0f, 1.0f, 0.0f, 0.5f));
        fire.getParticleInfluencer().setInitialVelocity(Vector3f.ZERO);
//        fire.getParticleInfluencer().setInitialVelocity(Vector3f.UNIT_Z.mult(10));
//        fire.getParticleInfluencer().setVelocityVariation(0.5f);
        fire.setStartSize(7.5f);
        fire.setEndSize(1.5f);
        fire.setGravity(Vector3f.ZERO);
        fire.setLowLife(0.1f);
        fire.setHighLife(0.1f);
        fire.setParticlesPerSec(100);

        fire.setRandomAngle(true);
        return fire;
    }

    @Override
    public Node build(BuildParameters params) {
        Sphere sphere = new Sphere(32, 32, 1);

        Geometry projectileGeom = new Geometry("projectile-geom", sphere);
        projectileGeom.setCullHint(Spatial.CullHint.Always);

        Node node = new Node("projectile");
        node.setLocalTranslation(params.location);
        node.attachChild(projectileGeom);

        // TODO: Give at least bit better material
        Material material = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", ColorRGBA.Yellow);
        node.setMaterial(material);

        node.setUserData(UserDataStrings.SPEED_MOVEMENT, 140f);
        node.setUserData(UserDataStrings.MASS, 0.30f);
        node.setUserData(UserDataStrings.DAMAGE, 180f);
        node.setUserData(UserDataStrings.IMPULSE_FACTOR, 23000f);

        if (worldManager.isClient()) {
            ParticleEmitter fire = createFireEmitter();
            node.attachChild(fire);

            ParticleEmitter smokeTrail = createSmokeEmitter();
            node.attachChild(smokeTrail);

            ParticleEmitter smokePuff = createSmokePuff();
            worldManager.getWorldRoot().attachChild(smokePuff);
            smokePuff.setLocalTranslation(params.location);
            smokePuff.addControl(new CTimedExistence(5f));
            smokePuff.emitAllParticles();

            node.addControl(new CEntityEvent());
            /**
             * Here we specify what happens on client side when fireball is
             * removed. In this case we want explosion effect.
             */
            ARocketRemoval removalAction =
                    new ARocketRemoval(assetManager);
            removalAction.setFireEmitter(fire);
            removalAction.setSmokeTrail(smokeTrail);


            node.getControl(CEntityEvent.class)
                    .setOnRemoval(removalAction);
        }

        SphereCollisionShape collisionShape = new SphereCollisionShape(6f);
        RigidBodyControl physicsBody = new RigidBodyControl(collisionShape,
                (float) node.getUserData(UserDataStrings.MASS));
        /**
         * We don't want projectiles to collide with each other so we give them
         * their own collision group and prevent them from colliding with that
         * group.
         */
        physicsBody.setCollisionGroup(CollisionGroups.PROJECTILES);
        physicsBody.setCollideWithGroups(CollisionGroups.NONE);

        /**
         * Add collision group of characters
         */
        physicsBody.addCollideWithGroup(CollisionGroups.CHARACTERS);

        node.addControl(physicsBody);

        CProjectile projectileControl = new CProjectile();

        ASplash splash =
                new ASplash(25f, 120f, DistanceScaling.LINEAR, null);
        splash.setSpatial(node);
        projectileControl.setSplashAction(splash);

        node.addControl(projectileControl);
        final CSpellBuff buffControl = new CSpellBuff();
        node.addControl(buffControl);
        return node;
    }
}

class ARocketRemoval implements ARemovalEvent {

    private ParticleEmitter fire;
    private ParticleEmitter smokeTrail;
    private AssetManager assetManager;
    private AudioNode sound;

    public ARocketRemoval(AssetManager assetManager) {
        this.assetManager = assetManager;
        sound = new AudioNode(assetManager,
                "Effects/Sound/FireballExplosion.wav");
        sound.setPositional(true);
        sound.setReverbEnabled(false);
        sound.setVolume(1f);
    }

    public void setFireEmitter(ParticleEmitter fire) {
        this.fire = fire;
    }

    private void leaveSmokeTrail(Node worldRoot, Vector3f worldTranslation) {
        smokeTrail.setParticlesPerSec(0);
        worldRoot.attachChild(smokeTrail);
        smokeTrail.setLocalTranslation(worldTranslation);
        smokeTrail.addControl(new CTimedExistence(5f));
    }

    private ParticleEmitter createShockwave() {
        ParticleEmitter wave = new ParticleEmitter("shockwave-emitter",
                ParticleMesh.Type.Triangle, 2);
        Material materialRed = new Material(Globals.assetManager,
                "Common/MatDefs/Misc/Particle.j3md");
        materialRed.setTexture("Texture",
                Globals.assetManager.loadTexture("Effects/shockwave.png"));
        wave.setMaterial(materialRed);
        wave.setImagesX(1);
        wave.setImagesY(1);

        wave.setGravity(Vector3f.ZERO);

        wave.setStartColor(new ColorRGBA(0.7f, 0.7f, 0.7f, 1f));
        wave.setEndColor(new ColorRGBA(0.7f, 0.7f, 0.7f, 0f));
        wave.setLowLife(0.5f);
        wave.setHighLife(0.5f);
        wave.setStartSize(0.50f);
        wave.setEndSize(RocketLauncher.SPLASH_RADIUS + 7f);
        wave.getParticleInfluencer().setInitialVelocity(Vector3f.ZERO);
        wave.getParticleInfluencer().setVelocityVariation(0f);
        wave.setParticlesPerSec(0f);

        return wave;
    }

    private void createSmokePuff(Node worldRoot, Vector3f worldTranslation) {
        ParticleEmitter smokePuff = new ParticleEmitter("smoke-puff",
                ParticleMesh.Type.Triangle, 20);
        Material material = new Material(assetManager,
                "Common/MatDefs/Misc/Particle.j3md");
        material.setTexture("Texture",
                assetManager.loadTexture("Effects/flame_alpha.png"));
        material.getAdditionalRenderState()
                .setBlendMode(RenderState.BlendMode.Alpha);
        smokePuff.setMaterial(material);
        smokePuff.setImagesX(2);
        smokePuff.setImagesY(2);
        smokePuff.setSelectRandomImage(true);
        smokePuff.setStartColor(new ColorRGBA(0.3f, 0.3f, 0.3f, 0.3f));
        smokePuff.setEndColor(new ColorRGBA(0.3f, 0.3f, 0.3f, 0.1f));

        smokePuff.getParticleInfluencer()
                .setInitialVelocity(Vector3f.UNIT_X.mult(5.0f));
        smokePuff.getParticleInfluencer().setVelocityVariation(1f);

        smokePuff.setStartSize(8f);
        smokePuff.setEndSize(30f);
        smokePuff.setGravity(Vector3f.ZERO);
        smokePuff.setLowLife(1.75f);
        smokePuff.setHighLife(6f);
        smokePuff.setParticlesPerSec(0);

        smokePuff.setRandomAngle(true);

        smokePuff.setShape(new EmitterSphereShape(Vector3f.ZERO, 4.0f));
        worldRoot.attachChild(smokePuff);
        smokePuff.setLocalTranslation(worldTranslation);
        smokePuff.emitAllParticles();
    }

    @Override
    public void exec(WorldManager worldManager, int reason) {
        Node node = new Node("rocket-explosion");
        Vector3f worldTranslation = fire.getParent().getLocalTranslation();
        leaveSmokeTrail(node, worldTranslation);
        createSmokePuff(node, worldTranslation);

        fire.removeFromParent();
        node.attachChild(fire);
        node.attachChild(sound);
        node.setLocalTranslation(worldTranslation);
        fire.setLocalTranslation(Vector3f.ZERO);
        worldManager.getWorldRoot().attachChild(node);
        node.addControl(new CTimedExistence(6f));

        fire.setStartColor(new ColorRGBA(0.95f, 0.150f, 0.0f, 0.40f));
        fire.setEndColor(new ColorRGBA(1.0f, 1.0f, 0.0f, 0.0f));
        fire.setLowLife(0.4f);
        fire.setHighLife(0.5f);
        fire.setNumParticles(120);
        fire.setStartSize(7.50f);
        fire.setEndSize(25f);
        fire.getParticleInfluencer()
                .setInitialVelocity(Vector3f.UNIT_X.mult(45.0f));
        fire.getParticleInfluencer().setVelocityVariation(1f);

//        fire.setShape(new EmitterSphereShape(Vector3f.ZERO, 2.0f));
        fire.emitAllParticles();
        fire.setParticlesPerSec(0.0f);

        ParticleEmitter wave = createShockwave();
        worldManager.getWorldRoot().attachChild(wave);
        wave.setLocalTranslation(worldTranslation);
        wave.emitAllParticles();
        wave.addControl(new CTimedExistence(4f));

//        sound.setLocalTranslation(worldTranslation);        
        sound.setVolume(5f);
        sound.play();
    }

    public void setSmokeTrail(ParticleEmitter smoke) {
        this.smokeTrail = smoke;
    }
}
