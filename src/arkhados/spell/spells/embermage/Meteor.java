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

import arkhados.WorldManager;
import arkhados.actions.EntityAction;
import arkhados.actions.SplashAction;
import arkhados.controls.EntityEventControl;
import arkhados.controls.GenericSyncControl;
import arkhados.controls.InfluenceInterfaceControl;
import arkhados.controls.SpellBuffControl;
import arkhados.controls.SpellCastControl;
import arkhados.controls.SyncInterpolationControl;
import arkhados.controls.TimedExistenceControl;
import arkhados.entityevents.RemovalEventAction;
import arkhados.spell.CastSpellActionBuilder;
import arkhados.spell.Spell;
import arkhados.spell.buffs.AbstractBuff;
import arkhados.spell.buffs.DamageOverTimeBuff;
import arkhados.util.DistanceScaling;
import arkhados.util.AbstractNodeBuilder;
import arkhados.util.RemovalReasons;
import arkhados.util.UserDataStrings;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioNode;
import com.jme3.cinematic.MotionPath;
import com.jme3.cinematic.MotionPathListener;
import com.jme3.cinematic.events.MotionEvent;
import com.jme3.collision.CollisionResults;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.shapes.EmitterSphereShape;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author william
 */
public class Meteor extends Spell {

    {
        iconName = "meteor.png";
    }

    public Meteor(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Meteor create() {
        final float cooldown = 8f;
        final float range = 90f;
        final float castTime = 0.4f;

        final Meteor spell = new Meteor("Meteor", cooldown, range, castTime);

        spell.castSpellActionBuilder = new CastSpellActionBuilder() {
            @Override
            public EntityAction newAction(Node caster, Vector3f vec) {
                final CastMeteorAction action = new CastMeteorAction(worldManager, spell);
                DamageOverTimeBuff ignite = Ignite.ifNotCooldownCreateDamageOverTimeBuff(caster);
                if (ignite != null) {
                    action.addAdditionalBuff(ignite);
                }
                return action;
            }
        };

        spell.nodeBuilder = new MeteorNodeBuilder();
        return spell;
    }
}

class CastMeteorAction extends EntityAction {

    private final Spell spell;
    private final WorldManager worldManager;
    private final List<AbstractBuff> additionalBuffs = new ArrayList<>();

    public CastMeteorAction(WorldManager worldManager, Spell spell) {
        this.spell = spell;
        this.worldManager = worldManager;
    }

    public void addAdditionalBuff(AbstractBuff buff) {
        if (buff != null) {
            additionalBuffs.add(buff);
        }
    }

    @Override
    public boolean update(float tpf) {
        final Vector3f startingPoint = spatial.getLocalTranslation().add(0f, 60f, 0f);

        final Vector3f target = spatial.getControl(SpellCastControl.class).getClosestPointToTarget(spell);
        raySelectPoint(startingPoint, target);

        final MotionPath path = new MotionPath();
        path.addWayPoint(startingPoint);
        path.addWayPoint(target);
        final Integer playerId = spatial.getUserData(UserDataStrings.PLAYER_ID);
        final int entityId = worldManager.addNewEntity(spell.getId(),
                startingPoint, Quaternion.IDENTITY, playerId);
        final Spatial meteor = worldManager.getEntity(entityId);

        final MotionEvent motionControl = new MotionEvent(meteor, path);
        motionControl.setInitialDuration(1f);
        motionControl.setSpeed(1f);

        final InfluenceInterfaceControl casterInterface = spatial.getControl(InfluenceInterfaceControl.class);
        meteor.getControl(SpellBuffControl.class).setOwnerInterface(casterInterface);

        path.addListener(new MotionPathListener() {
            @Override
            public void onWayPointReach(MotionEvent motionControl, int wayPointIndex) {
                if (wayPointIndex + 1 == path.getNbWayPoints()) {
                    final Float baseDamage = meteor.getUserData(UserDataStrings.DAMAGE);

                    final SpellBuffControl buffControl = meteor.getControl(SpellBuffControl.class);
                    buffControl.getBuffs().addAll(additionalBuffs);
                    final SplashAction splash = new SplashAction(30f, baseDamage, DistanceScaling.LINEAR, null);
                    splash.setSpatial(meteor);
                    splash.update(0f);
                    destroy();
                }
            }

            private void destroy() {
                worldManager.removeEntity(entityId, RemovalReasons.COLLISION);
            }
        });

        motionControl.play();
        return false;
    }

