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
package arkhados.spell.spells.shadowmancer;

import arkhados.spell.spells.embermage.*;
import arkhados.CollisionGroups;
import arkhados.WorldManager;
import arkhados.actions.EntityAction;
import arkhados.actions.castspellactions.ACastProjectile;
import arkhados.controls.CEntityEvent;
import arkhados.controls.CProjectile;
import arkhados.controls.CSpellBuff;
import arkhados.controls.CTimedExistence;
import arkhados.entityevents.ARemovalEvent;
import arkhados.spell.CastSpellActionBuilder;
import arkhados.spell.Spell;
import arkhados.spell.buffs.AbstractBuffBuilder;
import arkhados.spell.buffs.BrimstoneBuff;
import arkhados.spell.buffs.DamageOverTimeBuff;
import arkhados.util.AbstractNodeBuilder;
import arkhados.util.BuildParameters;
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
import com.jme3.scene.shape.Sphere;



/**
 * Shadowmancer's ShadowOrb (M1) spell. Projectile has moderate speed and deals
 * moderate damage.
 */
public class ShadowOrb extends Spell {

    {
        iconName = "fireball.png";
    }

    public ShadowOrb(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Spell create() {
        final float cooldown = 0.8f;
        final float range = 75f;
        final float castTime = 0.37f;

        final ShadowOrb spell =
                new ShadowOrb("Shadow Orb", cooldown, range, castTime);

        spell.castSpellActionBuilder = new CastSpellActionBuilder() {
            @Override
            public EntityAction newAction(Node caster, Vector3f location) {
                ACastProjectile castProjectile =
                        new ACastProjectile(spell, Spell.worldManager);
                AbstractBuffBuilder ignite =
                        Ignite.ifNotCooldownCreateDamageOverTimeBuff(caster);
                if (ignite != null) {
                    castProjectile.addBuff(ignite);
                }
                return castProjectile;
            }
        };

        spell.nodeBuilder = new OrbBuilder();

        return spell;
    }
}
class OrbBuilder extends AbstractNodeBuilder {

    private ParticleEmitter createFireEmitter() {
        ParticleEmitter purple = new ParticleEmitter("fire-emitter",
                ParticleMesh.Type.Triangle, 200);
        Material materialRed = new Material(assetManager,
                "Common/MatDefs/Misc/Particle.j3md");
        materialRed.setTexture("Texture",
                assetManager.loadTexture("Effects/flame.png"));
        purple.setMaterial(materialRed);
        purple.setImagesX(2);
        purple.setImagesY(2);
        purple.setSelectRandomImage(true);
        purple.setStartColor(new ColorRGBA(0.8f, 0.0150f, 0.80f, 1.0f));
        purple.setEndColor(new ColorRGBA(0.8f, 0f, 0.80f, 0.5f));
        purple.getParticleInfluencer().setInitialVelocity(Vector3f.ZERO);
//        fire.getParticleInfluencer().setInitialVelocity(Vector3f.UNIT_Z.mult(10));
//        fire.getParticleInfluencer().setVelocityVariation(0.5f);
        purple.setStartSize(2.5f);
        purple.setEndSize(1.0f);
        purple.setGravity(Vector3f.ZERO);
        purple.setLowLife(0.1f);
        purple.setHighLife(0.1f);
        purple.setParticlesPerSec(100);

        purple.setRandomAngle(true);
        return purple;
    }

    @Override
    public Node build(BuildParameters params) {
        Sphere sphere = new Sphere(32, 32, 1.0f);

        Geometry projectileGeom = new Geometry("projectile-geom", sphere);

        Node node = new Node("projectile");
        node.setLocalTranslation(params.location);
        node.attachChild(projectileGeom);

        // TODO: Give at least bit better material
        Material material =
                new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", ColorRGBA.Black);
        node.setMaterial(material);

        node.setUserData(UserDataStrings.SPEED_MOVEMENT, 150f);
        node.setUserData(UserDataStrings.MASS, 0.30f);
        node.setUserData(UserDataStrings.DAMAGE, 140f);
        node.setUserData(UserDataStrings.IMPULSE_FACTOR, 0f);

        if (worldManager.isClient()) {
            ParticleEmitter fire = createFireEmitter();
            node.attachChild(fire);

//            ParticleEmitter smoke = createSmokeEmitter();
//            node.attachChild(smoke);

            node.addControl(new CEntityEvent());
            /**
             * Here we specify what happens on client side when fireball is
             * removed. In this case we want explosion effect.
             */
            AOrbRemoval removalAction =
                    new AOrbRemoval(assetManager);
            removalAction.setFireEmitter(fire);
//            removalAction.setSmokeTrail(smoke);

            node.getControl(CEntityEvent.class)
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

        node.addControl(new CProjectile());
        CSpellBuff buffControl = new CSpellBuff();
        node.addControl(buffControl);

        return node;
    }
}

class AOrbRemoval implements ARemovalEvent {

    private ParticleEmitter purple;
    private AssetManager assetManager;
    private AudioNode sound;

    public AOrbRemoval(AssetManager assetManager) {
        this.assetManager = assetManager;
        sound = new AudioNode(assetManager,
                "Effects/Sound/FireballExplosion.wav");
        sound.setPositional(true);
        sound.setReverbEnabled(false);
        sound.setVolume(1f);
    }

    public void setFireEmitter(ParticleEmitter fire) {
        this.purple = fire;
    }

//    private void leaveSmokeTrail(Node worldRoot, Vector3f worldTranslation) {
//        smokeTrail.setParticlesPerSec(0);
//        worldRoot.attachChild(smokeTrail);
//        smokeTrail.setLocalTranslation(worldTranslation);
//        smokeTrail.addControl(new CTimedExistence(5f));
//    }

    private void createSmokePuff(Node worldRoot, Vector3f worldTranslation) {
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

        Vector3f worldTranslation = purple.getParent().getLocalTranslation();
//        leaveSmokeTrail(worldManager.getWorldRoot(), worldTranslation);
        createSmokePuff(worldManager.getWorldRoot(), worldTranslation);

        purple.removeFromParent();
        worldManager.getWorldRoot().attachChild(purple);
        purple.setLocalTranslation(worldTranslation);
        purple.addControl(new CTimedExistence(1f));

        purple.setStartColor(new ColorRGBA(0.3f, 0.0150f, 0.3f, 0.80f));
        purple.setEndColor(new ColorRGBA(0f, 0f, 0f, 0f));
        purple.setLowLife(0.1f);
        purple.setHighLife(0.3f);
        purple.setNumParticles(100);
        purple.setStartSize(0.50f);
        purple.setEndSize(3.0f);
        purple.getParticleInfluencer()
                .setInitialVelocity(Vector3f.UNIT_X.mult(15.0f));
        purple.getParticleInfluencer().setVelocityVariation(1f);

        purple.setShape(new EmitterSphereShape(Vector3f.ZERO, 2.0f));
        purple.emitAllParticles();
        purple.setParticlesPerSec(0.0f);

        sound.setLocalTranslation(worldTranslation);
        sound.play();
    }
}
