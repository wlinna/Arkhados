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
import arkhados.PlayerData;
import arkhados.actions.ADelay;
import arkhados.spell.buffs.AbstractBuffBuilder;
import arkhados.spell.influences.Influence;
import arkhados.util.UserData;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.PhysicsControl;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CAreaEffect extends AbstractControl
        implements PhysicsTickListener, PhysicsControl {

    private GhostControl ghostControl;
    private PhysicsSpace space;
    private final List<Influence> influences = new ArrayList<>();
    private final List<AbstractBuffBuilder> enterBuffs = new ArrayList<>();
    private final HashMap<CInfluenceInterface, Boolean> enteredPlayers
            = new HashMap<>();
    private CInfluenceInterface ownerInterface = null;

    public CAreaEffect() {
    }

    public CAreaEffect(GhostControl ghostControl) {
        this.ghostControl = ghostControl;
    }

    @Override
    protected void controlUpdate(float tpf) {
    }

    public void addInfluence(Influence influence) {
        if (influence == null) {
            throw new IllegalArgumentException(
                    "Nulls not allowed in containers");
        }

        influence.setOwner(ownerInterface);
        influences.add(influence);
    }

    public void addEnterBuff(AbstractBuffBuilder builder) {
        if (builder == null) {
            throw new IllegalArgumentException(
                    "Nulls not allowed in containers");
        }

        enterBuffs.add(builder);
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public CInfluenceInterface getOwnerInterface() {
        return ownerInterface;
    }

    public void setOwnerInterface(CInfluenceInterface ownerInterface) {
        if (ownerInterface == null) {
            throw new IllegalArgumentException("Null cannot be ownerInterface");
        }

        this.ownerInterface = ownerInterface;
    }

    @Override
    public void prePhysicsTick(PhysicsSpace space, float tpf) {
    }

    @Override
    public void physicsTick(PhysicsSpace space, float tpf) {
        // HACK
        CActionQueue actionQueue = getSpatial().getControl(CActionQueue.class);
        if (actionQueue != null && actionQueue.getCurrent() instanceof ADelay) {
            return;
        }

        int myTeamId = spatial.getUserData(UserData.TEAM_ID);
        List<PhysicsCollisionObject> collisionObjects
                = ghostControl.getOverlappingObjects();

        for (PhysicsCollisionObject collisionObject : collisionObjects) {
            if (!(collisionObject.getUserObject() instanceof Spatial)) {
                continue;
            }
            Spatial other = (Spatial) collisionObject.getUserObject();
            CInfluenceInterface targetInterface
                    = other.getControl(CInfluenceInterface.class);

            if (targetInterface == null) {
                continue;
            }

            int othersPlayerId = other.getUserData(UserData.PLAYER_ID);
            int othersTeamId = PlayerData.getIntData(othersPlayerId,
                    PlayerData.TEAM_ID);
            boolean sameTeam = myTeamId == othersTeamId;
            for (Influence influence : influences) {
                if (sameTeam && influence.isFriendly()) {
                    targetInterface.addInfluence(influence);
                } else if (!sameTeam && !influence.isFriendly()) {
                    targetInterface.addInfluence(influence);
                }
            }

            if (!enteredPlayers.containsKey(targetInterface)
                    && !enterBuffs.isEmpty()) {
                enteredPlayers.put(targetInterface, false);
                if (sameTeam) {
                    CharacterInteraction.help(ownerInterface, targetInterface,
                            0f, enterBuffs);
                } else {
                    CharacterInteraction.harm(ownerInterface, targetInterface,
                            0f, enterBuffs, false);
                }
            }
        }
        // TODO: Add way to inflict exitBuffs
    }

    @Override
    public void setPhysicsSpace(PhysicsSpace space) {
        if (this.space != null && space == null) {
            this.space.removeTickListener(this);
        } else if (this.space == null && space != null) {
            space.addTickListener(this);
        }

        this.space = space;
    }

    @Override
    public PhysicsSpace getPhysicsSpace() {
        return space;
    }
}
