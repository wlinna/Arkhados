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

import arkhados.ui.hud.ClientHudManager;
import arkhados.arena.AbstractArena;
import arkhados.arena.BasicSquareArena;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.light.DirectionalLight;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.Server;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.HashMap;
import arkhados.controls.CharacterPhysicsControl;
import arkhados.controls.EntityEventControl;
import arkhados.controls.EntityVariableControl;
import arkhados.controls.SyncInterpolationControl;
import arkhados.controls.TimedExistenceControl;
import arkhados.controls.UserInputControl;
import arkhados.effects.BuffEffect;
import arkhados.messages.syncmessages.AddEntityMessage;
import arkhados.messages.syncmessages.RemoveEntityMessage;
import arkhados.messages.syncmessages.RestoreTemporarilyRemovedEntityMessage;
import arkhados.messages.syncmessages.TemporarilyRemoveEntityMessage;
import arkhados.spell.Spell;
import arkhados.spell.buffs.buffinformation.BuffInformation;
import arkhados.util.EntityFactory;
import arkhados.util.PlayerDataStrings;
import arkhados.util.UserDataStrings;
import com.jme3.bullet.collision.shapes.PlaneCollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.light.Light;
import com.jme3.math.Plane;
import com.jme3.scene.control.LightControl;
import com.jme3.scene.control.LodControl;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author william
 */
public class WorldManager extends AbstractAppState {

    // TODO: Read locations from terrain
    public final static Vector3f[] STARTING_LOCATIONS = new Vector3f[]{
        new Vector3f(-50, 0, -50),
        new Vector3f(-50, 0, 50),
        new Vector3f(50, 0, -50),
        new Vector3f(50, 0, 50)
    };
    private Node worldRoot;
    private AbstractArena arena = new BasicSquareArena();
    private HashMap<Integer, Spatial> entities = new HashMap<>();
    private SyncManager syncManager;
    private int idCounter = 0;
    private Server server;
    private boolean isClient = false;

    public WorldManager() {
    }

    public WorldManager(boolean isClient) {
        this.isClient = isClient;
    }
    private SimpleApplication app;
    private AssetManager assetManager;
    private PhysicsSpace space;
    private ViewPort viewPort;
    private Node rootNode;
    private Camera cam;
    private EntityFactory entityFactory;
    private ServerWorldCollisionListener serverCollisionListener = null;
    private ClientMain clientMain;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        TimedExistenceControl.setWorldManager(this);
        super.initialize(stateManager, app);
        this.app = (SimpleApplication) app;
        this.rootNode = this.app.getRootNode();
        this.assetManager = this.app.getAssetManager();
        this.viewPort = this.app.getViewPort();
        this.space = app.getStateManager().getState(BulletAppState.class).getPhysicsSpace();

        this.cam = this.app.getCamera();

        this.syncManager = this.app.getStateManager().getState(SyncManager.class);

        this.server = this.syncManager.getServer();

        if (this.isServer()) {
            this.serverCollisionListener = new ServerWorldCollisionListener(this, this.syncManager);
            this.space.addCollisionListener(this.serverCollisionListener);
            this.entityFactory = new EntityFactory(assetManager, this);
        } else if (this.isClient()) {
            this.clientMain = (ClientMain) app;
            this.entityFactory = new EntityFactory(this.assetManager, this, app.getStateManager().getState(ClientHudManager.class));

            // FIXME: Sometimes shader linking error happens here
//            try {
//                final FilterPostProcessor fpp = new FilterPostProcessor(this.assetManager);
//                final BloomFilter bf = new BloomFilter(BloomFilter.GlowMode.SceneAndObjects);
//                bf.setBloomIntensity(1f);
//                bf.setDownSamplingFactor(2f);
//                fpp.addFilter(bf);
//                this.viewPort.addProcessor(fpp);
//            }
//            finally {
//            }
        }

