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

import arkhados.CollisionGroups;
import arkhados.WorldManager;
import arkhados.actions.EntityAction;
import arkhados.actions.castspellactions.CastProjectileAction;
import arkhados.controls.EntityEventControl;
import arkhados.controls.InfluenceInterfaceControl;
import arkhados.controls.ProjectileControl;
import arkhados.controls.SpellBuffControl;
import arkhados.controls.TimedExistenceControl;
import arkhados.entityevents.RemovalEventAction;
import arkhados.spell.CastSpellActionBuilder;
import arkhados.spell.Spell;
import arkhados.spell.buffs.AbstractBuff;
import arkhados.spell.buffs.DamageOverTimeBuff;
import arkhados.util.AbstractNodeBuilder;
import arkhados.util.RemovalReasons;
import arkhados.util.UserDataStrings;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.GhostControl;
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
 * Embermage's Fireball (M1) spell. Projectile has moderate speed and deals
 * moderate damage. Has small knockback effect on hit.
 */
public class Fireball extends Spell {

    {
        iconName = "fireball.png";
    }

    public Fireball(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Spell create() {
        final float cooldown = 1.0f;
        final float range = 80f;
        final float castTime = 0.37f;

        final Fireball spell = new Fireball("Fireball", cooldown, range, castTime);

        spell.castSpellActionBuilder = new CastSpellActionBuilder() {
            @Override
            public EntityAction newAction(Node caster, Vector3f location) {
                final CastProjectileAction castProjectile = new CastProjectileAction(spell, Spell.worldManager);
                DamageOverTimeBuff ignite = Ignite.ifNotCooldownCreateDamageOverTimeBuff(caster);
                if (ignite != null) {
                    castProjectile.addBuff(ignite);
                }
                return castProjectile;
            }
        };

        spell.nodeBuilder = new FireballBuilder();

        return spell;
    }
}

class FireballBuilder extends AbstractNodeBuilder {

    private ParticleEmitter createSmokeEmitter() {
        ParticleEmitter smoke = new ParticleEmitter("smoke-emitter",
                ParticleMesh.Type.Triangle, 300);
        Material materialGray = new Material(assetManager,
                "Common/MatDefs/Misc/Particle.j3md");
        materialGray.setTexture("Texture",
                assetManager.loadTexture("Effects/flame.png"));
        smoke.setMaterial(materialGray);
        smoke.setImagesX(2);
        smoke.setImagesY(2);
        smoke.setSelectRandomImage(true);
        smoke.setStartColor(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
        smoke.setStartColor(new ColorRGBA(0.5f, 0.5f, 0.5f, 0.1f));
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
        fire.setStartSize(2.5f);
        fire.setEndSize(1.0f);
        fire.setGravity(Vector3f.ZERO);
        fire.setLowLife(0.1f);
        fire.setHighLife(0.1f);
        fire.setParticlesPerSec(100);

        fire.setRandomAngle(true);
        return fire;
    }

    @Override
    public Node build(Object location) {
        Sphere sphere = new Sphere(32, 32, 1.0f);

        Geometry projectileGeom = new Geometry("projectile-geom", sphere);
        projectileGeom.setCullHint(Spatial.CullHint.Always);

        Node node = new Node("projectile");
        node.setLocalTranslation((Vector3f) location);
        node.attachChild(projectileGeom);

        // TODO: Give at least bit better material
        Material material =
                new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", ColorRGBA.Yellow);
        node.setMaterial(material);

        node.setUserData(UserDataStrings.SPEED_MOVEMENT, 140f);
        node.setUserData(UserDataStrings.MASS, 0.30f);
        node.setUserData(UserDataStrings.DAMAGE, 150f);
        node.setUserData(UserDataStrings.IMPULSE_FACTOR, 0f);

        if (worldManager.isClient()) {
            ParticleEmitter fire = createFireEmitter();
            node.attachChild(fire);

            ParticleEmitter smoke = createSmokeEmitter();
            node.attachChild(smoke);

            node.addControl(new EntityEventControl());
            /**
             * Here we specify what happens on client side when fireball is
             * removed. In this case we want explosion effect.
             */
            FireballRemovalAction removalAction =
                    new FireballRemovalAction(assetManager);
            removalAction.setFireEmitter(fire);
            removalAction.setSmokeTrail(smoke);


            node.getControl(EntityEventControl.class)
                    .setOnRemoval(removalAction);
        }

        SphereCollisionShape collisionShape = new SphereCollisionShape(3);
        RigidBodyControl physicsBody = new RigidBodyControl(collisionShape,
                (float) node.getUserData(UserDataStrings.MASS));
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
        GhostControl characterCollision = new GhostControl(collisionShape);
        characterCollision.setCollideWithGroups(CollisionGroups.CHARACTERS);
        characterCollision.setCollisionGroup(CollisionGroups.PROJECTILES);
        node.addControl(characterCollision);

        node.addControl(physicsBody);

        node.addControl(new ProjectileControl());
        SpellBuffControl buffControl = new SpellBuffControl();
        node.addControl(buffControl);
        buffControl.addBuff(new BrimstoneBuff(-1, 8f));

        return node;
    }
}

class FireballRemovalAction implements RemovalEventAction {

