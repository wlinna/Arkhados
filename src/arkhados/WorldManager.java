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
import arkhados.controls.CCharacterBuff;
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
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.HashMap;
import arkhados.controls.CCharacterPhysics;
import arkhados.controls.CEntityEvent;
import arkhados.controls.CEntityVariable;
import arkhados.controls.CSyncInterpolation;
import arkhados.controls.CTimedExistence;
import arkhados.controls.CUserInput;
import arkhados.effects.BuffEffect;
import arkhados.messages.syncmessages.CmdAddEntity;
import arkhados.messages.syncmessages.CmdRemoveEntity;
import arkhados.net.Sender;
import arkhados.spell.Spell;
import arkhados.spell.buffs.buffinformation.BuffInformation;
import arkhados.util.EntityFactory;
import arkhados.util.PlayerDataStrings;
import arkhados.util.RemovalReasons;
import arkhados.util.Selector;
import arkhados.util.UserDataStrings;
import com.jme3.bullet.collision.shapes.PlaneCollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.debug.BulletDebugAppState;
import com.jme3.light.Light;
import com.jme3.math.Plane;
import com.jme3.scene.control.LightControl;
import com.jme3.scene.control.LodControl;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author william
 */
public class WorldManager extends AbstractAppState {

    private final static Logger logger =
            Logger.getLogger(WorldManager.class.getName());

    static {
        logger.setLevel(Level.WARNING);
    }
    // TODO: Read locations from terrain
    public final static Vector3f[] STARTING_LOCATIONS = new Vector3f[]{
        new Vector3f(-50, 0, -50),
        new Vector3f(-50, 0, 50),
        new Vector3f(50, 0, -50),
        new Vector3f(50, 0, 50),
        new Vector3f(0, 0, 0)
    };
    private Node worldRoot;
    private AbstractArena arena = new BasicSquareArena();
    private HashMap<Integer, Spatial> entities = new HashMap<>();
    private SyncManager syncManager;
    private int idCounter = 0;
    private boolean isClient = false;
    private EffectHandler effectHandler = null;

    public WorldManager() {
    }

    public WorldManager(EffectHandler effectHandler) {
        this.effectHandler = effectHandler;
        isClient = true;
    }
    private SimpleApplication app;
    private AssetManager assetManager;
    private PhysicsSpace space;
    private Node rootNode;
    private Camera cam;
    private EntityFactory entityFactory;
    private ServerWorldCollisionListener serverCollisionListener = null;
    private ClientMain clientMain;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        CTimedExistence.setWorldManager(this);
        Selector.setWorldManager(this);
        super.initialize(stateManager, app);
        this.app = (SimpleApplication) app;
        rootNode = this.app.getRootNode();
        assetManager = app.getAssetManager();
        space = app.getStateManager().getState(BulletAppState.class)
                .getPhysicsSpace();

        cam = app.getCamera();

        syncManager = app.getStateManager().getState(SyncManager.class);

        Sender sender = stateManager.getState(Sender.class);

        if (sender.isServer()) {
            serverCollisionListener =
                    new ServerWorldCollisionListener(this, syncManager);
            space.addCollisionListener(serverCollisionListener);
            entityFactory = new EntityFactory(assetManager, this);
        } else if (isClient()) {
            clientMain = (ClientMain) app;
            entityFactory = new EntityFactory(assetManager, this,
                    app.getStateManager().getState(ClientHudManager.class),
                    effectHandler);

            // FIXME: Shader linking error happens here with Intel GPU's. 
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

//            app.getStateManager().attach(new BulletDebugAppState(space));
        }

