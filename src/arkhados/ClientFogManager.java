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
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.SceneProcessor;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 *
 * @author william
 */
public class ClientFogManager extends AbstractAppState implements SceneProcessor {

    private Geometry rangeFogQuad = null;
    private Vector3f playerPosition = new Vector3f(60, 0, 0);
    private Node player;
    private AssetManager assetManager;
    private Material mat;
    private FrameBuffer fogFb;
    private RenderManager renderManager;
    private Texture2D colorTexture;
    private ArrayList<Geometry> occluders = null;
    private Material fogShapeMaterial;
    private Material fogProcessMaterial;
    private ViewPort viewPort;
    private SimpleApplication app;
    private Geometry screenQuad = null;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.app = (SimpleApplication) app;
        assetManager = this.app.getAssetManager();
    }

    public void createFog(Node worldRoot) {
        Quad quad = new Quad(512, 512, true);

        rangeFogQuad = new Geometry("range-fog-quad", quad);

        mat = assetManager.loadMaterial("Materials/RangeFog.j3m");
        mat.setVector3("PlayerPosition", playerPosition);

        rangeFogQuad.setMaterial(mat);

        rangeFogQuad.lookAt(Vector3f.UNIT_Y, Vector3f.UNIT_X);

        rangeFogQuad.center().move(0, 10, 0);

        rangeFogQuad.setQueueBucket(RenderQueue.Bucket.Transparent);

        worldRoot.getParent().attachChild(rangeFogQuad);

        Node walls = (Node) worldRoot.getChild("Walls");

        occluders = new ArrayList<>();

        Material dummyMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        dummyMat.setColor("Color", ColorRGBA.BlackNoAlpha);

        for (Spatial wall : walls.getChildren()) {
            Node wallNode = (Node) wall;
            Geometry orig = (Geometry) wallNode.getChild("FogMeshGeom1");
            Geometry fogGeom = (Geometry) orig.deepClone();
            orig.removeFromParent();
            wallNode.attachChild(fogGeom);
                        
            occluders.add(fogGeom);
            fogGeom.setCullHint(Spatial.CullHint.Always);

            doubleVertices(fogGeom);

            fogGeom.setMaterial(dummyMat);
        }
    }

    private void doubleVertices(Geometry geometry) {
        Mesh mesh = geometry.getMesh();

        // Vertices       
        VertexBuffer oldPositionVertexBuffer = mesh.getBuffer(VertexBuffer.Type.Position);

        int oldVertexCount = oldPositionVertexBuffer.getNumElements();

        VertexBuffer.Format positionFormat = oldPositionVertexBuffer.getFormat();

        Buffer positionBuffer = VertexBuffer.createBuffer(VertexBuffer.Format.Float, 4,
                oldVertexCount * 2);

        VertexBuffer newPositionVertexBuffer = new VertexBuffer(VertexBuffer.Type.Position);
        newPositionVertexBuffer.setupData(VertexBuffer.Usage.Static,
                4, positionFormat, positionBuffer);

        for (int i = 0; i < oldVertexCount; ++i) {
            float x = (float) oldPositionVertexBuffer.getElementComponent(i, 0);
            float y = (float) oldPositionVertexBuffer.getElementComponent(i, 1);
            float z = (float) oldPositionVertexBuffer.getElementComponent(i, 2);

            newPositionVertexBuffer.setElementComponent(i, 0, x);
            newPositionVertexBuffer.setElementComponent(i, 1, y);
            newPositionVertexBuffer.setElementComponent(i, 2, z);
            newPositionVertexBuffer.setElementComponent(i, 3, 0f);

            int copyVertexIndex = i + oldVertexCount;

            newPositionVertexBuffer.setElementComponent(copyVertexIndex, 0, x);
            newPositionVertexBuffer.setElementComponent(copyVertexIndex, 1, y);
            newPositionVertexBuffer.setElementComponent(copyVertexIndex, 2, z);
            newPositionVertexBuffer.setElementComponent(copyVertexIndex, 3, 1f);
        }

        mesh.clearBuffer(VertexBuffer.Type.Position);
        mesh.setBuffer(newPositionVertexBuffer);

        // Indices
        VertexBuffer oldIndexVertexBuffer = mesh.getBuffer(VertexBuffer.Type.Index);
        VertexBuffer.Format indexFormat = oldIndexVertexBuffer.getFormat();

        List<Short> connectionIndices = new ArrayList<>();

        int skipCounter = 0;

        boolean skipNext = false;
        for (int i = 0; i < oldVertexCount; i++) {
            if (skipNext) {
                skipCounter = 0;
                skipNext = false;
                continue;
            }

            short o1 = (short) oldIndexVertexBuffer.getElementComponent(i, 0);
            short o2 = (short) oldIndexVertexBuffer.getElementComponent(i + 1, 0);
            short c1 = (short) (o1 + oldVertexCount);
            short c2 = (short) (o2 + oldVertexCount);

            connectionIndices.add(o1);
            connectionIndices.add(c1);
            connectionIndices.add(o2);

            connectionIndices.add(o2);
            connectionIndices.add(c1);
            connectionIndices.add(c2);

            ++skipCounter;
            if (skipCounter == 2) {
                skipNext = true;
            }
        }

        Buffer indexBuffer = VertexBuffer.createBuffer(indexFormat,
                oldIndexVertexBuffer.getNumComponents(),
                oldIndexVertexBuffer.getNumElements() * 2 + connectionIndices.size());

        VertexBuffer newIndexVertexBuffer = new VertexBuffer(VertexBuffer.Type.Index);
        newIndexVertexBuffer.setupData(VertexBuffer.Usage.Static,
                oldIndexVertexBuffer.getNumComponents(), indexFormat, indexBuffer);

        oldIndexVertexBuffer.copyElements(0, newIndexVertexBuffer, 0,
                oldIndexVertexBuffer.getNumElements());
        oldIndexVertexBuffer.copyElements(0, newIndexVertexBuffer,
                oldIndexVertexBuffer.getNumElements(), oldIndexVertexBuffer.getNumElements());

        for (int i = oldIndexVertexBuffer.getNumElements(); i < oldIndexVertexBuffer.getNumElements() * 2; i++) {
            short oldValue = (short) (Short) newIndexVertexBuffer.getElementComponent(i, 0);
            int newValue = oldValue + oldVertexCount;
            newIndexVertexBuffer.setElementComponent(i, 0, (short) newValue);
        }

        for (int i = 0; i < connectionIndices.size(); i++) {
            short index = (short) (int) connectionIndices.get(i);
            newIndexVertexBuffer.setElementComponent(oldIndexVertexBuffer.getNumElements() * 2 + i,
                    0, index);
        }

        mesh.clearBuffer(VertexBuffer.Type.Index);
        mesh.setBuffer(newIndexVertexBuffer);

        // Normals
        VertexBuffer oldNormalVertexBuffer = mesh.getBuffer(VertexBuffer.Type.Normal);
        VertexBuffer.Format normalFormat = oldNormalVertexBuffer.getFormat();

        Buffer normalBuffer = VertexBuffer.createBuffer(normalFormat,
                oldNormalVertexBuffer.getNumComponents(),
                oldNormalVertexBuffer.getNumElements() * 2);

        VertexBuffer newNormalVertexBuffer = new VertexBuffer(VertexBuffer.Type.Normal);
        newNormalVertexBuffer.setupData(VertexBuffer.Usage.Static, 3, normalFormat, normalBuffer);

        oldNormalVertexBuffer.copyElements(0, newNormalVertexBuffer,
                0, oldNormalVertexBuffer.getNumElements());
        oldNormalVertexBuffer.copyElements(0, newNormalVertexBuffer,
                oldNormalVertexBuffer.getNumElements(), oldNormalVertexBuffer.getNumElements());

        mesh.clearBuffer(VertexBuffer.Type.Normal);
        mesh.setBuffer(newNormalVertexBuffer);

        mesh.updateCounts();
    }

    @Override
    public void update(float tpf) {
    }

    @Override
    public void render(RenderManager rm) {
    }

    public void setPlayerNode(Node playerNode) {
        player = playerNode;
        if (rangeFogQuad != null) {
            mat.setVector3("PlayerPosition", player.getLocalTranslation());
        }
        if (fogShapeMaterial != null && player != null) {
            fogShapeMaterial.setVector3("PlayerPosition", player.getLocalTranslation());
        }
    }

    @Override
    public void initialize(RenderManager rm, ViewPort vp) {
        renderManager = rm;
        viewPort = vp;

        fogShapeMaterial = assetManager.loadMaterial("Materials/OccluderFogShape.j3m");
        fogProcessMaterial = assetManager.loadMaterial("Materials/OccluderFogProcess.j3m");

        fogShapeMaterial.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
        fogShapeMaterial.getAdditionalRenderState().setDepthWrite(false);
        fogShapeMaterial.getAdditionalRenderState().setDepthTest(false);

        int width = 1024;
        int height = 1024;

        // TODO: Find a way to use custom width and height.
        // If for example 1024, 1024 is used, screen ends up looking wrong and mouse doesn't work correctly
        // If resolution is big and I use screen width and height, performance suffers
        width = viewPort.getCamera().getWidth();
        height = viewPort.getCamera().getHeight();

        fogFb = new FrameBuffer(width, height, 1);
        colorTexture = new Texture2D(width, height, Image.Format.RGBA8);
        fogFb.setColorTexture(colorTexture);
//        fogFb.setDepthTexture(new Texture2D(width, height, Image.Format.Depth));
        fogProcessMaterial.setTexture("FogShape", colorTexture);

        app.enqueue(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Quad front = new Quad(viewPort.getCamera().getWidth(), viewPort.getCamera().getHeight());
                screenQuad = new Geometry("front-quad", front);
                screenQuad.setMaterial(fogProcessMaterial);
                ((SimpleApplication) app).getRootNode().attachChild(screenQuad);
                screenQuad.setQueueBucket(RenderQueue.Bucket.Gui);
                return null;
            }
        });

        initialized = true;
    }

    @Override
    public boolean isInitialized() {
        boolean appStateInitialized = super.isInitialized();
        if (appStateInitialized) {
            return viewPort != null && occluders != null;
        }

        return false;
    }

    @Override
    public void reshape(ViewPort vp, int w, int h) {
    }

    @Override
    public void preFrame(float tpf) {
    }

    @Override
    public void postQueue(RenderQueue rq) {
        if (occluders == null) {
            return;
        }
        Renderer renderer = renderManager.getRenderer();

        renderer.setFrameBuffer(fogFb);
        renderer.clearBuffers(true, true, true);

        renderManager.setForcedMaterial(fogShapeMaterial);

        for (Geometry geometry : occluders) {
            renderManager.renderGeometry(geometry);
        }

        renderManager.setForcedMaterial(null);
        renderer.setFrameBuffer(viewPort.getOutputFrameBuffer());
    }

    @Override
    public void postFrame(FrameBuffer out) {
    }

    @Override
    public void cleanup() {
        super.cleanup();

        renderManager = null;
        viewPort = null;

        mat = null;
        fogProcessMaterial = null;
        fogShapeMaterial = null;

        if (fogFb != null) {
            fogFb.dispose();
        }
        fogFb = null;

        if (rangeFogQuad != null) {
            rangeFogQuad.removeFromParent();
        }

        rangeFogQuad = null;

        if (screenQuad != null) {
            screenQuad.removeFromParent();
        }
        screenQuad = null;

        player = null;
        occluders = null;
    }
}
