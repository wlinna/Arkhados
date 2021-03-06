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
package arkhados.spell.spells.electrobot;

import arkhados.CollisionGroups;
import arkhados.World;
import arkhados.actions.cast.ACastProjectile;
import arkhados.characters.ElectroBot;
import arkhados.controls.CEntityEvent;
import arkhados.controls.CProjectile;
import arkhados.controls.CSpellBuff;
import arkhados.controls.CTimedExistence;
import arkhados.effects.ParticleInfluencerWithAngleSetting;
import com.jme3.effect.ParticleEmitter;
import arkhados.entityevents.ARemovalEvent;
import arkhados.spell.Spell;
import arkhados.spell.buffs.IncapacitateCC;
import arkhados.util.AbstractNodeBuilder;
import arkhados.util.BuildParameters;
import arkhados.util.UserData;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.effect.ParticleMesh;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;

public class Paralyze extends Spell {

    static final float CAST_TIME = 0.45f;

    {
        iconName = "Paralyze.png";
        setMoveTowardsTarget(false);
    }

    public Paralyze(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Spell create() {
        final float cooldown = 7f;
        final float range = 70f;

        Paralyze spell = new Paralyze("Paralyze", cooldown, range, CAST_TIME);

        spell.castSpellActionBuilder = (Node caster, Vector3f vec) -> {
            ACastProjectile action = new ACastProjectile(spell, world);
            action.setTypeId(ElectroBot.ACTION_PARALYZE);
            return action;
        };

        spell.nodeBuilder = new ParalysisBuilder();

        return spell;
    }
}

class ParalysisBuilder extends AbstractNodeBuilder {

    public static class CParticleDirector extends AbstractControl {

        private final ParticleEmitter trail;
        private final Vector3f temp = new Vector3f();
        private final float angles[] = new float[3];

        private final Vector3f previous = new Vector3f();

        public CParticleDirector(ParticleEmitter trail) {
            this.trail = trail;
        }

        @Override
        protected void controlUpdate(float tpf) {
            Vector3f newPos = spatial.getLocalTranslation();
            Vector3f velocity = newPos.subtract(previous, temp);
            trail.lookAt(newPos.add(velocity, temp), Vector3f.UNIT_Y);
            ParticleInfluencerWithAngleSetting influencer
                    = (ParticleInfluencerWithAngleSetting) trail
                    .getParticleInfluencer();

            float angle = spatial.getWorldRotation().toAngles(angles)[1]
                    + FastMath.HALF_PI;

            influencer.setAngle(angle);

            previous.set(newPos);
        }

        @Override
        protected void controlRender(RenderManager rm, ViewPort vp) {
        }

    }

