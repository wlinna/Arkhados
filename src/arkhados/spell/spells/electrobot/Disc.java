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
import arkhados.Globals;
import arkhados.World;
import arkhados.actions.EntityAction;
import arkhados.controls.CCharacterPhysics;
import arkhados.controls.CEntityEvent;
import arkhados.controls.CInfluenceInterface;
import arkhados.controls.CSpellBuff;
import arkhados.controls.CSync;
import arkhados.controls.CTimedExistence;
import arkhados.effects.ParticleInfluencerWithAngleSetting;
import com.jme3.effect.ParticleEmitter;
import arkhados.entityevents.ARemovalEvent;
import arkhados.messages.sync.statedata.ProjectileSyncData;
import arkhados.messages.sync.statedata.StateData;
import arkhados.spell.Spell;
import arkhados.spell.buffs.IncapacitateCC;
import arkhados.util.AbstractNodeBuilder;
import arkhados.util.BuildParameters;
import arkhados.util.CastSpawn;
import arkhados.util.RemovalReasons;
import arkhados.util.UserData;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
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
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.shape.Torus;
import com.jme3.texture.Texture;

public class Disc extends Spell {

    static final float CAST_TIME = 0.7f;

    {
        iconName = "ElectricDisc.png";
        setMoveTowardsTarget(false);
    }

    public Disc(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Spell create() {
        final float cooldown = 7f;
        final float range = 60f;

        Disc spell = new Disc("Disc", cooldown, range, CAST_TIME);

        spell.castSpellActionBuilder = (Node caster, Vector3f vec) -> {
            ACastDisc action = new ACastDisc(spell);
            return action;
        };

        spell.nodeBuilder = new DiscBuilder();

        return spell;
    }
}

class ACastDisc extends EntityAction {

    private final Spell spell;

    public ACastDisc(Spell spell) {
        this.spell = spell;
    }

    @Override
    public boolean update(float tpf) {
        CastSpawn.SpawnInfo spawn = CastSpawn.spawn(spatial, spell);
        Spatial disc = spawn.spatial;
        CDisc cDisc = disc.getControl(CDisc.class);
        cDisc.setOwner(spatial);
        cDisc.getBody().setLinearVelocity(spawn.viewDirection.mult(120f));
        cDisc.getBody().getPhysicsSpace().addCollisionListener(cDisc);
        return false;
    }
}

class CDisc extends AbstractControl implements PhysicsCollisionListener, CSync {

    private Spatial owner;

    // ONLY for applying forces etc. Not for collisions.
    private RigidBodyControl body;
    private float age = 0f;

    private Spatial enemy;
    private GhostControl ghost;

    private float accelFactor = 90f;

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        if (spatial != null) {
            body = spatial.getControl(RigidBodyControl.class);
            ghost = spatial.getControl(GhostControl.class);
            body.setGravity(Vector3f.ZERO);
        } else {
            body.getPhysicsSpace().removeCollisionListener(this);
        }
    }

