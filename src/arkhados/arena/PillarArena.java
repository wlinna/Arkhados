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
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Quad;
import java.nio.FloatBuffer;
import java.util.List;

public class PillarArena extends AbstractArena {

    private final static Vector3f[] SPAWN_POINTS = new Vector3f[]{
        new Vector3f(0, 0, 100),
        new Vector3f(-100, 0, 0),
        new Vector3f(0, 0, -100),
        new Vector3f(100, 0, 0)};

    private int spawnLocationIndex;
    private float radius;

    @Override
    public void readWorld(World worldManager, AssetManager assetManager) {
        super.readWorld(worldManager, assetManager);

        resetWallPhysics(worldManager.getSpace());

        Vector3f extent = ((BoundingBox) getTerrainNode().getWorldBound())
                .getExtent(new Vector3f());
        radius = extent.x - 20;

        if (worldManager.isClient()) {
            createLava();
            worldManager.getClientMain().getAudioRenderer()
                    .setEnvironment(new Environment(Environment.Cavern));
        }
    }

    private Quad modifyTextureCoordinates(Quad quad) {
        FloatBuffer buf = quad.getFloatBuffer(VertexBuffer.Type.TexCoord);
        for (int i = 0; i < 8; i++) {
            float original = buf.get(i);
            buf.put(i, original * 2f);
        }                
        
        return quad;
    }
    
    private void createLava() {
        Mesh mesh = modifyTextureCoordinates(new Quad(1024, 1024));        
        Geometry geom = new Geometry("lava-terrain", mesh);               
        
        Material lavaMaterial = 
                getAssetManager().loadMaterial("Materials/NewLava.j3m");
        geom.setMaterial(lavaMaterial);
        ((Node) getWorld().getWorldRoot()
                .getChild("terrain")).attachChild(geom);
        
        geom.lookAt(Vector3f.UNIT_Y, Vector3f.UNIT_X);
        geom.center();
        geom.move(0, -4, 0);

        AmbientLight ambientLight = new AmbientLight();
        ambientLight.setColor(ColorRGBA.White.mult(0.3f));
        getTerrainNode().getParent().addLight(ambientLight);
    }

    @Override
    public boolean validateLocation(Vector3f location) {
        return !(location.x < -radius || location.x > radius
                || location.z > radius || location.z < -radius);
    }

    private void resetWallPhysics(PhysicsSpace space) {
        List<Spatial> children = ((Node) getTerrainNode().getChild("Walls"))
                .getChildren();
        for (Spatial wallNode : children) {
            Spatial wall = ((Node) wallNode).getChild("Grave");                                    
            
            Vector3f originalScale = wall.getLocalScale().clone();
            
            wall.setLocalScale(wallNode.getWorldScale().clone());
            
            space.removeAll(wallNode);
            
            CollisionShape meshShape = CollisionShapeFactory
                    .createMeshShape(wall);
            
            wall.setLocalScale(originalScale);

            RigidBodyControl wallPhysics = new RigidBodyControl(meshShape, 0);
            wallPhysics.setCollideWithGroups(CollisionGroups.NONE);

            wallPhysics.setFriction(0f);
            wall.addControl(wallPhysics);
            wall.getControl(RigidBodyControl.class)
                    .setCollisionGroup(CollisionGroups.WALLS);

            space.addAll(wall);
        }
    }

    @Override
    public Vector3f getSpawnPoint(int teamId) {
        spawnLocationIndex = (spawnLocationIndex + 1) % SPAWN_POINTS.length;
        return SPAWN_POINTS[spawnLocationIndex].clone();
    }
}
