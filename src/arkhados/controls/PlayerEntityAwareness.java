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
import com.jme3.collision.CollisionResults;
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author william
 */
public class PlayerEntityAwareness {

    private Node ownNode;
    private Map<Spatial, Boolean> characterFlags = new HashMap<>(6);
    private final int playerId;
    private Node walls;
    private Ray ray = new Ray();
    private float rangeSquared = FastMath.sqr(140);
    private Vector3f reUsableVec = new Vector3f();
    private ServerFogManager fogManager;

    public PlayerEntityAwareness(int playerId, Node walls, ServerFogManager fogManager) {
        this.playerId = playerId;
        this.walls = walls;
        this.fogManager = fogManager;
    }

    public void update(float tpf) {
        for (Map.Entry<Spatial, Boolean> entry : characterFlags.entrySet()) {
            Spatial character = entry.getKey();

            boolean previousFlag = isAwareOf(entry.getKey());
            boolean newFlag;

            if (character == getOwnNode()) {
                entry.setValue(true);
                continue;
            }

            newFlag = testVisibility(character);
            entry.setValue(newFlag);

            if (newFlag != previousFlag) {
                fogManager.visibilityChanged(this, character, newFlag);
            }
        }
    }

    public boolean testVisibility(Spatial other) {
        if (other == getOwnNode()) {
            return true;
        }

        float distanceSquared = other.getLocalTranslation()
                .distanceSquared(getOwnNode().getLocalTranslation());

        if (distanceSquared > rangeSquared) {
            return false;
        }
        return true;
    }

    private boolean wallTest(Spatial other, float distance) {
        Vector3f direction = reUsableVec;
        direction.set(other.getLocalTranslation());
        direction.subtractLocal(getOwnNode().getLocalTranslation()).normalizeLocal();

        CollisionResults collisionResults = new CollisionResults();

        ray.setOrigin(getOwnNode().getLocalTranslation());
        ray.setDirection(direction);
        ray.setLimit(distance);
        walls.collideWith(ray, collisionResults);

        if (collisionResults.size() == 0) {
            return true;
        }

        return false;
    }

    public ServerFogManager getFogManager() {
        return fogManager;
    }

    public boolean isAwareOf(Spatial other) {
        if (other == getOwnNode() || !characterFlags.containsKey(other)) {
            return true;
        }

        return characterFlags.get(other);
    }

    public boolean addCharacter(Spatial character) {
        boolean sees = testVisibility(character);
        characterFlags.put(character, sees);
        return sees;
    }

    public Spatial getOwnNode() {
        return ownNode;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setOwnNode(Node ownNode) {
        this.ownNode = ownNode;
    }
}