        Spell.initSpells(entityFactory, assetManager, this);
        BuffInformation.initBuffs();
        BuffEffect.setAssetManager(assetManager);
    }

    public void loadLevel() {
        worldRoot = (Node) assetManager.loadModel(
                "Scenes/LavaArenaWithFogWalls.j3o");

        RigidBodyControl physics = new RigidBodyControl(
                new PlaneCollisionShape(new Plane(Vector3f.UNIT_Y, 0)), 0);
        physics.setFriction(1f);
        physics.setRestitution(0f);
        physics.setCollideWithGroups(CollisionGroups.NONE);

        worldRoot.getChild("Ground").addControl(physics);

        Spatial groundGeom = worldRoot.getChild("GroundGeom");
        LodControl lod = groundGeom.getControl(LodControl.class);

        if (lod == null) {
            lod = new LodControl();
            groundGeom.addControl(lod);
            lod.setTrisPerPixel(0);
        }

        worldRoot.setName("world-root");

        arena.readWorld(this, assetManager);
    }

    public void attachLevel() {
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.39f, -0.32f, -0.74f));
        worldRoot.addLight(sun);

        space.addAll(worldRoot);
        space.setGravity(new Vector3f(0, -98.1f, 0));
        rootNode.attachChild(worldRoot);

        cam.setLocation(new Vector3f(0, 160, 20));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);

        if (isServer()) {
            app.getStateManager().getState(ServerFogManager.class)
                    .setWalls((Node) worldRoot.getChild("Walls"));
        } else {
            ClientFogManager clientFogManager =
                    app.getStateManager().getState(ClientFogManager.class);
            app.getViewPort().addProcessor(clientFogManager);
            clientFogManager.createFog(worldRoot);
        }

        UserCommandManager userCommandManager =
                app.getStateManager().getState(UserCommandManager.class);
        if (userCommandManager != null) {
            userCommandManager.createCameraControl();
        }
    }

    /**
     * Adds new entity on server
     *
     * @param nodeBuilderId
     * @param location
     * @param rotation
     * @param playerId id of owning player. -1, if server owns
     * @return entity id
     */
    public int addNewEntity(int nodeBuilderId, Vector3f location,
            Quaternion rotation, int playerId) {
        ++idCounter;
        addEntity(idCounter, nodeBuilderId, location, rotation, playerId);
        return idCounter;
    }

    public void addEntity(int id, int nodeBuilderId, Vector3f location,
            Quaternion rotation, int playerId) {
        Sender sender = app.getStateManager().getState(Sender.class);

        Spatial entity =
                entityFactory.createEntityById(nodeBuilderId, location);
        setEntityTranslation(entity, location, rotation);
        entity.setUserData(UserDataStrings.PLAYER_ID, playerId);
        entity.setUserData(UserDataStrings.ENTITY_ID, id);
        entity.setUserData(UserDataStrings.INVISIBLE_TO_ALL, false);
        entity.setUserData(UserDataStrings.INVISIBLE_TO_ENEMY, false);
        int teamId = PlayerData.getIntData(playerId, PlayerDataStrings.TEAM_ID);
        entity.setUserData(UserDataStrings.TEAM_ID, teamId);

        entities.put(id, entity);
        syncManager.addObject(id, entity);
        space.addAll(entity);

        // We need to add GhostControl separately
        final GhostControl ghostControl = entity.getControl(GhostControl.class);
        if (ghostControl != null) {
            space.add(ghostControl);
        }

        worldRoot.attachChild(entity);
        CEntityVariable variableControl =
                new CEntityVariable(this, sender);
        entity.addControl(variableControl);

        boolean isCharacter =
                entity.getControl(CCharacterPhysics.class) != null;

        if (isCharacter && PlayerData.isHuman(playerId)) {
            logger.log(Level.INFO, "Adding entity {0} for player {1}",
                    new Object[]{id, playerId});
            entity.setUserData(UserDataStrings.FOLLOW_ME, true);

            CUserInput userInputControl = new CUserInput();
            if (isServer()) {
                ServerPlayerInputState inputState = ServerPlayerInputHandler
                        .get().getPlayerInputState(playerId);
                inputState.currentActiveSpatial = entity;
                userInputControl.setInputState(inputState);
            } else {
                ClientHudManager hudManager =
                        app.getStateManager().getState(ClientHudManager.class);
                hudManager.addCharacter(entity);
            }
            entity.addControl(userInputControl);
        }

        boolean followMe = entity.getUserDataKeys()
                .contains(UserDataStrings.FOLLOW_ME);

        ServerFogManager serverFogManager =
                app.getStateManager().getState(ServerFogManager.class);
        if (serverFogManager != null) {
            if (isCharacter) {
                serverFogManager.registerCharacterForPlayer(playerId, entity);
            }

            serverFogManager.createNewEntity(entity,
                    new CmdAddEntity(id, nodeBuilderId,
                    location, rotation, playerId));
        }

        if (isClient()) {
            UserCommandManager userCommandManager = app.getStateManager()
                    .getState(UserCommandManager.class);
            boolean ownedByMe =
                    userCommandManager.trySetPlayersCharacter(entity);

            if (ownedByMe) {
                app.getStateManager().getState(ClientFogManager.class)
                        .setPlayerNode(entity);
                logger.log(Level.INFO,
                        "Setting player''s node. Id {0}, playerId {1}",
                        new Object[]{id, playerId});
            } else if (playerId == userCommandManager.getPlayerId()
                    && followMe) {
                app.getStateManager().getState(ClientFogManager.class)
                        .setPlayerNode(entity);
                userCommandManager.followSpatial(entity);
            }
        }
    }

    public void temporarilyRemoveEntity(int id) {
        logger.log(Level.FINE, "Temporarily removing entity with id {0}", id);
        Spatial spatial = getEntity(id);
        spatial.setUserData(UserDataStrings.INVISIBLE_TO_ALL, true);
        spatial.removeFromParent();
        syncManager.removeEntity(id);

        CCharacterPhysics characterPhysics =
                spatial.getControl(CCharacterPhysics.class);
        if (characterPhysics != null) {
            characterPhysics.setEnabled(false);
        }
    }

    public void restoreTemporarilyRemovedEntity(int id, Vector3f location,
            Quaternion rotation) {
        logger.log(Level.FINE,
                "Restoring temporarily removed entity. Id {0}", id);
        Spatial spatial = getEntity(id);
        spatial.setUserData(UserDataStrings.INVISIBLE_TO_ALL, false);
        worldRoot.attachChild(spatial);
        syncManager.addObject(id, spatial);

        CCharacterPhysics characterPhysics =
                spatial.getControl(CCharacterPhysics.class);
        if (characterPhysics != null) {
            characterPhysics.setEnabled(true);
        }

        CSyncInterpolation interpolationControl =
                spatial.getControl(CSyncInterpolation.class);
        if (interpolationControl != null) {
            interpolationControl.ignoreNext();
        }

        setEntityTranslation(spatial, location, rotation);
    }

    private void setEntityTranslation(Spatial entity, Vector3f location,
            Quaternion rotation) {
        RigidBodyControl rigid = entity.getControl(RigidBodyControl.class);
        if (rigid != null && !rigid.isKinematic()) {
            rigid.setPhysicsLocation(location);
            rigid.setPhysicsRotation(rotation.toRotationMatrix());
        } else if (entity.getControl(CCharacterPhysics.class) != null) {
            entity.getControl(CCharacterPhysics.class).warp(location);
            entity.setLocalTranslation(location);
            entity.getControl(CCharacterPhysics.class).setViewDirection(
                    rotation.mult(Vector3f.UNIT_Z).setY(0).normalizeLocal());
        } else {
            entity.setLocalTranslation(location);
            entity.setLocalRotation(rotation);
        }
    }

    public void removeEntity(int id, int reason) {
        logger.log(Level.INFO, "Removing entity with id {0}", id);

        Spatial spatial = entities.remove(id);
        if (spatial == null) {
            return;
        }

        ServerFogManager serverFogManager =
                app.getStateManager().getState(ServerFogManager.class);

        syncManager.removeEntity(id);

        if (serverFogManager != null) {
            serverFogManager
                    .removeEntity(spatial, new CmdRemoveEntity(id, reason));
        }

        if (isClient()) {
            if (reason != -1) {
                CEntityEvent eventControl =
                        spatial.getControl(CEntityEvent.class);
                if (eventControl != null) {
                    eventControl.getOnRemoval().exec(this, reason);
                }

                if (reason == RemovalReasons.DISAPPEARED) {
                    logger.log(Level.INFO, "Entity {0} disappeared", id);
                    UserCommandManager userCommandManager = app.
                            getStateManager().getState(UserCommandManager.class);
                    if (id == userCommandManager.getCharacterId()) {
                        userCommandManager.nullifyCharacter();
                    }
                }
            }

            app.getStateManager().getState(ClientHudManager.class)
                    .entityDisappeared(spatial);

            // TODO: Consider doing this to all controls to generalize destruction
            CCharacterBuff buffControl =
                    spatial.getControl(CCharacterBuff.class);
            spatial.removeControl(buffControl);
        }


        spatial.removeFromParent();
        LightControl lightControl = spatial.getControl(LightControl.class);
        if (lightControl != null) {
            Light light = lightControl.getLight();
            if (light != null) {
                getWorldRoot().removeLight(light);
            }
        }
        space.removeAll(spatial);
        final GhostControl ghostControl = spatial.getControl(GhostControl.class);
        if (ghostControl != null) {
            space.remove(ghostControl);
        }
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
    }

    public SyncManager getSyncManager() {
        return syncManager;
    }

    public boolean isServer() {
        Sender sender = app.getStateManager().getState(Sender.class);
        return sender.isServer();
    }

    public boolean isClient() {
        return isClient;
    }

    public void clear() {
        for (PlayerData playerData : PlayerData.getPlayers()) {
            playerData.setData(PlayerDataStrings.ENTITY_ID, -1l);
        }
        if (worldRoot != null) {
            space.removeAll(worldRoot);
            rootNode.detachChild(worldRoot);
        }
        entities.clear();
        syncManager.clear();

        idCounter = 0;

        worldRoot = null;

        if (isClient()) {
            ClientFogManager clientFogManager =
                    app.getStateManager().getState(ClientFogManager.class);
            app.getViewPort().removeProcessor(clientFogManager);
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();
        if (worldRoot != null) {
            rootNode.detachChild(worldRoot);
        }
    }

    public Node getWorldRoot() {
        return worldRoot;
    }

    public Spatial getEntity(int id) {
        return entities.get(id);
    }

    public boolean validateLocation(Vector3f location) {
        return arena.validateLocation(location);
    }

    public ClientMain getClientMain() {
        return clientMain;
    }

    public PhysicsSpace getSpace() {
        return space;
    }
}