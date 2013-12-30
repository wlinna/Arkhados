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

import arkhados.WorldManager;
import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.light.AmbientLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.FluidSimHeightMap;
import com.jme3.terrain.heightmap.HeightMap;
import com.jme3.terrain.heightmap.HillHeightMap;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author william
 */
public class BasicSquareArena extends AbstractArena {
    private float radius;

    @Override
    public void readWorld(WorldManager worldManager, AssetManager assetManager) {
        super.readWorld(worldManager, assetManager);

        super.getTerrainNode().scale(5f);

        final Vector3f extent = ((BoundingBox) super.getTerrainNode().getWorldBound()).getExtent(new Vector3f());
        this.radius = extent.x - 15;

        if (worldManager.isClient()) {
            this.createLavaTerrain();
        }
    }

    private void createLavaTerrain() {
        TerrainQuad lavaTerrainShape = null;
        try {
            HillHeightMap heightMap = new HillHeightMap(512, 1, 1f, 200f, (byte) 3);
            lavaTerrainShape = new TerrainQuad("lava-terrain", 65, 513, heightMap.getHeightMap());

        } catch (Exception ex) {
            Logger.getLogger(BasicSquareArena.class.getName()).log(Level.SEVERE, null, ex);
        }

        final Material lavaMaterial = super.getAssetManager().loadMaterial("Materials/LavaTerrain.j3m");
        lavaMaterial.setFloat("DiffuseMap_0_scale", 0.05f);
        lavaTerrainShape.setMaterial(lavaMaterial);
        super.getWorldManager().getWorldRoot().attachChild(lavaTerrainShape);
        lavaTerrainShape.setLocalTranslation(0f, -2f, 0f);
//        lavaTerrainShape.scale(2f);

        final AmbientLight ambientLight = new AmbientLight();
        ambientLight.setColor(ColorRGBA.White.mult(0.3f));
        super.getTerrainNode().addLight(ambientLight);
    }

    @Override
    public boolean validateLocation(Vector3f location) {
        if (location.x < -radius || location.x > radius || location.z > radius || location.z < -radius) {
            return false;
        }

        return true;
    }
}
