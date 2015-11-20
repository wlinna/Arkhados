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

import arkhados.Globals;
import arkhados.World;
import arkhados.actions.EntityAction;
import arkhados.actions.ASplash;
import arkhados.controls.CEntityEvent;
import arkhados.controls.CGenericSync;
import arkhados.controls.CInfluenceInterface;
import arkhados.controls.CSpellBuff;
import arkhados.controls.CSpellCast;
import arkhados.controls.CSyncInterpolation;
import arkhados.controls.CTimedExistence;
import arkhados.entityevents.ARemovalEvent;
import arkhados.spell.Spell;
import arkhados.spell.buffs.AbstractBuffBuilder;
import arkhados.util.DistanceScaling;
import arkhados.util.AbstractNodeBuilder;
import arkhados.util.BuildParameters;
import arkhados.util.RemovalReasons;
import arkhados.util.UserData;
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

public class Meteor extends Spell {

    static final float SPLASH_RADIUS = 25f;

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

        spell.castSpellActionBuilder = (Node caster, Vector3f vec) -> {
            ACastMeteor action = new ACastMeteor(world, spell);
            AbstractBuffBuilder ignite =
                    Ignite.ifNotCooldownCreateDamageOverTimeBuff(caster);
            if (ignite != null) {
                action.addAdditionalBuff(ignite);
            }
            return action;
        };

        spell.nodeBuilder = new MeteorNodeBuilder();
        return spell;
    }
}

class ACastMeteor extends EntityAction {

    private final Spell spell;
    private final World world;
    private final List<AbstractBuffBuilder> additionalBuffs = new ArrayList<>();

    public ACastMeteor(World world, Spell spell) {
        this.spell = spell;
        this.world = world;
    }

    public void addAdditionalBuff(AbstractBuffBuilder buff) {
        if (buff != null) {
            additionalBuffs.add(buff);
        }
    }

    @Override
    public boolean update(float tpf) {
        final Vector3f startingPoint =
                spatial.getLocalTranslation().add(0f, 60f, 0f);

        final Vector3f target =
                spatial.getControl(CSpellCast.class).getClosestPointToTarget(spell);
        raySelectPoint(startingPoint, target);

        final MotionPath path = new MotionPath();
        path.addWayPoint(startingPoint);
        path.addWayPoint(target);
        int playerId = spatial.getUserData(UserData.PLAYER_ID);
        final int entityId = world.addNewEntity(spell.getId(), startingPoint,
                Quaternion.IDENTITY, playerId);
        final Spatial meteor = world.getEntity(entityId);

        final MotionEvent motionControl = new MotionEvent(meteor, path);
        motionControl.setInitialDuration(0.6f);
        motionControl.setSpeed(1f);

        final CInfluenceInterface casterInterface =
                spatial.getControl(CInfluenceInterface.class);
        meteor.getControl(CSpellBuff.class).setOwnerInterface(casterInterface);

        path.addListener(new MotionPathListener() {
            @Override
            public void onWayPointReach(MotionEvent motionControl,
                    int wayPointIndex) {
                if (wayPointIndex + 1 == path.getNbWayPoints()) {
                    final float baseDamage =
                            meteor.getUserData(UserData.DAMAGE);

                    CSpellBuff buffControl = meteor.getControl(CSpellBuff.class);
                    buffControl.getBuffs().addAll(additionalBuffs);
                    ASplash splash =
                            new ASplash(Meteor.SPLASH_RADIUS,
                            baseDamage, DistanceScaling.LINEAR, null);
                    splash.setCasterInterface(casterInterface);
                    int teamId = meteor.getUserData(UserData.TEAM_ID);
                    splash.setExcludedTeam(teamId);
                    splash.setSpatial(meteor);
                    splash.update(0f);
                    destroy();
                }
            }

            private void destroy() {
                world.removeEntity(entityId, RemovalReasons.COLLISION);
            }
        });

        motionControl.play();
        return false;
    }

