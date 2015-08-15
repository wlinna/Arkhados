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
package arkhados.arena;

import arkhados.CollisionGroups;
import arkhados.World;
import com.jme3.asset.AssetManager;
import com.jme3.audio.Environment;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.light.AmbientLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import java.util.List;

public class BasicSquareArena extends AbstractArena {

    private float radius;

    @Override
    public void readWorld(World world, AssetManager assetManager) {
        super.readWorld(world, assetManager);

        this.resetWallPhysics(world.getSpace());

        final Vector3f extent = ((BoundingBox) super.getTerrainNode().getWorldBound()).getExtent(new Vector3f());
        this.radius = extent.x - 15;

        if (world.isClient()) {
            this.createLavaQuad();
            world.getClientMain().getAudioRenderer().setEnvironment(new Environment(Environment.Cavern));
        }
    }

    private void createLavaQuad() {
        Quad quad = new Quad(512, 512, true);
        Geometry geom = new Geometry("lava-terrain", quad);
        final Material lavaMaterial = super.getAssetManager().loadMaterial("Materials/LavaTerrain.j3m");
        geom.setMaterial(lavaMaterial);
        ((Node) super.getWorldManager().getWorldRoot().getChild("terrain")).attachChild(geom);

        geom.lookAt(Vector3f.UNIT_Y, Vector3f.UNIT_X);
        geom.setLocalTranslation(-256, -2, -256);

        final AmbientLight ambientLight = new AmbientLight();
        ambientLight.setColor(ColorRGBA.White.mult(0.3f));
        super.getTerrainNode().getParent().addLight(ambientLight);
    }

    @Override
    public boolean validateLocation(Vector3f location) {
        if (location.x < -radius || location.x > radius || location.z > radius || location.z < -radius) {
            return false;
        }

        return true;
    }

    private void resetWallPhysics(PhysicsSpace space) {
        List<Spatial> children = ((Node) this.getTerrainNode().getChild("Walls")).getChildren();
        for (Spatial wallNode : children) {
            Spatial wall = ((Node) wallNode).getChild("Wall");
            wall.scale(6f);

            space.removeAll(wallNode);

            CollisionShape meshShape = CollisionShapeFactory.createMeshShape(wall);

            wall.scale(1f / 6f);
            RigidBodyControl wallPhysics = new RigidBodyControl(meshShape, 0);
            wallPhysics.setCollideWithGroups(CollisionGroups.NONE);

            wallPhysics.setFriction(0.5f);
            wall.addControl(wallPhysics);
            wall.getControl(RigidBodyControl.class).setCollisionGroup(CollisionGroups.WALLS);

            space.addAll(wall);
        }
    }
}