    @Override
    protected void controlUpdate(float tpf) {
        // 1. Update age; check it; check the proximity to the ElectroBot
        //    -> Punish the enemy
        // 2. Update "gravity" to point towards the ElectroBot
        // 3. Check for collision with an enemy characters

        age += tpf;
        float distanceSq = spatial.getLocalTranslation()
                .distanceSquared(owner.getLocalTranslation());

        // TODO: The must NOT be destroyed at beginning, but it MUST be
        // destroyed if it has caught an enemy, or if it has lived long enough
        if (age > 3.5f || (enemy != null && distanceSq < 100f)) {
            if (enemy != null) {
                CharacterInteraction.harm(
                        owner.getControl(CInfluenceInterface.class),
                        enemy.getControl(CInfluenceInterface.class),
                        50f, null, false);
            }

            // Remove
            World world = Globals.app.getStateManager().getState(World.class);
            world.removeEntity((int) spatial.getUserData(UserData.ENTITY_ID),
                    RemovalReasons.EXPIRED);
            return;
        }
        Vector3f bodyLoc = body.getPhysicsLocation();
        body.setGravity(Vector3f.ZERO);
        Vector3f gravity = owner.getLocalTranslation()
                .subtract(bodyLoc).setY(0f)
                .normalizeLocal().multLocal(accelFactor);
        Vector3f velocity = body.getLinearVelocity();
        velocity.addLocal(gravity.multLocal(tpf));
        body.setLinearVelocity(velocity);

        if (enemy != null) {
            enemy.getControl(CCharacterPhysics.class).warp(bodyLoc);
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    @Override
    public void collision(PhysicsCollisionEvent event) {
        if ((event.getObjectA() != ghost && event.getObjectB() != ghost)
                || (event.getObjectA().getUserObject()
                == event.getObjectB().getUserObject())) {
            return;
        }

        PhysicsCollisionObject otherObject
                = event.getObjectA().getUserObject() == spatial
                        ? event.getObjectB()
                        : event.getObjectA();

        int otherCollisionGroup = otherObject.getCollisionGroup();
        if (otherCollisionGroup != CollisionGroups.CHARACTERS
                && otherCollisionGroup != CollisionGroups.WALLS
                && otherCollisionGroup != CollisionGroups.SPIRIT_STONE) {
            return;
        }

        // This filters away shields
        if (otherCollisionGroup == CollisionGroups.CHARACTERS) {
            Spatial targetSpatial = (Spatial) otherObject.getUserObject();
            if (targetSpatial.getControl(CCharacterPhysics.class) == null) {
                return;
            }
        }

        if (otherObject.getCollisionGroup() == CollisionGroups.CHARACTERS) {
            Spatial otherSpatial = (Spatial) otherObject.getUserObject();
            if (!spatial.getUserData(UserData.TEAM_ID).equals(otherSpatial.getUserData(UserData.TEAM_ID))) {
                enemy = otherSpatial;
            }      
        }
    }

    @Override
    public StateData getSyncableData(StateData stateData) {
        int entityId = spatial.getUserData(UserData.ENTITY_ID);
        ProjectileSyncData data = new ProjectileSyncData(entityId, body);
        return data;
    }

    public void setOwner(Spatial owner) {
        this.owner = owner;
    }

    public RigidBodyControl getBody() {
        return body;
    }
}

class DiscBuilder extends AbstractNodeBuilder {

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
        Mesh mesh = new Torus(32, 16, 1f, 4f);

        Geometry geom = new Geometry("paralyze-geom", mesh);
        geom.rotateUpTo(Vector3f.UNIT_Z.negate());

        Node node = new Node("disc");
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

            CEntityEvent cEvent = new CEntityEvent();
            node.addControl(cEvent);

            AudioNode audio = new AudioNode(assets,
                    "Effects/Sound/ElectricDisc.wav",
                    AudioData.DataType.Buffer);
            audio.setName("buzz");
            audio.setLooping(true);
            audio.setVolume(1.8f);
            audio.play();
            node.attachChild(audio);

            ADiscRemoval aRemoval = new ADiscRemoval(audio);
            aRemoval.setBullet(node);
            aRemoval.setSmokeTrail(smoke);

            cEvent.setOnRemoval(aRemoval);
            cEvent.setOnDisappear(aRemoval);
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
        characterCollision.setCollisionGroup(CollisionGroups.PROJECTILES);
        node.addControl(characterCollision);

        node.addControl(physicsBody);
        physicsBody.setGravity(Vector3f.ZERO);

        if (world.isServer()) {
            CDisc cDisc = new CDisc();
            node.addControl(cDisc);
        }

        /**
         * We don't want projectiles to collide with each other so we give them
         * their own collision group and prevent them from colliding with that
         * group.
         */
        physicsBody.setCollisionGroup(CollisionGroups.NONE);
//        physicsBody.removeCollideWithGroup(CollisionGroups.PROJECTILES);
//        physicsBody.removeCollideWithGroup(CollisionGroups.WALLS);
        physicsBody.setCollideWithGroups(0);

        CSpellBuff cBuff = new CSpellBuff();
        node.addControl(cBuff);
        cBuff.addBuff(new IncapacitateCC.MyBuilder(2.4f));
        return node;
    }
}

class ADiscRemoval implements ARemovalEvent {

    private Node bullet;
    private ParticleEmitter trail;
    private final AudioNode audio;

    public ADiscRemoval(AudioNode audio) {
        this.audio = audio;
    }        

    public void setBullet(Node bullet) {
        this.bullet = bullet;
    }

    private void leaveSmokeTrail(final Node worldRoot, Vector3f worldTranslation) {
        trail.setParticlesPerSec(0);
        worldRoot.attachChild(trail);
        trail.setLocalTranslation(worldTranslation);
        trail.addControl(new CTimedExistence(5f));
        audio.stop();
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
