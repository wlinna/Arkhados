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

import arkhados.CharacterInteraction;
import arkhados.CollisionGroups;
import arkhados.World;
import arkhados.actions.EntityAction;
import arkhados.characters.EmberMage;
import arkhados.controls.CCharacterPhysics;
import arkhados.controls.CEntityVariable;
import arkhados.controls.CGenericSync;
import arkhados.controls.CInfluenceInterface;
import arkhados.controls.PlayerEntityAwareness;
import arkhados.controls.CSpellBuff;
import arkhados.controls.CSpellCast;
import arkhados.controls.CSyncInterpolation;
import arkhados.effects.particle.ParticleEmitter;
import arkhados.spell.Spell;
import arkhados.spell.buffs.AbstractBuffBuilder;
import arkhados.spell.buffs.SlowCC;
import arkhados.util.AbstractNodeBuilder;
import arkhados.util.BuildParameters;
import arkhados.util.PathCheck;
import arkhados.util.UserData;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.cinematic.MotionPath;
import com.jme3.cinematic.events.MotionEvent;
import com.jme3.effect.ParticleMesh;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Spline;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.shape.Sphere;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Firewalk extends Spell {

    {
        iconName = "flame_walk.png";
    }

    public Firewalk(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Firewalk create() {
        final float cooldown = 9f;
        final float range = 90f;
        final float castTime = 0.15f;
        final Firewalk spell
                = new Firewalk("Firewalk", cooldown, range, castTime);

        spell.castSpellActionBuilder = (Node caster, Vector3f vec) -> {
            ACastFirewalk castAction = new ACastFirewalk(spell, world);
            AbstractBuffBuilder ignite
                    = Ignite.ifNotCooldownCreateDamageOverTimeBuff(caster);
            if (ignite != null) {
                castAction.additionalBuffs.add(ignite);
            }
            return castAction;
        };

        spell.nodeBuilder = new FirewalkNodeBuilder();
        return spell;
    }

    private static class ACastFirewalk extends EntityAction {

        private final Spell spell;
        private final List<AbstractBuffBuilder> additionalBuffs
                = new ArrayList<>();
        private final World world;

        public ACastFirewalk(Spell spell, World world) {
            this.spell = spell;
            this.world = world;
            super.setTypeId(EmberMage.ACTION_FIREWALK);
        }

        public void addAdditionalBuff(AbstractBuffBuilder buff) {
            if (buff == null) {
                throw new IllegalArgumentException("Nulls are not allowed "
                        + "for buff collection");
            }
            additionalBuffs.add(buff);
        }

        private void motion() {
            Vector3f startLocation
                    = spatial.getLocalTranslation().add(0, 1f, 0);
            int playerId = spatial.getUserData(UserData.PLAYER_ID);
            final int firewalkId = world.addNewEntity(spell.getId(),
                    startLocation, Quaternion.IDENTITY, playerId);
            Spatial firewalkNode = world.getEntity(firewalkId);

            final PlayerEntityAwareness awareness
                    = spatial.getControl(CEntityVariable.class).getAwareness();
            awareness.setOwnSpatial(firewalkNode);

            CSpellBuff buffControl = firewalkNode.getControl(CSpellBuff.class);
            buffControl.setOwnerInterface(spatial
                    .getControl(CInfluenceInterface.class));
            buffControl.getBuffs().addAll(additionalBuffs);

            final MotionPath path = new MotionPath();
            path.setPathSplineType(Spline.SplineType.Linear);

            float room = spatial.getControl(CCharacterPhysics.class)
                    .getCapsuleShape().getRadius();

            CSpellCast castControl = spatial.getControl(CSpellCast.class);
            Spatial walls = world.getWorldRoot().getChild("Walls");
            final Vector3f finalLocation = PathCheck.closestNonColliding(walls,
                    startLocation,
                    castControl.getClosestPointToTarget(spell).add(0, 1f, 0),
                    room);

            path.addWayPoint(startLocation);
            path.addWayPoint(finalLocation);

            MotionEvent motionControl = new MotionEvent(firewalkNode, path);
            motionControl.setSpeed(1f);
            motionControl.setInitialDuration(
                    finalLocation.distance(startLocation) / 105f);

            final int id = spatial.getUserData(UserData.ENTITY_ID);
            world.temporarilyRemoveEntity(id);
            path.addListener((MotionEvent cMotion, int wayPointIndex) -> {
                if (path.getNbWayPoints() == wayPointIndex + 1) {
                    world.restoreTemporarilyRemovedEntity(id, finalLocation,
                            spatial.getLocalRotation());
                    world.removeEntity(firewalkId, -1);
                    awareness.setOwnSpatial(spatial);
                }
            });

            motionControl.play();
        }

        @Override
        public boolean update(float tpf) {
            motion();
            return false;
        }
    }

    private static class FirewalkNodeBuilder extends AbstractNodeBuilder {

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
            fire.setStartColor(new ColorRGBA(0.95f, 0.650f, 0.0f, 1.0f));
            fire.setEndColor(new ColorRGBA(1.0f, 1.0f, 0.0f, 0.1f));
            fire.getParticleInfluencer().setInitialVelocity(Vector3f.ZERO);
            fire.setStartSize(4.5f);
            fire.setEndSize(8.5f);
            fire.setGravity(Vector3f.ZERO);
            fire.setLowLife(0.4f);
            fire.setHighLife(0.4f);
            fire.setParticlesPerSec(30);
            return fire;
        }

        @Override
        public Node build(BuildParameters params) {
            Sphere sphere = new Sphere(16, 16, 0.2f);
            Geometry projectileGeom = new Geometry("projectile-geom", sphere);
            Node node = new Node("firewalk");
            node.setLocalTranslation(params.location);
            node.attachChild(projectileGeom);

            node.addControl(new CSyncInterpolation());
            // TODO: Give at least bit better material
            Material material = new Material(assets,
                    "Common/MatDefs/Misc/Unshaded.j3md");
            material.setColor("Color", ColorRGBA.Yellow);
            node.setMaterial(material);

            node.setUserData(UserData.SPEED_MOVEMENT, 100f);
            node.setUserData(UserData.MASS, 0f);
            node.setUserData(UserData.DAMAGE, 50f);
            node.setUserData(UserData.IMPULSE_FACTOR, 0f);
            node.setUserData(UserData.FOLLOW_ME, true);

            CSpellBuff buffControl = new CSpellBuff();
            AbstractBuffBuilder slowCC = new SlowCC.MyBuilder(1f, 0.2f);
            buffControl.addBuff(slowCC);
            node.addControl(buffControl);

            node.addControl(new CGenericSync());

            if (world.isServer()) {
                SphereCollisionShape shape = new SphereCollisionShape(8f);

                GhostControl ghost = new GhostControl(shape);
                ghost.setCollisionGroup(CollisionGroups.NONE);
                ghost.setCollideWithGroups(CollisionGroups.CHARACTERS);

                node.addControl(ghost);

                node.addControl(new CFirewalkCollisionHandler());
            }
            if (AbstractNodeBuilder.world.isClient()) {
                final ParticleEmitter fire = createFireEmitter();
                node.attachChild(fire);
            }
            return node;
        }
    }
}

