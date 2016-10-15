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
package arkhados.effects;

import com.jme3.asset.AssetManager;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;

public class BuffEffect {

    protected static AssetManager assets = null;

    public static void setAssetManager(AssetManager assetManager) {
        assets = assetManager;
    }

    // TODO: Get this from some central place
    private float timeLeft;

    public BuffEffect(float timeLeft) {
        this.timeLeft = timeLeft;
    }

    public void update(float tpf) {
        timeLeft -= tpf;
    }

    public void updateRender(RenderManager rm, ViewPort vp) {
    }
    
    public void setStacks(int stacks) {        
    }

    public void destroy() {
    }

    public float getTimeLeft() {
        return timeLeft;
    }
}