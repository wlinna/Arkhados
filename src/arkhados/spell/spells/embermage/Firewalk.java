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
import arkhados.controls.CharacterPhysicsControl;
import arkhados.controls.InfluenceInterfaceControl;
import arkhados.controls.SyncInterpolationControl;
import arkhados.spell.CastSpellActionBuilder;
import arkhados.spell.Spell;
import arkhados.spell.influences.Influence;
import arkhados.util.NodeBuilder;
import arkhados.util.UserDataStrings;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.cinematic.MotionPath;
import com.jme3.cinematic.MotionPathListener;
import com.jme3.cinematic.events.MotionEvent;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author william
 */
public class Firewalk extends Spell {

    public Firewalk(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Firewalk create() {
        final float cooldown = 10f;
        final float range = 100f;
        final float castTime = 0.2f;
        final Firewalk spell = new Firewalk("Firewalk", cooldown, range, castTime);

        spell.castSpellActionBuilder = new CastSpellActionBuilder() {
            public EntityAction newAction(Vector3f vec) {
                return new CastFirewalkAction(spell, Spell.worldManager);
            }
        };

        spell.nodeBuilder = new FirewalkNodeBuilder();
        return spell;
    }

    private static class CastFirewalkAction extends EntityAction {

        private final Spell spell;
        private final WorldManager world;

        public CastFirewalkAction(Spell spell, WorldManager world) {
            this.spell = spell;
            this.world = world;
        }

        private void motion() {
            final CharacterPhysicsControl physics = super.spatial.getControl(CharacterPhysicsControl.class);
            final Vector3f startLocation = super.spatial.getLocalTranslation().clone().setY(1f);
            final long firewalkId = this.world.addNewEntity(spell.getName(), startLocation, Quaternion.IDENTITY);
            final Node firewalkNode = (Node) this.world.getEntity(firewalkId);

            final MotionPath path = new MotionPath();
            path.setPathSplineType(Spline.SplineType.Linear);
            final Vector3f finalLocation = physics.getTargetLocation().clone().setY(1f);
            path.addWayPoint(startLocation);
            path.addWayPoint(finalLocation);



            MotionEvent motionControl = new MotionEvent(firewalkNode, path);
            motionControl.setSpeed(1f);
            motionControl.setInitialDuration(finalLocation.distance(startLocation) / 105f);

            final long id = super.spatial.getUserData(UserDataStrings.ENTITY_ID);
            world.temporarilyRemoveEntity(id);
            path.addListener(new MotionPathListener() {
                public void onWayPointReach(MotionEvent motionControl, int wayPointIndex) {
                    if (path.getNbWayPoints() == wayPointIndex + 1) {
                        world.restoreTemporarilyRemovedEntity(id, finalLocation, spatial.getLocalRotation());
                        world.removeEntity(firewalkId, "");
                    }
                }
            });

            motionControl.play();
        }

        @Override
        public boolean update(float tpf) {
            this.motion();
            return false;
        }
    }

    private static class FirewalkNodeBuilder extends NodeBuilder {

        public FirewalkNodeBuilder() {
        }

        @Override
        public Node build() {
            final Sphere sphere = new Sphere(32, 32, 2f);
            final Geometry projectileGeom = new Geometry("projectile-geom", sphere);
            final Node node = new Node("firewalk");
            node.attachChild(projectileGeom);

            node.addControl(new SyncInterpolationControl());
            // TODO: Give at least bit better material
            final Material material = new Material(NodeBuilder.assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            material.setColor("Color", ColorRGBA.Yellow);
            node.setMaterial(material);

            node.setUserData(UserDataStrings.SPEED_MOVEMENT, 100f);
            node.setUserData(UserDataStrings.MASS, 0f);
            node.setUserData(UserDataStrings.DAMAGE, 50f);
            node.setUserData(UserDataStrings.IMPULSE_FACTOR, 0f);


            if (worldManager.isServer()) {
                final SphereCollisionShape collisionShape = new SphereCollisionShape(8f);

                final GhostControl ghost = new GhostControl(collisionShape);
                ghost.setCollisionGroup(GhostControl.COLLISION_GROUP_NONE);
                ghost.setCollideWithGroups(GhostControl.COLLISION_GROUP_02);

                node.addControl(ghost);

                node.addControl(new FirewalkCollisionHandler());
            }
            return node;
        }
    }
}

class FirewalkCollisionHandler extends AbstractControl {

    private GhostControl ghost;
    private Set<Long> collidedWith = new HashSet<Long>(8);

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        this.ghost = spatial.getControl(GhostControl.class);
    }

    @Override
    protected void controlUpdate(float tpf) {
        List<PhysicsCollisionObject> collisionObjects = this.ghost.getOverlappingObjects();
        for (PhysicsCollisionObject collisionObject : collisionObjects) {
            if (collisionObject.getUserObject() instanceof Spatial) {
                Spatial spatial = (Spatial) collisionObject.getUserObject();
                Long entityId = spatial.getUserData(UserDataStrings.ENTITY_ID);
                if (collidedWith.contains(entityId)) {
                    continue;
                }

                this.collidedWith.add(entityId);
                this.collisionEffect(spatial);
            }
        }
    }

    private void collisionEffect(Spatial spatial) {
        InfluenceInterfaceControl influenceInterface = spatial.getControl(InfluenceInterfaceControl.class);
        if (influenceInterface == null) {
            return;
        }
        influenceInterface.doDamage(80f);
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }
}