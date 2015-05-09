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
package arkhados.controls;

import arkhados.CharacterInteraction;
import arkhados.Globals;
import arkhados.PlayerData;
import arkhados.WorldManager;
import arkhados.actions.SplashAction;
import arkhados.messages.syncmessages.statedata.ProjectileSyncData;
import arkhados.messages.syncmessages.statedata.StateData;
import arkhados.util.PlayerDataStrings;
import arkhados.util.RemovalReasons;
import arkhados.util.UserDataStrings;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import java.util.concurrent.Callable;

public class CGrenade extends AbstractControl
        implements PhysicsTickListener, PhysicsCollisionListener, CSync {

    private SplashAction splashAction;
    private float age = 0f;
    private float detonationTime;
    private float launchSpeed;
    private Vector3f direction = null;
    private CInfluenceInterface ownerInterface;

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);

        if (spatial == null) {
            Globals.app.enqueue(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    Globals.space.removeCollisionListener(CGrenade.this);
                    Globals.space.removeTickListener(CGrenade.this);
                    return null;
                }
            });

        }
    }

    public void setDirection(Vector3f direction) {
        this.direction = direction;
    }

    @Override
    protected void controlUpdate(float tpf) {
        age += tpf;

        if (age >= detonationTime) {
            if (splashAction != null) {
                splashAction.update(tpf);
            }


            int entityId = spatial.getUserData(UserDataStrings.ENTITY_ID);
            WorldManager world =
                    Globals.app.getStateManager().getState(WorldManager.class);
            world.removeEntity(entityId, RemovalReasons.EXPIRED);
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public void setDetonationTime(float ageCap) {
        this.detonationTime = ageCap;
    }

    public void setLaunchSpeedBasedOnRange(float range) {
        RigidBodyControl body = spatial.getControl(RigidBodyControl.class);
        float gravity = -body.getGravity().y / body.getMass();

        launchSpeed = FastMath.sqrt(range * gravity);

    }

    public float getLaunchSpeed() {
        return launchSpeed;
    }

    @Override
    public void prePhysicsTick(PhysicsSpace space, float tpf) {
        RigidBodyControl body = spatial.getControl(RigidBodyControl.class);
        if (direction != null) {
            body.setLinearVelocity(direction);
            direction = null;
        }
    }

    @Override
    public void physicsTick(PhysicsSpace space, float tpf) {
    }

    @Override
    public StateData getSyncableData(StateData stateData) {
        int id = spatial.getUserData(UserDataStrings.ENTITY_ID);
        return new ProjectileSyncData(id,
                spatial.getControl(RigidBodyControl.class));
    }

    @Override
    public void collision(PhysicsCollisionEvent event) {
        boolean isA = spatial == event.getNodeA();
        boolean isB = spatial == event.getNodeB();
        if (!isA && !isB) {
            return;
        }

        Spatial other = isA ? event.getNodeB() : event.getNodeA();

        CInfluenceInterface otherInterface =
                other.getControl(CInfluenceInterface.class);

        if (otherInterface == null) {
            return;
        }

        damage(other, otherInterface);
    }

    private void damage(Spatial other, CInfluenceInterface otherInterface) {
        int grenadeTeamId = spatial.getUserData(UserDataStrings.TEAM_ID);
        int targetPlayerId = other.getUserData(UserDataStrings.PLAYER_ID);
        int targetTeamId =
                PlayerData.getIntData(targetPlayerId, PlayerDataStrings.TEAM_ID);

        if (targetTeamId == grenadeTeamId) {
            return;
        }

        RigidBodyControl body = spatial.getControl(RigidBodyControl.class);

        final float damage = spatial.getUserData(UserDataStrings.DAMAGE);
        int removalReason = RemovalReasons.COLLISION;
        if (otherInterface.isImmuneToProjectiles()) {
            otherInterface.reducePurifyingFlame(damage);
            removalReason = RemovalReasons.ABSORBED;
        } else {

            CSpellBuff buffControl = spatial.getControl(CSpellBuff.class);

            boolean canBreakCC = damage > 0f ? true : false;

            CharacterInteraction.harm(ownerInterface, otherInterface,
                    damage, buffControl.getBuffs(), canBreakCC);

            Float impulseFactor = spatial
                    .getUserData(UserDataStrings.IMPULSE_FACTOR);

            Vector3f impulse = other.getLocalTranslation()
                    .subtract(body.getPhysicsLocation().setY(0))
                    .normalizeLocal().multLocal(impulseFactor);

            other.getControl(CCharacterPhysics.class)
                    .applyImpulse(impulse);

            if (splashAction != null) {
                splashAction.excludeSpatial(other);
                splashAction.update(0);
            }
        }

        int entityId = spatial.getUserData(UserDataStrings.ENTITY_ID);
        Globals.app.getStateManager().getState(WorldManager.class)
                .removeEntity(entityId, removalReason);

    }

    public CInfluenceInterface getOwnerInterface() {
        return ownerInterface;
    }

    public void setOwnerInterface(CInfluenceInterface ownerInterface) {
        this.ownerInterface = ownerInterface;
    }
            
    public SplashAction getSplashAction() {
        return splashAction;
    }
    
    public void setSplashAction(SplashAction action) {
        this.splashAction = action;
    }

}
