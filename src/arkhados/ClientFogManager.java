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
package arkhados;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.debug.Grid;
import com.jme3.scene.shape.Quad;

/**
 *
 * @author william
 */
public class ClientFogManager extends AbstractAppState {

    private Geometry rangeFogQuad = null;
    private Vector3f playerPosition = new Vector3f(60, 0, 0);
    private Node player;
    private AssetManager assetManager;
    private Material mat;
    private float updateTimer = 0.01f;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        assetManager = ((SimpleApplication) app).getAssetManager();
    }

    public void createFog(Node worldRoot) {
        Quad quad = new Quad(512, 512, true);
              
        rangeFogQuad = new Geometry("range-fog-quad", quad);
        
        mat = assetManager.loadMaterial("Materials/MyFog.j3m");
        mat.setVector3("PlayerPosition", playerPosition);             
        
        rangeFogQuad.setMaterial(mat);
            
        rangeFogQuad.lookAt(Vector3f.UNIT_Y, Vector3f.UNIT_X);
        
        rangeFogQuad.center().move(0, 10, 0);        

        rangeFogQuad.setQueueBucket(RenderQueue.Bucket.Transparent);

        worldRoot.getParent().attachChild(rangeFogQuad);           
    }

    @Override
    public void update(float tpf) {
        updateTimer += tpf;
        if (rangeFogQuad != null && player != null && updateTimer >= 0.01f) {
            rangeFogQuad.getMaterial().setVector3("PlayerPosition", player.getLocalTranslation());
            updateTimer = 0;
        }
    }

    public void setPlayerNode(Node playerNode) {
        player = playerNode;
        if (rangeFogQuad != null) {
            mat.setVector3("PlayerPosition", player.getLocalTranslation());
        }
    }
}