    private Vector3f raySelectPoint(Vector3f from, Vector3f to) {
        Vector3f direction = to.subtract(from);
        Ray ray = new Ray(from, direction);
        Node wallsNode = (Node) worldManager.getWorldRoot().getChild("Walls");

        CollisionResults results = new CollisionResults();
        wallsNode.collideWith(ray, results);
        if (results.size() > 0) {
            to.set(results.getClosestCollision().getContactPoint());
        }

        return to;
    }
}

class MeteorNodeBuilder extends AbstractNodeBuilder {

    private ParticleEmitter createFireEmitter() {
        final ParticleEmitter fire = new ParticleEmitter("fire-emitter", ParticleMesh.Type.Triangle, 100);
        final Material materialRed = new Material(AbstractNodeBuilder.assetManager, "Common/MatDefs/Misc/Particle.j3md");
        materialRed.setTexture("Texture", AbstractNodeBuilder.assetManager.loadTexture("Effects/flame.png"));
        fire.setMaterial(materialRed);
        fire.setImagesX(2);
        fire.setImagesY(2);
        fire.setSelectRandomImage(true);
        fire.setStartColor(new ColorRGBA(0.95f, 0.150f, 0.0f, 1.0f));
        fire.setEndColor(new ColorRGBA(1.0f, 1.0f, 0.0f, 0.5f));
        fire.getParticleInfluencer().setInitialVelocity(Vector3f.ZERO);
        fire.setStartSize(6.5f);
        fire.setEndSize(0.5f);
        fire.setGravity(Vector3f.ZERO);
        fire.setLowLife(0.2f);
        fire.setHighLife(0.3f);
        fire.setParticlesPerSec(40);
        fire.getParticleInfluencer().setVelocityVariation(0.5f);
        fire.setRandomAngle(true);
        return fire;
    }

    @Override
    public Node build() {
        final Sphere sphere = new Sphere(32, 32, 2.0f);
        final Geometry meteorGeom = new Geometry("meteor-geom", sphere);
        final Node node = new Node("meteor");
        node.attachChild(meteorGeom);

        final Material material = new Material(AbstractNodeBuilder.assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", ColorRGBA.Black);
        node.setMaterial(material);

        node.setUserData(UserDataStrings.DAMAGE, 300f);
        node.setUserData(UserDataStrings.IMPULSE_FACTOR, 15000f);

        final SpellBuffControl spellBuffControl = new SpellBuffControl();
        node.addControl(spellBuffControl);

        node.addControl(new GenericSyncControl());

        if (AbstractNodeBuilder.worldManager.isClient()) {
            node.addControl(new SyncInterpolationControl());
            node.addControl(new EntityEventControl());
            final ParticleEmitter fire = createFireEmitter();
            node.attachChild(fire);

            final MeteorRemovalAction removalAction = new MeteorRemovalAction(assetManager);
            removalAction.setEmitter(fire);

            node.getControl(EntityEventControl.class).setOnRemoval(removalAction);
        }

        return node;
    }
}

class MeteorRemovalAction implements RemovalEventAction {

    private ParticleEmitter emitter;
    private AudioNode sound;

    MeteorRemovalAction(AssetManager assetManager) {
        sound = new AudioNode(assetManager, "Effects/Sound/MeteorBoom.wav");
        sound.setPositional(true);
        sound.setReverbEnabled(false);
        sound.setVolume(5f);
    }

    @Override
    public void exec(WorldManager worldManager, int reason) {
        if (reason != RemovalReasons.COLLISION) {
            return;
        }
        Vector3f worldTranslation = emitter.getParent().getLocalTranslation();
        worldManager.getWorldRoot().attachChild(sound);
        sound.setLocalTranslation(worldTranslation);
        sound.play();

        emitter.removeFromParent();
        worldManager.getWorldRoot().attachChild(emitter);

        emitter.setLocalTranslation(worldTranslation);
        emitter.addControl(new TimedExistenceControl(4f));
        emitter.getParticleInfluencer().setInitialVelocity(new Vector3f(1f, 0.01f, 1f).mult(16.0f));
        emitter.getParticleInfluencer().setVelocityVariation(1f);
        emitter.setStartSize(6f);
        emitter.setEndSize(40f);
        emitter.setLowLife(2f);
        emitter.setHighLife(2f);
        emitter.setEndColor(new ColorRGBA(1.0f, 1.0f, 0.0f, 0.01f));
        emitter.setShape(new EmitterSphereShape(Vector3f.ZERO, 6.0f));

        emitter.setNumParticles(20);
        emitter.emitAllParticles();
        emitter.setParticlesPerSec(0.0f);
    }

    void setEmitter(ParticleEmitter emitter) {
        this.emitter = emitter;
    }
}