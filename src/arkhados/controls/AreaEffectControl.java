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
import arkhados.actions.DelayAction;
import arkhados.spell.buffs.AbstractBuff;
import arkhados.spell.influences.Influence;
import arkhados.util.PlayerDataStrings;
import arkhados.util.UserDataStrings;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.control.GhostControl;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author william
 */
public class AreaEffectControl extends AbstractControl {

    private GhostControl ghostControl;
    private final List<Influence> influences = new ArrayList<>();
    private final List<AbstractBuff> exitBuffs = new ArrayList<>();
    private final List<AbstractBuff> enterBuffs = new ArrayList<>();
    private final HashMap<InfluenceInterfaceControl, Boolean> enteredPlayers = new HashMap<>();
    private InfluenceInterfaceControl ownerInterface = null;

    public AreaEffectControl() {
    }

    public AreaEffectControl(GhostControl ghostControl) {
        this.ghostControl = ghostControl;
    }

    @Override
    protected void controlUpdate(float tpf) {
        // HACK
        ActionQueueControl actionQueue = getSpatial().getControl(ActionQueueControl.class);
        if (actionQueue != null &&  actionQueue.getCurrent() instanceof DelayAction) {
            return;
        }

        final int myTeamId = spatial.getUserData(UserDataStrings.TEAM_ID);
        List<PhysicsCollisionObject> collisionObjects = this.ghostControl.getOverlappingObjects();

        for (PhysicsCollisionObject collisionObject : collisionObjects) {
            if (!(collisionObject.getUserObject() instanceof Spatial)) {
                continue;
            }
            final Spatial other = (Spatial) collisionObject.getUserObject();
            final InfluenceInterfaceControl targetInterface = other.getControl(InfluenceInterfaceControl.class);
            if (targetInterface == null) {
                continue;
            }
            final Integer othersPlayerId = other.getUserData(UserDataStrings.PLAYER_ID);
            final Integer othersTeamId = PlayerData.getIntData(othersPlayerId, PlayerDataStrings.TEAM_ID);
            final boolean sameTeam = myTeamId == othersTeamId;
            for (Influence influence : influences) {
                if (sameTeam && influence.isFriendly()) {
                    influence.affect(targetInterface, tpf);
                } else if (!sameTeam && !influence.isFriendly()) {
                    influence.affect(targetInterface, tpf);
                }
            }

            if (!enteredPlayers.containsKey(targetInterface)) {
                enteredPlayers.put(targetInterface, false);
                if (!sameTeam) {
                    CharacterInteraction.harm(ownerInterface, targetInterface, 0f, enterBuffs, false);
                }
            }
        }

        // TODO: Add way to inflict exitBuffs
    }

    public void addInfluence(Influence influence) {
        if (influence == null) {
            throw new IllegalArgumentException("Nulls not allowed in containers");
        }
        influences.add(influence);
    }

    public void addEnterBuff(AbstractBuff buff) {
        if (buff == null) {
            throw new IllegalArgumentException("Nulls not allowed in containers");
        }
        enterBuffs.add(buff);
    }

    public void addExitBuff(AbstractBuff buff) {
        if (buff == null) {
            throw new IllegalArgumentException("Nulls not allowed in containers");
        }
        exitBuffs.add(buff);
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public void setOwnerInterface(InfluenceInterfaceControl ownerInterface) {
        if (ownerInterface == null) {
            throw new IllegalArgumentException("Null cannot be ownerInterface");
        }
        this.ownerInterface = ownerInterface;
    }
}