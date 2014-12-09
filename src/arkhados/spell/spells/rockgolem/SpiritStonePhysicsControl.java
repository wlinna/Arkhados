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
package arkhados.spell.spells.rockgolem;

import arkhados.WorldManager;
import arkhados.controls.SyncControl;
import arkhados.messages.syncmessages.statedata.GenericSyncData;
import arkhados.messages.syncmessages.statedata.StateData;
import arkhados.util.UserDataStrings;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 *
 * @author william
 */
public class SpiritStonePhysicsControl extends RigidBodyControl implements SyncControl {

    private Vector3f tempVector = new Vector3f();
    private Vector3f punchDirection = null;
    private SpiritStoneCollisionListener collisionListener;    
    private WorldManager worldManager;

    public SpiritStonePhysicsControl(CollisionShape shape, float mass, WorldManager worldManager) {
        super(shape, mass);
        setKinematic(true);
        this.worldManager = worldManager;
    }

    @Override
    public void setPhysicsSpace(PhysicsSpace space) {
        super.setPhysicsSpace(space);
        if (worldManager.isClient()) {
            return;
        }
        
        if (space != null) {  
            setGravity(Vector3f.ZERO);
            setDamping(0.1f, 0.9f);
            collisionListener = new SpiritStoneCollisionListener((Node)spatial, worldManager);
            space.addCollisionListener(collisionListener);
        } else if (space == null && this.space != null) {
            this.space.removeCollisionListener(collisionListener);
        }
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);

        if (punchDirection != null) {
            tempVector.set(punchDirection).multLocal(tpf);
            spatial.getLocalTranslation().addLocal(tempVector);
        }
    }

    public void punch(Vector3f direction) {
        punchDirection = direction.clone();
    }

    @Override
    public StateData getSyncableData(StateData stateData) {
        return new GenericSyncData((int) spatial.getUserData(UserDataStrings.ENTITY_ID), spatial);
    }

    public Vector3f getLocation() {
        return spatial.getLocalTranslation();
    }

    public boolean isPunched() {
        return punchDirection != null;
    }
}