class CFirewalkCollisionHandler extends AbstractControl {

    private GhostControl ghost;
    private final Set<Integer> collidedWith = new HashSet<>(8);

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        if (spatial == null) {
            return;
        }
        ghost = spatial.getControl(GhostControl.class);
    }

    @Override
    protected void controlUpdate(float tpf) {
        List<PhysicsCollisionObject> collisionObjects
                = ghost.getOverlappingObjects();
        for (PhysicsCollisionObject collisionObject : collisionObjects) {
            if (collisionObject.getUserObject() instanceof Spatial) {
                Spatial spatial = (Spatial) collisionObject.getUserObject();
                Integer entityId = spatial.getUserData(UserData.ENTITY_ID);
                if (collidedWith.contains(entityId)) {
                    continue;
                }

                collidedWith.add(entityId);
                collisionEffect(spatial);
            }
        }
    }

    private void collisionEffect(Spatial target) {
        CInfluenceInterface targetInterface
                = target.getControl(CInfluenceInterface.class);
        if (targetInterface == null) {
            return;
        }
        
        int myTeam = spatial.getUserData(UserData.TEAM_ID);
        if (target.getUserData(UserData.TEAM_ID).equals(myTeam)) {
            return;
        }

        CSpellBuff buffControl = spatial.getControl(CSpellBuff.class);
        CharacterInteraction.harm(buffControl.getOwnerInterface(),
                targetInterface, 80f, buffControl.getBuffs(), true);
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }
}
