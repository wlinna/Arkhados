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

import arkhados.CollisionGroups;
import arkhados.Globals;
import arkhados.World;
import arkhados.actions.ACastingSpell;
import arkhados.actions.AChannelingSpell;
import arkhados.actions.ADelay;
import arkhados.actions.ASplash;
import arkhados.actions.EntityAction;
import arkhados.controls.CActionQueue;
import arkhados.controls.CCharacterPhysics;
import arkhados.controls.CEntityEvent;
import arkhados.controls.CGenericSync;
import arkhados.controls.CSpellBuff;
import arkhados.controls.CSpellCast;
import arkhados.controls.CSyncInterpolation;
import arkhados.controls.CTimedExistence;
import arkhados.entityevents.ARemovalEvent;
import arkhados.spell.CastSpellActionBuilder;
import arkhados.spell.Spell;
import arkhados.spell.spells.embermage.Meteor;
import arkhados.util.AbstractNodeBuilder;
import arkhados.util.BuildParameters;
import arkhados.util.DistanceScaling;
import arkhados.util.RemovalReasons;
import arkhados.util.UserData;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;

public class IntoTheShadows extends Spell {

    {
        iconName = "flame_walk.png";
    }
    static final float RADIUS = 27.5f;

    public IntoTheShadows(String name, float cooldown, float range,
            float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static IntoTheShadows create() {
        final float cooldown = 9f;
        final float range = 90f;
        final float castTime = 0.15f;
        final IntoTheShadows spell = new IntoTheShadows("Into the Shadows",
                cooldown, range, castTime);

        spell.castSpellActionBuilder = new CastSpellActionBuilder() {
            @Override
            public EntityAction newAction(Node caster, Vector3f vec) {
                ACastIntoTheShadows castAction =
                        new ACastIntoTheShadows(spell, world);
                return castAction;
            }
        };

        spell.nodeBuilder = new IntoTheShadowsBuilder();
        return spell;
    }

    private static class ACastIntoTheShadows extends EntityAction {

        private final Spell spell;
        private final World world;

        public ACastIntoTheShadows(Spell spell, World world) {
            this.spell = spell;
            this.world = world;
        }

        @Override
        public boolean update(float tpf) {
            spawnCloud();
            return false;
        }

        private void spawnCloud() {
            CSpellCast cSpell = spatial.getControl(CSpellCast.class);
            Vector3f target = cSpell.getClosestPointToTarget(spell)
                    .add(0f, 5f, 0f);

            int playerId = spatial.getUserData(UserData.PLAYER_ID);
            int playerEntityId = spatial.getUserData(UserData.ENTITY_ID);
            int cloudId = world.addNewEntity(spell.getId(), target,
                    Quaternion.IDENTITY, playerId);
            Spatial cloud = world.getEntity(cloudId);
            CActionQueue cloudActions = cloud.getControl(CActionQueue.class);

            ASplash splash = new ASplash(IntoTheShadows.RADIUS, 120f,
                    DistanceScaling.CONSTANT, null);

            cloudActions.enqueueAction(new ADelay(0.4f));
            cloudActions.enqueueAction(splash);
            cloudActions.enqueueAction(
                    new ARestoreEntity(world, playerEntityId, target));
            cloudActions.enqueueAction(new ARemoveCloud(world, cloudId));

            world.temporarilyRemoveEntity(playerEntityId);            
        }
    }

    private static class IntoTheShadowsBuilder extends AbstractNodeBuilder {

        private ParticleEmitter createCloudEmitter() {
            ParticleEmitter cloud = new ParticleEmitter("fire-emitter",
                    ParticleMesh.Type.Triangle, 100);
            Material mat = new Material(assetManager,
                    "Common/MatDefs/Misc/Particle.j3md");
            mat.setTexture("Texture",
                    assetManager.loadTexture("Effects/flame_alpha.png"));
            mat.getAdditionalRenderState()
                    .setBlendMode(RenderState.BlendMode.Alpha);
            cloud.setMaterial(mat);
            cloud.setImagesX(2);
            cloud.setImagesY(2);
            cloud.setSelectRandomImage(true);
            cloud.setStartColor(new ColorRGBA(0f, 0f, 0f, 1.0f));
            cloud.setEndColor(new ColorRGBA(0f, 0f, 0f, 0.1f));
            cloud.getParticleInfluencer().setInitialVelocity(Vector3f.ZERO);
            cloud.setStartSize(8.5f);
            cloud.setEndSize(IntoTheShadows.RADIUS);
            cloud.setGravity(Vector3f.ZERO);
            cloud.setLowLife(0.8f);
            cloud.setHighLife(1.2f);
            cloud.setParticlesPerSec(30);
            return cloud;
        }

        @Override
        public Node build(BuildParameters params) {
            Sphere sphere = new Sphere(16, 16, 0.2f);
            Geometry projectileGeom = new Geometry("geom", sphere);
            Node node = new Node("cloud");
            node.setLocalTranslation(params.location);
            node.attachChild(projectileGeom);

            node.addControl(new CSyncInterpolation());
            // TODO: Give at least bit better material
            Material material = new Material(assetManager,
                    "Common/MatDefs/Misc/Unshaded.j3md");
            material.setColor("Color", ColorRGBA.Black);
            node.setMaterial(material);

            node.setUserData(UserData.SPEED_MOVEMENT, 0f);
            node.setUserData(UserData.MASS, 0f);
            node.setUserData(UserData.DAMAGE, 50f);
            node.setUserData(UserData.IMPULSE_FACTOR, 26000f);

            CSpellBuff buffControl = new CSpellBuff();
            node.addControl(buffControl);

            node.addControl(new CGenericSync());

            CActionQueue actionQueue = new CActionQueue();
            node.addControl(actionQueue);

            if (world.isServer()) {
                SphereCollisionShape collisionShape =
                        new SphereCollisionShape(8f);

                GhostControl ghost = new GhostControl(collisionShape);
                ghost.setCollisionGroup(CollisionGroups.NONE);
                ghost.setCollideWithGroups(CollisionGroups.CHARACTERS);
            }
            if (world.isClient()) {
                ParticleEmitter cloud = createCloudEmitter();
                node.attachChild(cloud);

                CEntityEvent cEvent = new CEntityEvent();
                node.addControl(cEvent);
                cEvent.setOnRemoval(new CloudRemoval().setCloud(cloud));
            }
            return node;
        }
    }
}

class CloudRemoval implements ARemovalEvent {

