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

import arkhados.Globals;
import arkhados.WorldManager;
import arkhados.actions.EntityAction;
import arkhados.actions.castspellactions.CastProjectileAction;
import arkhados.controls.CharacterPhysicsControl;
import arkhados.controls.EntityEventControl;
import arkhados.controls.InfluenceInterfaceControl;
import arkhados.controls.ProjectileControl;
import arkhados.controls.SpellBuffControl;
import arkhados.controls.TimedExistenceControl;
import arkhados.entityevents.RemovalEventAction;
import arkhados.spell.CastSpellActionBuilder;
import arkhados.spell.Spell;
import arkhados.util.NodeBuilder;
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
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;

/**
 * EliteSoldiers's Shotgun (M1) spell. Fires 6 pellets that spread out.
 */
public class Shotgun extends Spell {
    {
        this.iconName = "shotgun.png";
    }

    public Shotgun(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Spell create() {
        final float cooldown = 1.5f;
        final float range = 80f;
        final float castTime = 0.4f;

        final Shotgun spell = new Shotgun("Shotgun", cooldown, range, castTime);

        spell.castSpellActionBuilder = new CastSpellActionBuilder() {
            public EntityAction newAction(Node caster, Vector3f location) {
                final CastShotgunAction castShotgun = new CastShotgunAction(spell, Spell.worldManager);
                return castShotgun;
            }
        };

        spell.nodeBuilder = new PelletBuilder();

        return spell;
    }
}

class CastShotgunAction extends EntityAction {

    private static final int PELLETS = 6;
    private static final float SPREAD = 30f * FastMath.DEG_TO_RAD;
    private static final float STEP = SPREAD / (PELLETS - 1);
    private WorldManager worldManager;
    private final Shotgun spell;

    CastShotgunAction(Shotgun spell, WorldManager worldManager) {
        this.spell = spell;
        this.worldManager = worldManager;

    }

    @Override
    public boolean update(float tpf) {
        final CharacterPhysicsControl physicsControl = super.spatial.getControl(CharacterPhysicsControl.class);

        float characterRadius = super.spatial.getUserData(UserDataStrings.RADIUS);

        Vector3f targetLocation = physicsControl.getTargetLocation();
        final Vector3f viewDirection = targetLocation.subtract(super.spatial.getLocalTranslation()).normalizeLocal();
        super.spatial.getControl(CharacterPhysicsControl.class).setViewDirection(viewDirection);

        final Long playerId = super.spatial.getUserData(UserDataStrings.PLAYER_ID);

        Quaternion currentRotation = new Quaternion();

        for (int i = 0; i < PELLETS; ++i) {
            currentRotation.fromAngleAxis(SPREAD / 2f - i * STEP, Vector3f.UNIT_Y);
            Vector3f spawnLocation = super.spatial.getLocalTranslation();

            Vector3f pelletDirection = currentRotation.mult(viewDirection).normalizeLocal();

            final long projectileId = this.worldManager.addNewEntity("Shotgun",
                    spawnLocation, Quaternion.IDENTITY, playerId);
            final Spatial projectile = this.worldManager.getEntity(projectileId);

            final Float damage = projectile.getUserData(UserDataStrings.DAMAGE);
            final Float damageFactor = super.spatial.getUserData(UserDataStrings.DAMAGE_FACTOR);
            projectile.setUserData(UserDataStrings.DAMAGE, damage * damageFactor);

            final ProjectileControl projectileControl = projectile.getControl(ProjectileControl.class);
            projectileControl.setRange(this.spell.getRange());
            projectileControl.setDirection(pelletDirection);
            projectileControl.setOwnerInterface(super.spatial.getControl(InfluenceInterfaceControl.class));
        }

        Globals.effectHandler.sendEffect("Effects/Sound/Shotgun.wav", super.spatial.getWorldTranslation());
        return false;
    }
}

class PelletBuilder extends NodeBuilder {

    private ParticleEmitter createWhiteTrailEmitter() {
        final ParticleEmitter trail = new ParticleEmitter("trail-emitter", ParticleMesh.Type.Triangle, 200);
        final Material materialWhite = new Material(NodeBuilder.assetManager, "Common/MatDefs/Misc/Particle.j3md");
        materialWhite.setTexture("Texture", NodeBuilder.assetManager.loadTexture("Effects/flame.png"));
        trail.setMaterial(materialWhite);
        trail.setImagesX(2);
        trail.setImagesY(2);
        trail.setSelectRandomImage(true);
        trail.setStartColor(new ColorRGBA(0.9f, 0.9f, 0.9f, 0.9f));
        trail.setEndColor(new ColorRGBA(0.9f, 0.9f, 0.9f, 0.9f));
        trail.getParticleInfluencer().setInitialVelocity(Vector3f.ZERO);
        trail.setStartSize(0.5f);
        trail.setEndSize(0.1f);
        trail.setHighLife(0.1f);
        trail.setLowLife(0.1f);
        trail.setParticlesPerSec(100);
        trail.setRandomAngle(true);
        return trail;
    }

