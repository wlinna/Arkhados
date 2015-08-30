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

import arkhados.World;
import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

public abstract class AbstractArena {
    private Node terrain;
    private World world;
    private AssetManager assetManager;

    public void readWorld(World world, AssetManager assetManager) {
        this.world = world;
        this.assetManager = assetManager;
        this.terrain = (Node)this.world.getWorldRoot().getChild("terrain");
    }

    public abstract boolean validateLocation(Vector3f location);

    public Node getTerrainNode() {
        return terrain;
    }

    public World getWorld() {
        return world;
    }

    public abstract Vector3f getSpawnPoint(int teamId);
    
    protected AssetManager getAssetManager() {
        return assetManager;
    }
}