    private Spatial cloud;

    private ParticleEmitter createShockwave() {
        ParticleEmitter wave = new ParticleEmitter("shockwave-emitter",
                ParticleMesh.Type.Triangle, 3);
        Material mat = new Material(Globals.assets,
                "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture", Globals.assets
                .loadTexture("Effects/shockwave_alpha.png"));
        mat.getAdditionalRenderState()
                .setBlendMode(RenderState.BlendMode.Alpha);
        wave.setMaterial(mat);
        wave.setImagesX(1);
        wave.setImagesY(1);

        wave.setGravity(Vector3f.ZERO);

        wave.setStartColor(new ColorRGBA(0f, 0f, 0f, 1f));
        wave.setEndColor(new ColorRGBA(0f, 0f, 0f, 0f));
        wave.setLowLife(0.5f);
        wave.setHighLife(0.5f);
        wave.setStartSize(0.50f);
        wave.setEndSize(IntoTheShadows.RADIUS);
        wave.getParticleInfluencer().setInitialVelocity(Vector3f.ZERO);
        wave.getParticleInfluencer().setVelocityVariation(0f);
        wave.setParticlesPerSec(0f);

        return wave;
    }

    @Override
    public void exec(World world, int reason) {
        if (reason != RemovalReasons.EXPIRED) {
            return;
        }

        Vector3f worldTranslation = cloud.getParent().getWorldTranslation();

        ParticleEmitter wave = createShockwave();
        world.getWorldRoot().attachChild(wave);
        wave.setLocalTranslation(worldTranslation);
        wave.emitAllParticles();
        wave.addControl(new CTimedExistence(4f));
    }

    public CloudRemoval setCloud(Spatial cloud) {
        this.cloud = cloud;
        return this;
    }
}

class ARestoreEntity extends EntityAction {

    private final World world;
    private final int id;
    private final Vector3f loc;
    private final Quaternion rot = Quaternion.IDENTITY;

    public ARestoreEntity(World world, int id, Vector3f location) {
        this.world = world;
        this.id = id;
        this.loc = location;
    }

    @Override
    public boolean update(float tpf) {
        world.restoreTemporarilyRemovedEntity(id, loc, rot);
        return false;
    }
}

class ARemoveCloud extends EntityAction {

    private final World world;
    private final int id;

    public ARemoveCloud(World world, int id) {
        this.world = world;
        this.id = id;
    }

    @Override
    public boolean update(float tpf) {
        world.removeEntity(id, RemovalReasons.EXPIRED);
        return false;
    }
}