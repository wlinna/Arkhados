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
import arkhados.spell.buffs.AbstractBuff;
import arkhados.spell.influences.Influence;
import arkhados.util.PlayerDataStrings;
import arkhados.util.UserDataStrings;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.control.GhostControl;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author william
 */
public class AreaEffectControl extends AbstractControl {

    private GhostControl ghostControl;
    private final List<Influence> influences = new ArrayList<Influence>();
    private final List<AbstractBuff> exitBuffs = new ArrayList<AbstractBuff>();
    private final List<AbstractBuff> enterBuffs = new ArrayList<AbstractBuff>();
    private final HashMap<InfluenceInterfaceControl, Boolean> enteredPlayers
            = new HashMap<InfluenceInterfaceControl, Boolean>();
    private InfluenceInterfaceControl ownerInterface = null;


    public AreaEffectControl() {
    }

    public AreaEffectControl(GhostControl ghostControl) {
        this.ghostControl = ghostControl;
    }

    @Override
    protected void controlUpdate(float tpf) {
        final Long myPlayerId = super.spatial.getUserData(UserDataStrings.PLAYER_ID);
        final long myTeamId = PlayerData.getLongData(myPlayerId, PlayerDataStrings.TEAM_ID);
        List<PhysicsCollisionObject> collisionObjects = this.ghostControl.getOverlappingObjects();

        for (PhysicsCollisionObject collisionObject : collisionObjects) {
            if (!(collisionObject.getUserObject() instanceof Spatial)) {
                continue;
            }
            final Spatial other = (Spatial) collisionObject.getUserObject();
            final InfluenceInterfaceControl influenceInterface = other.getControl(InfluenceInterfaceControl.class);
            if (influenceInterface == null) {
                continue;
            }

            final Long othersPlayerId = other.getUserData(UserDataStrings.PLAYER_ID);
            final Long othersTeamId = PlayerData.getLongData(othersPlayerId, PlayerDataStrings.TEAM_ID);
            final boolean sameTeam = myTeamId == othersTeamId;
            for (Influence influence : this.influences) {
                if (sameTeam && influence.isFriendly()) {
                    influence.affect(influenceInterface, tpf);
                } else if (!sameTeam && !influence.isFriendly()) {
                    influence.affect(influenceInterface, tpf) ;
                }
            }

            if (!this.enteredPlayers.containsKey(influenceInterface)) {
                this.enteredPlayers.put(influenceInterface, false);
                CharacterInteraction.harm(this.ownerInterface, influenceInterface, 0f, this.enterBuffs, false);
            }
        }

        // TODO: Add way to inflict exitBuffs
    }

    public void addInfluence(final Influence influence) {
        this.influences.add(influence);
    }

    public void addEnterBuff(final AbstractBuff buff) {
        this.enterBuffs.add(buff);
    }

    public void addExitBuff(final AbstractBuff buff) {
        this.exitBuffs.add(buff);
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public Control cloneForSpatial(Spatial spatial) {
        AreaEffectControl control = new AreaEffectControl();
//        control.ghostControl = this.ghostControl.cloneForSpatial(spatial);
        return control;
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule in = im.getCapsule(this);
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule out = ex.getCapsule(this);
    }

    public void setOwnerInterface(InfluenceInterfaceControl ownerInterface) {
        this.ownerInterface = ownerInterface;
    }
}