    private ParticleEmitter createTrailEmitter() {
        ParticleEmitter trail = new ParticleEmitter("trail-emitter",
                ParticleMesh.Type.Triangle, 650);
        Material mat
                = new Material(assets, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture", assets.loadTexture("Effects/smoketrail.png"));
        trail.setMaterial(mat);
        trail.setImagesX(1);
        trail.setImagesY(3);
        trail.setSelectRandomImage(true);
        trail.setStartColor(new ColorRGBA(0.3f, 0.3f, 0.9f, 0.6f));
        trail.setParticleInfluencer(new ParticleInfluencerWithAngleSetting());
        trail.getParticleInfluencer().setInitialVelocity(Vector3f.ZERO);
        trail.getParticleInfluencer().setVelocityVariation(0f);
        trail.setStartSize(1f);
        trail.setEndSize(1f);
        trail.setGravity(Vector3f.ZERO);
        trail.setLowLife(0.2f);
        trail.setHighLife(0.2f);
        trail.setParticlesPerSec(2000);
        trail.setFaceNormal(Vector3f.UNIT_Y);
        return trail;
    }

    @Override
    public Node build(BuildParameters params) {
        Sphere sphere = new Sphere(32, 32, 4f);

        Geometry geom = new Geometry("paralyze-geom", sphere);

        Node node = new Node("rail");
        node.setLocalTranslation(params.location);
        node.attachChild(geom);

        // TODO: Give at least bit better material
//        Material material = new Material(assets,
//                "Common/MatDefs/Misc/Unshaded.j3md");
//        material.setColor("Color", ColorRGBA.Cyan);
//        node.setMaterial(material);
        
                Material mat = new Material(assets, "MatDefs/Lava/Lava.j3md");
        mat.setFloat("Speed", 30f);

        Texture tex = assets.loadTexture("Textures/Plasma.png");
        Texture noise = assets.loadTexture("Textures/noise3.png");
        tex.setWrap(Texture.WrapMode.MirroredRepeat);
        noise.setWrap(Texture.WrapMode.MirroredRepeat);
        mat.setTexture("Color", tex);
        mat.setTexture("Noise", noise);

        mat.getAdditionalRenderState()
                .setBlendMode(RenderState.BlendMode.Additive);

        geom.setQueueBucket(RenderQueue.Bucket.Transparent);
        geom.setMaterial(mat);

        node.setUserData(UserData.SPEED, 180f);
        node.setUserData(UserData.MASS, 0.30f);
        node.setUserData(UserData.DAMAGE, 200f);
        node.setUserData(UserData.IMPULSE_FACTOR, 0f);

        if (world.isClient()) {
            final ParticleEmitter smoke = createTrailEmitter();
            node.attachChild(smoke);

            node.addControl(new CEntityEvent());
            /**
             * Here we specify what happens on client side when fireball is
             * removed. In this case we want explosion effect.
             */
            AParalysisRemoval aRemoval = new AParalysisRemoval();
            aRemoval.setBullet(node);
            aRemoval.setSmokeTrail(smoke);

            node.getControl(CEntityEvent.class).setOnRemoval(aRemoval);
            node.addControl(new CParticleDirector(smoke));
        }

        SphereCollisionShape collisionShape = new SphereCollisionShape(4.5f);
        RigidBodyControl physicsBody = new RigidBodyControl(collisionShape,
                (float) node.getUserData(UserData.MASS));


        /**
         * Add collision group of characters
         */
        GhostControl characterCollision = new GhostControl(collisionShape);
        characterCollision.setCollideWithGroups(CollisionGroups.CHARACTERS);
        characterCollision.setCollisionGroup(CollisionGroups.NONE);
        node.addControl(characterCollision);

        node.addControl(physicsBody);
        CProjectile cProjectile = new CProjectile();
        cProjectile.setIsProjectile(false);
        node.addControl(cProjectile);
        
        /**
         * We don't want projectiles to collide with each other so we give them
         * their own collision group and prevent them from colliding with that
         * group.
         */
        physicsBody.setCollisionGroup(CollisionGroups.NONE);
        physicsBody.setCollideWithGroups(0);
        
        CSpellBuff cBuff = new CSpellBuff();
        node.addControl(cBuff);
        cBuff.addBuff(new IncapacitateCC.MyBuilder(2.4f));
        return node;
    }
}

class AParalysisRemoval implements ARemovalEvent {

    private Node bullet;
    private ParticleEmitter trail;

    public void setBullet(Node bullet) {
        this.bullet = bullet;
    }

    private void leaveSmokeTrail(final Node worldRoot, Vector3f worldTranslation) {
        trail.setParticlesPerSec(0);
        worldRoot.attachChild(trail);
        trail.setLocalTranslation(worldTranslation);
        trail.addControl(new CTimedExistence(5f));
    }

    @Override
    public void exec(World world, int reason) {
        Vector3f worldTranslation = bullet.getLocalTranslation();
        leaveSmokeTrail(world.getWorldRoot(), worldTranslation);
    }

    public void setSmokeTrail(ParticleEmitter smoke) {
        trail = smoke;
    }
}
