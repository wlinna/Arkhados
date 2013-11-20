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
package arkhados.spell.buffs;

import arkhados.SyncManager;
import arkhados.controls.InfluenceInterfaceControl;
import arkhados.messages.syncmessages.BuffMessage;
import arkhados.util.UserDataStrings;
import com.jme3.scene.Spatial;

/**
 * Base class for all buffs, negative or positive.
 *
 * @author william
 */
public abstract class AbstractBuff {
    private static long currentBuffId = 0;

    // TODO: Consider removing this. If there's going to be way to
    private static SyncManager syncManager;
    protected String name = null;
    private long buffGroupId;
    protected float duration;
    protected InfluenceInterfaceControl targetInterface = null;
    private InfluenceInterfaceControl ownerInterface = null;
    protected boolean friendly = false;

    private long buffId = ++currentBuffId;
    /**
     * @param buffGroupId identifies group of buffs so that they can be removed
     * with single dispel. Not used currently
     */
    public AbstractBuff(long buffGroupId, float duration) {
        this.buffGroupId = buffGroupId;
        this.duration = duration;
    }

    public void attachToCharacter(InfluenceInterfaceControl targetInterface) {
        this.targetInterface = targetInterface;
        targetInterface.addOtherBuff(this);
        if (this.name != null) {
            final Long entityId = this.targetInterface.getSpatial().getUserData(UserDataStrings.ENTITY_ID);
            getSyncManager().broadcast(new BuffMessage(entityId, this.name, this.buffId, true));
        }
    }

    /**
     *
     * @return Id of buff group that buff belongs to.
     */
    public long getBuffGroupId() {
        return this.buffGroupId;
    }

    public void update(float time) {
        this.duration -= time;
    }

    /**
     * Method for checking from buff's internal state whether it should be
     * removed or not
     *
     * @return true if buff should continue. false, if it should be removed
     */
    public boolean shouldContinue() {
        if (this.duration <= 0f) {
            return false;
        }
        return true;
    }

    public void destroy() {
        if (this.name != null) {
            final Long entityId = this.targetInterface.getSpatial().getUserData(UserDataStrings.ENTITY_ID);
            getSyncManager().broadcast(new BuffMessage(entityId, null, this.buffId, false));
        }
    }

    public InfluenceInterfaceControl getOwnerInterface() {
        return ownerInterface;
    }

    public void setOwnerInterface(final InfluenceInterfaceControl ownerInterface) {
        this.ownerInterface = ownerInterface;
    }

    public boolean isFriendly() {
        return this.friendly;
    }

    private static SyncManager getSyncManager() {
        return syncManager;
    }

    public static void setSyncManager(SyncManager aSyncManager) {
        syncManager = aSyncManager;
    }
}