    private Vector3f raySelectPoint(Vector3f from, Vector3f to) {
        Vector3f direction = to.subtract(from);
        Ray ray = new Ray(from, direction);
        Node wallsNode = (Node) world.getWorldRoot().getChild("Walls");

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
        ParticleEmitter fire = new ParticleEmitter("fire-emitter",
                ParticleMesh.Type.Triangle, 100);
        Material materialRed = new Material(assets,
                "Common/MatDefs/Misc/Particle.j3md");
        materialRed.setTexture("Texture",
                assets.loadTexture("Effects/flame.png"));
        fire.setMaterial(materialRed);
        fire.setImagesX(2);
        fire.setImagesY(2);
        fire.setSelectRandomImage(true);
        fire.setStartColor(new ColorRGBA(0.95f, 0.150f, 0f, 1f));
        fire.setEndColor(new ColorRGBA(1f, 1f, 0f, 0.5f));
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
    public Node build(BuildParameters params) {
        Sphere sphere = new Sphere(32, 32, 2.0f);
        Geometry meteorGeom = new Geometry("meteor-geom", sphere);
        Node node = new Node("meteor");
        node.setLocalTranslation(params.location);
        node.attachChild(meteorGeom);

        Material material = new Material(assets,
                "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", ColorRGBA.Black);
        node.setMaterial(material);

        node.setUserData(UserData.DAMAGE, 340f);
        node.setUserData(UserData.IMPULSE_FACTOR, 25000f);

        CSpellBuff spellBuffControl = new CSpellBuff();
        node.addControl(spellBuffControl);

        node.addControl(new CGenericSync());

        if (world.isClient()) {
            node.addControl(new CSyncInterpolation());
            node.addControl(new CEntityEvent());
            ParticleEmitter fire = createFireEmitter();
            node.attachChild(fire);

            AMeteorRemoval removalAction = new AMeteorRemoval();
            removalAction.setEmitter(fire);

            node.getControl(CEntityEvent.class).setOnRemoval(removalAction);
        }

        return node;
    }
}

class AMeteorRemoval implements ARemovalEvent {

    private ParticleEmitter emitter;
    private final AudioNode sound;

    AMeteorRemoval() {
        sound = new AudioNode(Globals.assets, "Effects/Sound/MeteorBoom.wav");
        sound.setPositional(true);
        sound.setReverbEnabled(false);
        sound.setVolume(5f);
    }

    private ParticleEmitter createShockwave() {
        ParticleEmitter wave = new ParticleEmitter("shockwave-emitter",
                ParticleMesh.Type.Triangle, 3);
        Material mat = new Material(Globals.assets,
                "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture", 
                Globals.assets.loadTexture("Effects/shockwave.png"));
        wave.setMaterial(mat);
        wave.setImagesX(1);
        wave.setImagesY(1);

        wave.setGravity(Vector3f.ZERO);

        wave.setStartColor(new ColorRGBA(0.7f, 0.7f, 0.7f, 1f));
        wave.setEndColor(new ColorRGBA(0.7f, 0.7f, 0.7f, 0f));
        wave.setLowLife(0.5f);
        wave.setHighLife(0.5f);
        wave.setStartSize(0.50f);
        wave.setEndSize(Meteor.SPLASH_RADIUS + 7f);
        wave.getParticleInfluencer().setInitialVelocity(Vector3f.ZERO);
        wave.getParticleInfluencer().setVelocityVariation(0f);
        wave.setParticlesPerSec(0f);
        wave.setFaceNormal(Vector3f.UNIT_Y);

        return wave;
    }

    @Override
    public void exec(World world, int reason) {
        if (reason != RemovalReasons.COLLISION) {
            return;
        }
        Vector3f worldTranslation = emitter.getParent().getLocalTranslation();
        world.getWorldRoot().attachChild(sound);
        sound.setLocalTranslation(worldTranslation);
        sound.play();

        emitter.removeFromParent();
        world.getWorldRoot().attachChild(emitter);

        emitter.setLocalTranslation(worldTranslation);
        emitter.addControl(new CTimedExistence(4f));
        emitter.getParticleInfluencer()
                .setInitialVelocity(new Vector3f(1f, 0.01f, 1f).mult(16.0f));
        emitter.getParticleInfluencer().setVelocityVariation(1f);
        emitter.setStartSize(6f);
        emitter.setEndSize(40f);
        emitter.setLowLife(2f);
        emitter.setHighLife(2f);
        emitter.setEndColor(new ColorRGBA(1f, 1f, 0f, 0.01f));
        emitter.setShape(new EmitterSphereShape(Vector3f.ZERO, 6.0f));

        emitter.setNumParticles(20);
        emitter.emitAllParticles();
        emitter.setParticlesPerSec(0f);
        ParticleEmitter wave = createShockwave();
        world.getWorldRoot().attachChild(wave);
        wave.setLocalTranslation(worldTranslation);
        wave.emitAllParticles();
        wave.addControl(new CTimedExistence(4f));
    }

    void setEmitter(ParticleEmitter emitter) {
        this.emitter = emitter;
    }
}