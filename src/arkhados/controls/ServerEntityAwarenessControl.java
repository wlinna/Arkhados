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
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author william
 */
public class ServerEntityAwarenessControl extends AbstractControl {

    private Map<Spatial, Boolean> characterFlags = new HashMap<>(6);
    private Node walls;
    private Ray ray = new Ray();
    private float rangeSquared = FastMath.sqr(140);
    private Vector3f reUsableVec = new Vector3f();
    private ServerFogManager fogManager;

    public ServerEntityAwarenessControl(Node walls, ServerFogManager fogManager) {
        this.walls = walls;
        this.fogManager = fogManager;
    }        

    @Override
    protected void controlUpdate(float tpf) {
        for (Map.Entry<Spatial, Boolean> entry : characterFlags.entrySet()) {
            Spatial character = entry.getKey();

            boolean previousFlag = entry.getValue();
            boolean newFlag;

            if (character == getSpatial()) {
                continue;
            }

            float distanceSquared = character.getLocalTranslation()
                    .distanceSquared(getSpatial().getLocalTranslation());

            if (distanceSquared > rangeSquared) {
                entry.setValue(false);
                if (previousFlag == true) {
                    fogManager.visibilityChanged(getSpatial(), character, false);
                }
                continue;
            }

            newFlag = wallTest(spatial, FastMath.sqrt(distanceSquared));

            if (newFlag != previousFlag) {
                fogManager.visibilityChanged(spatial, character, newFlag);
            }

            entry.setValue(newFlag);
        }
    }

    private boolean wallTest(Spatial other, float distance) {
        Vector3f direction = reUsableVec;
        direction.set(other.getLocalTranslation());
        direction.subtractLocal(getSpatial().getLocalTranslation()).normalizeLocal();

        CollisionResults collisionResults = new CollisionResults();

        ray.setOrigin(getSpatial().getLocalTranslation());
        ray.setDirection(direction);
        ray.setLimit(distance);
        ray.collideWith(walls, collisionResults);

        if (collisionResults.size() == 0) {
            return true;
        }

        return false;
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public ServerFogManager getFogManager() {
        return fogManager;
    }
}