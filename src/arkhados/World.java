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

import arkhados.ui.hud.ClientHud;
import arkhados.arena.AbstractArena;
import arkhados.arena.BasicSquareArena;
import arkhados.arena.PillarArena;
import arkhados.controls.CCharacterBuff;
import arkhados.controls.CCharacterHud;
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
import arkhados.messages.sync.CmdAddEntity;
import arkhados.messages.sync.CmdRemoveEntity;
import arkhados.net.Sender;
import arkhados.spell.Spell;
import arkhados.spell.buffs.info.BuffInfo;
import arkhados.util.BuildParameters;
import arkhados.util.EntityFactory;
import arkhados.util.PhysicsWorkaround;
import arkhados.util.RemovalReasons;
import arkhados.util.Selector;
import arkhados.util.UserData;
import com.jme3.bullet.collision.shapes.PlaneCollisionShape;
import com.jme3.bullet.debug.BulletDebugAppState;
import com.jme3.light.Light;
import com.jme3.math.Plane;
import com.jme3.scene.control.LightControl;
import com.jme3.util.IntMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class World extends AbstractAppState {

    private final static Logger logger =
            Logger.getLogger(World.class.getName());

    static {
        logger.setLevel(Level.WARNING);
    }
    // TODO: Read locations from terrain

    private Node worldRoot;
    private Node fakeWorldRoot;
    private AbstractArena arena = new PillarArena();
    private final IntMap<Spatial> entities = new IntMap<>();
    private Sync sync;
    private short idCounter = 0;
    private boolean isClient = false;
    private EffectHandler effectHandler = null;

    public World() {
    }

    public World(EffectHandler effectHandler) {
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
    private float worldTime = 0f;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        CTimedExistence.setWorld(this);
        Selector.setWorld(this);
        super.initialize(stateManager, app);
        this.app = (SimpleApplication) app;
        rootNode = this.app.getRootNode();
        assetManager = app.getAssetManager();
        space = app.getStateManager().getState(BulletAppState.class)
                .getPhysicsSpace();

        cam = app.getCamera();

        sync = app.getStateManager().getState(Sync.class);

        Sender sender = stateManager.getState(Sender.class);

        if (sender.isServer()) {
            serverCollisionListener = new ServerWorldCollisionListener(this);
            space.addCollisionListener(serverCollisionListener);
            entityFactory = new EntityFactory();
        } else if (isClient()) {
            clientMain = (ClientMain) app;
            entityFactory = new EntityFactory(effectHandler);

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

        Spell.initSpells(entityFactory, this);
        BuffInfo.initBuffs();
        BuffEffect.setAssetManager(assetManager);
    }

    public void loadLevel() {
        worldRoot = (Node) assetManager.loadModel("Scenes/PillarArena.j3o");
//        worldRoot = (Node) assetManager.loadModel(
//                "Scenes/LavaArenaWithFogWalls.j3o");
        fakeWorldRoot = new Node("fake-world-root");

        RigidBodyControl physics = new RigidBodyControl(
                new PlaneCollisionShape(new Plane(Vector3f.UNIT_Y, 0)), 0);
        physics.setFriction(1f);
        physics.setRestitution(0f);
        physics.setCollideWithGroups(CollisionGroups.NONE);

        worldRoot.getChild("Ground").addControl(physics);

//        Spatial groundGeom = worldRoot.getChild("GroundGeom");
//        LodControl lod = groundGeom.getControl(LodControl.class);
//
//        if (lod == null) {
//            lod = new LodControl();
//            groundGeom.addControl(lod);
//            lod.setTrisPerPixel(0);
//        }

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
        rootNode.attachChild(fakeWorldRoot);

        cam.setLocation(new Vector3f(0, 160, 20));
        cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);

        if (isServer()) {
            app.getStateManager().getState(ServerFog.class)
                    .setWalls((Node) worldRoot.getChild("Walls"));
        } else {
            ClientFog fog = app.getStateManager().getState(ClientFog.class);
            app.getViewPort().addProcessor(fog);
            fog.createFog(worldRoot);
        }

        UserCommandManager userCommandManager =
                app.getStateManager().getState(UserCommandManager.class);
        if (userCommandManager != null) {
            userCommandManager.createCameraControl();
        }
    }

    private int giveNewId() {
        while (entities.containsKey((int) (++idCounter))) {
        }

        return idCounter;
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
        addEntity(giveNewId(), nodeBuilderId, location, rotation, playerId, 0f);
        return idCounter;
    }

    public void addEntity(int id, int nodeBuilderId, Vector3f location,
            Quaternion rotation, int playerId, float age) {
        Sender sender = app.getStateManager().getState(Sender.class);
        Spatial entity = entityFactory.build(nodeBuilderId,
                new BuildParameters(age, location));

        entity.setUserData(UserData.NODE_BUILDER_ID, nodeBuilderId);
        setEntityTranslation(entity, location, rotation);
        entity.setUserData(UserData.PLAYER_ID, playerId);
        entity.setUserData(UserData.ENTITY_ID, id);
        entity.setUserData(UserData.INVISIBLE_TO_ALL, false);
        entity.setUserData(UserData.INVISIBLE_TO_ENEMY, false);
        int teamId = PlayerData.getIntData(playerId, PlayerData.TEAM_ID);
        entity.setUserData(UserData.TEAM_ID, teamId);
        if (isServer()) {
            entity.setUserData(UserData.BIRTHTIME, worldTime);
        } else {
            entity.setUserData(UserData.BIRTHTIME, age);
        }

        entities.put(id, entity);
        sync.addObject(id, entity);

        PhysicsWorkaround.addAll(space, entity);

        worldRoot.attachChild(entity);
        CEntityVariable cVariable = new CEntityVariable(this, sender);
        entity.addControl(cVariable);

        boolean isCharacter =
                entity.getControl(CCharacterPhysics.class) != null;

        if (isCharacter) {
            logger.log(Level.INFO, "Adding entity {0} for player {1}",
                    new Object[]{id, playerId});
            entity.setUserData(UserData.FOLLOW_ME, true);

            CUserInput cUserInput = new CUserInput();
            if (isServer()) {
                ServerInputState inputState = ServerInput.get()
                        .getInputState(playerId);
                inputState.currentActiveSpatial = entity;
                cUserInput.setInputState(inputState);
            } else {
                ClientHud hud = app.getStateManager().getState(ClientHud.class);
                hud.addCharacter(entity);
            }
            entity.addControl(cUserInput);
        }

        boolean followMe = entity.getUserDataKeys()
                .contains(UserData.FOLLOW_ME);

        ServerFog serverFog = app.getStateManager().getState(ServerFog.class);
        if (serverFog != null) {
            if (isCharacter) {
                serverFog.registerCharacterForPlayer(playerId, entity);
            }

            serverFog.createNewEntity(entity, new CmdAddEntity(id,
                    nodeBuilderId, location, rotation, playerId));
        }

        if (isClient()) {
            UserCommandManager userCommandManager = app.getStateManager()
                    .getState(UserCommandManager.class);
            boolean ownedByMe =
                    userCommandManager.trySetPlayersCharacter(entity);

            if (ownedByMe) {
                app.getStateManager().getState(ClientFog.class)
                        .setPlayerNode(entity);
                logger.log(Level.INFO,
                        "Setting player''s node. Id {0}, playerId {1}",
                        new Object[]{id, playerId});
            } else if (playerId == userCommandManager.getPlayerId()
                    && followMe) {
                app.getStateManager().getState(ClientFog.class)
                        .setPlayerNode(entity);
                userCommandManager.followSpatial(entity);
            }
        }
    }

    public void temporarilyRemoveEntity(int id) {
        logger.log(Level.FINE, "Temporarily removing entity with id {0}", id);
        Spatial spatial = getEntity(id);
        spatial.setUserData(UserData.INVISIBLE_TO_ALL, true);
        spatial.removeFromParent();
        sync.removeEntity(id);

        CCharacterPhysics physics = spatial.getControl(CCharacterPhysics.class);
        if (physics != null) {
            physics.setEnabled(false);
        }
    }

    public void restoreTemporarilyRemovedEntity(int id, Vector3f location,
            Quaternion rotation) {
        logger.log(Level.FINE,
                "Restoring temporarily removed entity. Id {0}", id);
        Spatial spatial = getEntity(id);
        spatial.setUserData(UserData.INVISIBLE_TO_ALL, false);
        worldRoot.attachChild(spatial);
        sync.addObject(id, spatial);

        CCharacterPhysics physics = spatial.getControl(CCharacterPhysics.class);
        if (physics != null) {
            physics.setEnabled(true);
        }

        CSyncInterpolation cInterpolation =
                spatial.getControl(CSyncInterpolation.class);
        if (cInterpolation != null) {
            cInterpolation.ignoreNext();
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

        ServerFog serverFog = app.getStateManager().getState(ServerFog.class);

        sync.removeEntity(id);

        if (serverFog != null) {
            serverFog.removeEntity(spatial, new CmdRemoveEntity(id, reason));
        }

        if (isClient()) {
            if (reason != -1) {
                CEntityEvent cEvent = spatial.getControl(CEntityEvent.class);
                if (cEvent != null
                        && reason != RemovalReasons.DISAPPEARED) {
                    cEvent.getOnRemoval().exec(this, reason);
                }

                if (reason == RemovalReasons.DISAPPEARED) {
                    logger.log(Level.INFO, "Entity {0} disappeared", id);
                    UserCommandManager userCommandManager =
                            app.getStateManager()
                            .getState(UserCommandManager.class);
                    if (id == userCommandManager.getCharacterId()) {
                        userCommandManager.nullifyCharacter();
                    }
                }
            }

            app.getStateManager().getState(ClientHud.class)
                    .entityDisappeared(spatial);

//             TODO: Consider doing this to all controls to generalize destruction
            CCharacterBuff cBuff = spatial.getControl(CCharacterBuff.class);
            spatial.removeControl(cBuff);

            CCharacterHud cHud = spatial.getControl(CCharacterHud.class);
            spatial.removeControl(cHud);
        }

        spatial.removeFromParent();
        LightControl cLight = spatial.getControl(LightControl.class);
        if (cLight != null) {
            Light light = cLight.getLight();
            if (light != null) {
                getWorldRoot().removeLight(light);
            }
        }

        PhysicsWorkaround.removeAll(space, spatial);
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
        worldTime += tpf;
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
            playerData.setData(PlayerData.ENTITY_ID, -1l);
        }
        if (worldRoot != null) {
            space.removeAll(worldRoot);
            rootNode.detachChild(worldRoot);
        }
        entities.clear();
        sync.clear();

        idCounter = 0;

        worldRoot = null;

        if (isClient()) {
            ClientFog fog = app.getStateManager().getState(ClientFog.class);
            app.getViewPort().removeProcessor(fog);
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

    public float getWorldTime() {
        return worldTime;
    }

    public Node getFakeWorldRoot() {
        return fakeWorldRoot;
    }

    public AbstractArena getArena() {
        return arena;
    }
}