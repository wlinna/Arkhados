/*
 * Copyright (c) 2009-2011 William Linna
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'Arkhados' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package arkhados.controls;

import arkhados.ServerFogManager;
import arkhados.util.UserDataStrings;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.collision.CollisionResults;
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author william
 */
public class PlayerEntityAwareness {

    private static final float BLIND_RANGE_SQUARED = FastMath.sqr(40f);
    private static final Logger logger =
            Logger.getLogger(PlayerEntityAwareness.class.getName());
    private Spatial ownSpatial;
    private Map<Spatial, Boolean> entityFlags = new HashMap<>(6);
    private final int playerId;
    private Node walls;
    private Ray ray = new Ray();
    private float rangeSquared = FastMath.sqr(170f);
    private Vector3f _reUsableVec = new Vector3f();
    private ServerFogManager fogManager;

    public PlayerEntityAwareness(int playerId, Node walls,
            ServerFogManager fogManager) {
        this.playerId = playerId;
        this.walls = walls;
        this.fogManager = fogManager;
    }

    public void update(float tpf) {
        if (ownSpatial == null) {
            return;
        }
        for (Map.Entry<Spatial, Boolean> entry : entityFlags.entrySet()) {
            Spatial character = entry.getKey();

            boolean previousFlag = entry.getValue();
            boolean newFlag;

            newFlag = testVisibility(character);

            entry.setValue(newFlag);

            if (newFlag != previousFlag) {
                fogManager.visibilityChanged(this, character, newFlag);
            }
        }
    }

    public boolean testVisibility(Spatial other) {
        if (other == null) {
            return false;
        }
        if (other.getUserData(UserDataStrings.INVISIBLE_TO_ALL)) {
            return false;
        }
        if (getOwnSpatial() == null) {
            logger.log(Level.WARNING,
                    "Player {0} tried testVisibility without owning spatial",
                    playerId);
            return false;
        }
        if (other == getOwnSpatial()) {
            return true;
        }

        Vector3f otherLocation;

        RigidBodyControl rigidBody = other.getControl(RigidBodyControl.class);
        if (rigidBody != null) {
            otherLocation = rigidBody.getPhysicsLocation();
        } else {
            otherLocation = other.getLocalTranslation();
        }

        float distanceSquared = otherLocation
                .distanceSquared(getOwnSpatial().getLocalTranslation());

        if (distanceSquared > rangeSquared) {
            return false;
        }

        CInfluenceInterface influenceInterface =
                getOwnSpatial().getControl(CInfluenceInterface.class);
        if (influenceInterface != null
                && influenceInterface.isBlind()
                && distanceSquared > BLIND_RANGE_SQUARED) {
            return false;
        }

        return wallTest(other, FastMath.sqrt(distanceSquared));
    }

    private boolean wallTest(Spatial other, float distance) {
        Vector3f direction = _reUsableVec;
        direction.set(other.getLocalTranslation());
        direction.subtractLocal(getOwnSpatial().getLocalTranslation())
                .normalizeLocal();

        CollisionResults collisionResults = new CollisionResults();

        ray.setOrigin(getOwnSpatial().getLocalTranslation());
        ray.setDirection(direction);
        ray.setLimit(distance);
        walls.collideWith(ray, collisionResults);

        if (collisionResults.size() == 0
                || collisionResults.getClosestCollision()
                .getDistance() > distance) {
            return true;
        }

        return false;
    }

    public ServerFogManager getFogManager() {
        return fogManager;
    }

    public boolean isAwareOf(Spatial other) {
        if (other == getOwnSpatial() && other.getUserData(UserDataStrings.INVISIBLE_TO_ALL) == false) {
            return true;
        }
        if (!entityFlags.containsKey(other)) {
            return true; // This needs explanation. It seems counter-intuitive
        }

        return entityFlags.get(other);
    }

    public boolean addEntity(Spatial character) {
        boolean sees = testVisibility(character);
        entityFlags.put(character, sees);
        return sees;
    }

    public boolean removeEntity(Spatial entity) {
        Boolean saw = entityFlags.remove(entity);
        if (saw == null) {
            return false;
        }
        return saw;
    }

    public Spatial getOwnSpatial() {
        return ownSpatial;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setOwnSpatial(Spatial ownNode) {
        this.ownSpatial = ownNode;
    }
}