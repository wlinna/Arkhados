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
package arkhados.util;

import arkhados.WorldManager;
import arkhados.effects.EffectBox;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;

/**
 *
 * @author william
 */
public abstract class AbstractNodeBuilder implements NodeBuilder {

    protected static WorldManager worldManager;
    protected static AssetManager assetManager;
    
    private EffectBox effectBox = null;

    @Override
    public abstract Node build();

    public static void setWorldManager(WorldManager worldManager) {
        AbstractNodeBuilder.worldManager = worldManager;
    }

    public static void setAssetManager(AssetManager assetManager) {
        AbstractNodeBuilder.assetManager = assetManager;
    }

    @Override
    public EffectBox getEffectBox() {
        return this.effectBox;
    }

    public void setEffectBox(EffectBox effectBox) {
        this.effectBox = effectBox;
    }
}