    private ParticleEmitter fire;
    private ParticleEmitter smokeTrail;
    private AssetManager assetManager;
    private AudioNode sound;

    public FireballRemovalAction(AssetManager assetManager) {
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
        smokeTrail.addControl(new TimedExistenceControl(5f));
    }

    private void createSmokePuff( Node worldRoot, Vector3f worldTranslation) {
        ParticleEmitter smokePuff = new ParticleEmitter("smoke-puff",
                ParticleMesh.Type.Triangle, 20);
        Material materialGray = new Material(assetManager,
                "Common/MatDefs/Misc/Particle.j3md");
        materialGray.setTexture("Texture",
                assetManager.loadTexture("Effects/flame.png"));
        smokePuff.setMaterial(materialGray);
        smokePuff.setImagesX(2);
        smokePuff.setImagesY(2);
        smokePuff.setSelectRandomImage(true);
        smokePuff.setStartColor(new ColorRGBA(0.5f, 0.5f, 0.5f, 0.2f));
        smokePuff.setStartColor(new ColorRGBA(0.5f, 0.5f, 0.5f, 0.1f));

        smokePuff.getParticleInfluencer()
                .setInitialVelocity(Vector3f.UNIT_X.mult(5.0f));
        smokePuff.getParticleInfluencer().setVelocityVariation(1f);

        smokePuff.setStartSize(2.0f);
        smokePuff.setEndSize(6.0f);
        smokePuff.setGravity(Vector3f.ZERO);
        smokePuff.setLowLife(0.75f);
        smokePuff.setHighLife(1f);
        smokePuff.setParticlesPerSec(0);

        smokePuff.setRandomAngle(true);

        smokePuff.setShape(new EmitterSphereShape(Vector3f.ZERO, 4.0f));
        worldRoot.attachChild(smokePuff);
        smokePuff.setLocalTranslation(worldTranslation);
        smokePuff.emitAllParticles();
    }

    @Override
    public void exec(WorldManager worldManager, int reason) {
        if (reason == RemovalReasons.DISAPPEARED) {
            return;
        }
        
        Vector3f worldTranslation = fire.getParent().getLocalTranslation();
        leaveSmokeTrail(worldManager.getWorldRoot(), worldTranslation);
        createSmokePuff(worldManager.getWorldRoot(), worldTranslation);

        fire.removeFromParent();
        worldManager.getWorldRoot().attachChild(fire);
        fire.setLocalTranslation(worldTranslation);
        fire.addControl(new TimedExistenceControl(1f));

        fire.setStartColor(new ColorRGBA(0.95f, 0.150f, 0.0f, 0.40f));
        fire.setEndColor(new ColorRGBA(1.0f, 1.0f, 0.0f, 0.0f));
        fire.setLowLife(0.1f);
        fire.setHighLife(0.3f);
        fire.setNumParticles(100);
        fire.setStartSize(0.50f);
        fire.setEndSize(3.0f);
        fire.getParticleInfluencer()
                .setInitialVelocity(Vector3f.UNIT_X.mult(15.0f));
        fire.getParticleInfluencer().setVelocityVariation(1f);

        fire.setShape(new EmitterSphereShape(Vector3f.ZERO, 2.0f));
        fire.emitAllParticles();
        fire.setParticlesPerSec(0.0f);

        sound.setLocalTranslation(worldTranslation);
        sound.play();
    }

    public void setSmokeTrail(ParticleEmitter smoke) {
        smokeTrail = smoke;
    }
}

class BrimstoneBuff extends AbstractBuff {

    private static float maxDuration;
    private static final int stackCap = 3;
    private int stacks = 0;

    public BrimstoneBuff(int buffGroupId, float duration) {
        super(buffGroupId, duration);
        maxDuration = duration;
    }

    @Override
    public void attachToCharacter(InfluenceInterfaceControl targetInterface) {

        BrimstoneBuff existingBrimstone = null;
        for (AbstractBuff buff : targetInterface.getBuffs()) {
            if (buff instanceof BrimstoneBuff) {
                existingBrimstone = (BrimstoneBuff) buff;
                break;
            }
        }

        if (existingBrimstone != null) {
            existingBrimstone.duration = maxDuration;
            if (existingBrimstone.stacks < stackCap) {
                existingBrimstone.stacks++;
            }
        } else {
            super.attachToCharacter(targetInterface);
        }
    }

    public int getStacks() {
        return stacks;
    }
}