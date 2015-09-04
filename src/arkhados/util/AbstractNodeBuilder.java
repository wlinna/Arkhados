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

import arkhados.World;
import arkhados.effects.EffectBox;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;

public abstract class AbstractNodeBuilder implements NodeBuilder {

    protected static World world;
    protected static AssetManager assets;
    private EffectBox effectBox = null;

    @Override
    public abstract Node build(BuildParameters params);

    public static void setWorld(World world) {
        AbstractNodeBuilder.world = world;
    }

    public static void setAssetManager(AssetManager assetManager) {
        AbstractNodeBuilder.assets = assetManager;
    }

    @Override
    public EffectBox getEffectBox() {
        return this.effectBox;
    }

    public void setEffectBox(EffectBox effectBox) {
        this.effectBox = effectBox;
    }
}