    @Override
    public Node build() {
        final Sphere sphere = new Sphere(8, 8, 0.3f);

        final Geometry projectileGeom = new Geometry("projectile-geom", sphere);
//        projectileGeom.setCullHint(Spatial.CullHint.Always);

        final Node node = new Node("projectile");
        node.attachChild(projectileGeom);

        // TODO: Give at least bit better material
        final Material material = new Material(NodeBuilder.assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", ColorRGBA.Black);
        node.setMaterial(material);

        node.setUserData(UserDataStrings.SPEED_MOVEMENT, 140f);
        node.setUserData(UserDataStrings.MASS, 0.30f);
        node.setUserData(UserDataStrings.DAMAGE, 30f);
        node.setUserData(UserDataStrings.IMPULSE_FACTOR, 0f);

        if (NodeBuilder.worldManager.isClient()) {
            final ParticleEmitter trail = this.createWhiteTrailEmitter();
            node.attachChild(trail);

            // TODO: Enable these later to add removalAction

//            node.addControl(new EntityEventControl());
            /**
             * Here we specify what happens on client side when pellet is
             * removed. In this case we want explosion effect.
             */
//            final PelletRemovalAction removalAction = new PelletRemovalAction(assetManager);
//            removalAction.setSmokeTrail(trail);
//            node.getControl(EntityEventControl.class).setOnRemoval(removalAction);
        }

        final SphereCollisionShape collisionShape = new SphereCollisionShape(1.0f);
        final RigidBodyControl physicsBody = new RigidBodyControl(collisionShape, (Float) node.getUserData(UserDataStrings.MASS));
        /**
         * We don't want projectiles to collide with each other so we give them
         * their own collision group and prevent them from colliding with that
         * group.
         */
        physicsBody.setCollisionGroup(RigidBodyControl.COLLISION_GROUP_NONE);
        physicsBody.setCollideWithGroups(RigidBodyControl.COLLISION_GROUP_NONE);

        /**
         * Add collision with characters
         */
        final GhostControl characterCollision = new GhostControl(collisionShape);
        characterCollision.setCollisionGroup(GhostControl.COLLISION_GROUP_16);
        characterCollision.setCollideWithGroups(GhostControl.COLLISION_GROUP_02);
        node.addControl(characterCollision);
        node.addControl(physicsBody);

        node.addControl(new ProjectileControl());

        final SpellBuffControl buffControl = new SpellBuffControl();
        node.addControl(buffControl);

        return node;
    }
}

class PelletRemovalAction implements RemovalEventAction {

    private ParticleEmitter whiteTrail;
    private AssetManager assetManager;

    public PelletRemovalAction(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    private void leaveSmokeTrail(final Node worldRoot, Vector3f worldTranslation) {
        this.whiteTrail.setParticlesPerSec(0);
        worldRoot.attachChild(this.whiteTrail);
        this.whiteTrail.setLocalTranslation(worldTranslation);
        this.whiteTrail.addControl(new TimedExistenceControl(0.5f));
    }

    private void createSmokePuff(final Node worldRoot, Vector3f worldTranslation) {
        final ParticleEmitter smokePuff = new ParticleEmitter("smoke-puff", ParticleMesh.Type.Triangle, 20);
        Material materialGray = new Material(this.assetManager, "Common/MatDefs/Misc/Particle.j3md");
        materialGray.setTexture("Texture", this.assetManager.loadTexture("Effects/flame.png"));
        smokePuff.setMaterial(materialGray);
        smokePuff.setImagesX(2);
        smokePuff.setImagesY(2);
        smokePuff.setSelectRandomImage(true);
        smokePuff.setStartColor(new ColorRGBA(0.5f, 0.5f, 0.5f, 0.2f));
        smokePuff.setStartColor(new ColorRGBA(0.5f, 0.5f, 0.5f, 0.1f));

        smokePuff.getParticleInfluencer().setInitialVelocity(Vector3f.UNIT_X.mult(5.0f));
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

    public void exec(WorldManager worldManager, String reason) {
//        this.leaveSmokeTrail(worldManager.getWorldRoot(), worldTranslation);
//        this.createSmokePuff(worldManager.getWorldRoot(), worldTranslation);
    }

    public void setSmokeTrail(ParticleEmitter smoke) {
        this.whiteTrail = smoke;
    }
}