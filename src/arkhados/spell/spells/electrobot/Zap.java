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

import arkhados.CharacterInteraction;
import arkhados.CollisionGroups;
import arkhados.World;
import arkhados.actions.cast.ACastProjectile;
import arkhados.characters.ElectroBot;
import arkhados.controls.CInfluenceInterface;
import arkhados.controls.CProjectile;
import arkhados.controls.CSpellBuff;
import arkhados.controls.CTimedExistence;
import arkhados.effects.Lightning;
import arkhados.effects.ParticleInfluencerWithAngleSetting;
import com.jme3.effect.ParticleEmitter;
import arkhados.entityevents.ARemovalEvent;
import arkhados.spell.Spell;
import arkhados.spell.buffs.AbstractBuff;
import arkhados.spell.buffs.AbstractBuffBuilder;
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
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.control.AbstractControl;
import java.util.Iterator;
import java.util.List;

public class Zap extends Spell {

    static final float CAST_TIME = 0.4f;

    {
        iconName = "railgun.png";
        setMoveTowardsTarget(false);
    }

    public Zap(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Spell create() {
        final float cooldown = 0.9f;
        final float range = 60f;

        Zap spell = new Zap("Zap", cooldown, range, CAST_TIME);

        spell.castSpellActionBuilder = (Node caster, Vector3f vec) -> {
            boolean hadBuff = false;
            List<AbstractBuff> buffs = caster
                    .getControl(CInfluenceInterface.class).getBuffs();
            for (Iterator<AbstractBuff> it = buffs.iterator(); it.hasNext();) {
                AbstractBuff buff = it.next();
                if (buff instanceof PowerBuff) {
                    hadBuff = true;
                    it.remove();
                    break;
                }
            }

            ACastProjectile action = new ACastProjectile(spell, world);
            action.setTypeId(ElectroBot.ACTION_ZAP);
            if (hadBuff) {
                action.addBuff(new TriggerBuff.TriggerBuffBuilder());
            }
            return action;
        };

        spell.nodeBuilder = new ZapBuilder();

        return spell;
    }
}

class ZapBuilder extends AbstractNodeBuilder {

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
        trail.setStartColor(new ColorRGBA(0.3f, 0.3f, 0.9f, 1f));
        trail.setParticleInfluencer(new ParticleInfluencerWithAngleSetting());
        trail.getParticleInfluencer().setInitialVelocity(Vector3f.ZERO);
        trail.getParticleInfluencer().setVelocityVariation(0f);
        trail.setStartSize(2f);
        trail.setEndSize(2f);
        trail.setGravity(Vector3f.ZERO);
        trail.setLowLife(0.3f);
        trail.setHighLife(0.3f);
        trail.setParticlesPerSec(2000);
        trail.setFaceNormal(Vector3f.UNIT_Y);
        return trail;
    }

    @Override
    public Node build(BuildParameters params) {
        Mesh mesh = Lightning.createGeometry(0f, 0.3f, 0.3f); 
        
        Geometry projectileGeom = new Geometry("zap-geom", mesh);
        projectileGeom.scale(5f);

        Node node = new Node("rail");
        node.setLocalTranslation(params.location);
        node.attachChild(projectileGeom);

        Material material = assets.loadMaterial("Materials/ZapLightning.j3m");        
        material.setFloat("LateralFactor", 0.4f);
        material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.AlphaAdditive);
        projectileGeom.setQueueBucket(RenderQueue.Bucket.Translucent);
        node.setMaterial(material);

        node.setUserData(UserData.SPEED, 140f);
        node.setUserData(UserData.MASS, 0.30f);
        node.setUserData(UserData.DAMAGE, 150f);
        node.setUserData(UserData.IMPULSE_FACTOR, 0f);

        if (world.isClient()) {
//            final ParticleEmitter smoke = createTrailEmitter();
//            node.attachChild(smoke);

//            node.addControl(new CEntityEvent());
            /**
             * Here we specify what happens on client side when fireball is
             * removed. In this case we want explosion effect.
             */
//            AZapRemoval removalAction = new AZapRemoval();
//            removalAction.setBullet(node);
//            removalAction.setSmokeTrail(smoke);

//            node.getControl(CEntityEvent.class).setOnRemoval(removalAction);
//            node.addControl(new CParticleDirector(smoke));
        }

        SphereCollisionShape collisionShape = new SphereCollisionShape(2.5f);
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
        GhostControl characterCollision = new GhostControl(collisionShape);
        characterCollision.setCollideWithGroups(CollisionGroups.CHARACTERS);
        characterCollision.setCollisionGroup(CollisionGroups.PROJECTILES);
        node.addControl(characterCollision);

        node.addControl(physicsBody);
        CProjectile cProjectile = new CProjectile();
        cProjectile.setIsProjectile(false);
        node.addControl(cProjectile);
        final CSpellBuff cBuff = new CSpellBuff();
        node.addControl(cBuff);

        return node;
    }
}

class AZapRemoval implements ARemovalEvent {

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

class TriggerBuff extends AbstractBuff {

    public TriggerBuff() {
        super(0f);
    }

    @Override
    public void attachToCharacter(CInfluenceInterface targetInterface) {
        // TODO: Add debuff        
        CharacterInteraction.harm(getOwnerInterface(),
                targetInterface, 100f, null, false);
    }

    static class TriggerBuffBuilder extends AbstractBuffBuilder {

        public TriggerBuffBuilder() {
            super(0f);
        }

        @Override
        public AbstractBuff build() {
            return set(new TriggerBuff());
        }
    }
}