        Spell.initSpells(assetManager, this);
        BuffInformation.initBuffs();
        BuffEffect.setAssetManager(assetManager);
    }

    public void preloadModels(String[] modelNames) {
        for (String path : modelNames) {
            this.assetManager.loadModel(path);
        }
    }

    public void loadLevel() {
        this.worldRoot = (Node) this.assetManager.loadModel("Scenes/LavaArenaWithWalls.j3o");

        RigidBodyControl physics = new RigidBodyControl(new PlaneCollisionShape(new Plane(Vector3f.UNIT_Y, 0f)), 0f);
        physics.setFriction(1f);
        physics.setRestitution(0f);
        physics.setCollideWithGroups(CollisionGroups.NONE);

        this.worldRoot.getChild("Ground").addControl(physics);

        Spatial groundGeom = this.worldRoot.getChild("GroundGeom");
        LodControl lod = groundGeom.getControl(LodControl.class);

        if (lod == null) {
            lod = new LodControl();            
            groundGeom.addControl(lod);
            lod.setTrisPerPixel(0);
        }

        this.worldRoot.setName("world-root");

        this.arena.readWorld(this, this.assetManager);
    }

    public void attachLevel() {
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.39f, -0.32f, -0.74f));
        this.worldRoot.addLight(sun);

        this.space.addAll(this.worldRoot);
        this.space.setGravity(new Vector3f(0f, -98.1f, 0));
        this.rootNode.attachChild(this.worldRoot);

        this.cam.setLocation(new Vector3f(0.0f, 160.0f, 20.0f));
        this.cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
    }

    /**
     * Adds new entity on server
     *
     * @param typeId
     * @param location
     * @param rotation
     * @param playerId id of owning player. -1, if server owns
     * @return entity id
     */
    public int addNewEntity(String typeId, Vector3f location, Quaternion rotation, int playerId) {
        ++this.idCounter;
        this.addEntity(this.idCounter, typeId, location, rotation, playerId);
        return this.idCounter;
    }

    public void addEntity(int id, String modelPath, Vector3f location, Quaternion rotation, int playerId) {
        if (this.isServer()) {
            this.syncManager.broadcast(new AddEntityMessage(id, modelPath, location, rotation, playerId));
        }

        Spatial entitySpatial = this.entityFactory.createEntityById(modelPath);
        this.setEntityTranslation(entitySpatial, location, rotation);
        entitySpatial.setUserData(UserDataStrings.PLAYER_ID, playerId);
        entitySpatial.setUserData(UserDataStrings.ENTITY_ID, id);
        this.entities.put(id, entitySpatial);
        this.syncManager.addObject(id, entitySpatial);
        this.space.addAll(entitySpatial);

        // We need to add GhostControl separately
        final GhostControl ghostControl = entitySpatial.getControl(GhostControl.class);
        if (ghostControl != null) {
            this.space.add(ghostControl);
        }

        this.worldRoot.attachChild(entitySpatial);
        EntityVariableControl variableControl = new EntityVariableControl(this);
        entitySpatial.addControl(variableControl);

        if (entitySpatial.getControl(CharacterPhysicsControl.class) != null && PlayerData.isHuman(playerId)) {
            entitySpatial.addControl(new UserInputControl());
        }

        if (this.isClient()) {
            this.clientMain.getUserCommandManager().trySetPlayersCharacter(entitySpatial);
        }
    }

    public void temporarilyRemoveEntity(int id) {
        if (this.isServer()) {
            this.server.broadcast(new TemporarilyRemoveEntityMessage(id));
        }
        Spatial spatial = this.getEntity(id);
        spatial.removeFromParent();
        this.syncManager.removeEntity(id);

        CharacterPhysicsControl characterPhysics = spatial.getControl(CharacterPhysicsControl.class);
        if (characterPhysics != null) {
            characterPhysics.setEnabled(false);
        }
    }

    public void restoreTemporarilyRemovedEntity(int id, Vector3f location, Quaternion rotation) {
        if (this.isServer()) {
            this.server.broadcast(new RestoreTemporarilyRemovedEntityMessage(id, location, rotation));
        }
        Spatial spatial = this.getEntity(id);
        this.worldRoot.attachChild(spatial);
        this.syncManager.addObject(id, spatial);

        CharacterPhysicsControl characterPhysics = spatial.getControl(CharacterPhysicsControl.class);
        if (characterPhysics != null) {
            characterPhysics.setEnabled(true);
        }

        SyncInterpolationControl interpolationControl = spatial.getControl(SyncInterpolationControl.class);
        if (interpolationControl != null) {
            interpolationControl.ignoreNext();
        }

        this.setEntityTranslation(spatial, location, rotation);

        if (this.isClient()) {
            this.app.getStateManager().getState(UserCommandManager.class).sendWalkDirection();
        }
    }

    private void setEntityTranslation(Spatial entityModel, Vector3f location, Quaternion rotation) {
        if (entityModel.getControl(RigidBodyControl.class) != null) {
            entityModel.getControl(RigidBodyControl.class).setPhysicsLocation(location);
            entityModel.getControl(RigidBodyControl.class).setPhysicsRotation(rotation.toRotationMatrix());
        } else if (entityModel.getControl(CharacterPhysicsControl.class) != null) {
            entityModel.getControl(CharacterPhysicsControl.class).warp(location);
            entityModel.setLocalTranslation(location);
            entityModel.getControl(CharacterPhysicsControl.class).setViewDirection(rotation.mult(Vector3f.UNIT_Z).setY(0).normalizeLocal());
        } else {
            entityModel.setLocalTranslation(location);
            entityModel.setLocalRotation(rotation);
        }
    }

    public void removeEntity(int id, String reason) {
        if (this.isServer()) {
            this.syncManager.broadcast(new RemoveEntityMessage(id, reason));
        }
        this.syncManager.removeEntity(id);
        Spatial spatial = this.entities.remove(id);
        if (spatial == null) {
            return;
        }

        if (this.isClient()) {
            if (!"".equals(reason)) {
                EntityEventControl eventControl = spatial.getControl(EntityEventControl.class);
                if (eventControl != null) {
                    eventControl.getOnRemoval().exec(this, reason);
                }
            }
        }
        spatial.removeFromParent();
        LightControl lightControl = spatial.getControl(LightControl.class);
        if (lightControl != null) {
            Light light = lightControl.getLight();
            if (light != null) {
                this.getWorldRoot().removeLight(light);
            }
        }
        this.space.removeAll(spatial);
        final GhostControl ghostControl = spatial.getControl(GhostControl.class);
        if (ghostControl != null) {
            this.space.remove(ghostControl);
        }
    }

    public static List<SpatialDistancePair> getSpatialsWithinDistance(Spatial spatial, float distance) {
        Node worldRoot = spatial.getParent();
        while (!"world-root".equals(worldRoot.getName())) {
            worldRoot = worldRoot.getParent();
            if (worldRoot == null) {
                // Consider throwing exception here
                return null;
            }
        }

        List<SpatialDistancePair> spatialDistancePairs = new LinkedList<SpatialDistancePair>();
        for (Spatial child : worldRoot.getChildren()) {
            float distanceBetween = child.getWorldTranslation().distance(spatial.getWorldTranslation());
            if (distanceBetween > distance) {
                continue;
            }
            if (child == spatial) {
                continue;
            }
            spatialDistancePairs.add(new SpatialDistancePair(child, distanceBetween));
        }
        return spatialDistancePairs;
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
    }

    public SyncManager getSyncManager() {
        return this.syncManager;
    }

    public boolean isServer() {
        return this.server != null;
    }

    public boolean isClient() {
        return this.isClient;
    }

    public void clear() {
        for (PlayerData playerData : PlayerData.getPlayers()) {
            playerData.setData(PlayerDataStrings.ENTITY_ID, -1l);
        }
        if (this.worldRoot != null) {
            this.space.removeAll(this.worldRoot);
            this.rootNode.detachChild(this.worldRoot);
        }
        this.entities.clear();
        this.syncManager.clear();

        this.idCounter = 0;

        this.worldRoot = null;
    }

    @Override
    public void cleanup() {
        super.cleanup();
        if (this.worldRoot != null) {
            this.rootNode.detachChild(this.worldRoot);
        }
    }

    public Node getWorldRoot() {
        return this.worldRoot;
    }

    public Spatial getEntity(int id) {
        return this.entities.get(id);
    }

    public boolean validateLocation(Vector3f location) {
        return this.arena.validateLocation(location);
    }

    public ClientMain getClientMain() {
        return clientMain;
    }

    void preloadSoundEffects(String[] string) {
        for (String audioPath : string) {
            this.assetManager.loadAudio("Effects/Sound/" + audioPath);
        }
    }

    public PhysicsSpace getSpace() {
        return space;
    }
}