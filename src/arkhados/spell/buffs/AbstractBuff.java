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

import arkhados.ServerFog;
import arkhados.controls.CEntityVariable;
import arkhados.controls.CInfluenceInterface;
import arkhados.messages.sync.CmdBuff;
import arkhados.messages.sync.CmdBuffStacks;
import arkhados.net.Sender;
import arkhados.util.UserData;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all buffs, negative or positive.
 */
public abstract class AbstractBuff {
    public static interface EndEventListener {
        public void buffEnded();
        public static class Simple implements EndEventListener {
            private final Runnable runnable;

            public Simple(Runnable runnable) {
                this.runnable = runnable;
            }

            @Override
            public void buffEnded() {
                runnable.run();
            }
        }
    }

    private static int currentBuffId = 0;
    // TODO: Consider removing this. If there's going to be way to
    private static Sender sender;
    protected String name = null;
    private int typeId = -1;
    private int stacks = 1;
    protected float duration;
    protected CInfluenceInterface targetInterface = null;
    private CInfluenceInterface ownerInterface = null;
    protected boolean friendly = false;
    private final int buffId = ++currentBuffId;
    
    private final List<EndEventListener> buffEndHandlers 
            = new ArrayList<>();

    protected AbstractBuff(float duration, int stacks) {
        this(duration);
        this.stacks = stacks;
    }
    
    protected AbstractBuff(float duration) {
        this.duration = duration;
    }

    public void attachToCharacter(CInfluenceInterface targetInterface) {
        this.targetInterface = targetInterface;
        targetInterface.addBuff(this);

        CmdBuff buffCommand = generateBuffCommand(true);
        if (buffCommand != null) {
            Spatial spatial = targetInterface.getSpatial();
            spatial.getControl(CEntityVariable.class).getAwareness()
                    .getFog().addCommand(spatial, buffCommand);
        }
    }

    public CmdBuff generateBuffCommand(boolean added) {
        int entityId = targetInterface.getSpatial()
                .getUserData(UserData.ENTITY_ID);
        return typeId == -1 ? null : new CmdBuff(
                entityId, typeId, buffId, duration, stacks, added);
    }

    public void update(float time) {
        duration -= time;
    }
    
    public void dispel() {
        duration = 0f;
    }

    /**
     * Method for checking from buff's internal state whether it should be
     * removed or not
     *
     * @return true if buff should continue. false, if it should be removed
     */
    public boolean shouldContinue() {
        return duration > 0f;
    }

    public int getStacks() {
        return stacks;
    }

    public void changeStackAmount(int amount) {
        stacks = amount;
        int entityId = targetInterface.getSpatial()
                .getUserData(UserData.ENTITY_ID);
        CmdBuffStacks cmdStacks = new CmdBuffStacks(entityId, buffId, stacks);
        Spatial spatial = targetInterface.getSpatial();
        ServerFog fog = spatial.getControl(CEntityVariable.class).getAwareness()
                .getFog();
        fog.addCommand(spatial, cmdStacks);
    }

    public void destroy() {
        CmdBuff buffCommand = generateBuffCommand(false);
        if (buffCommand != null) {
            Spatial spatial = targetInterface.getSpatial();
            ServerFog fog = spatial
                    .getControl(CEntityVariable.class).getAwareness().getFog();
            fog.addCommand(spatial, buffCommand);
        }
        
        for (EndEventListener endHandler : buffEndHandlers) {
            endHandler.buffEnded();
        }
    }

    public CInfluenceInterface getOwnerInterface() {
        return ownerInterface;
    }

    public void setOwnerInterface(CInfluenceInterface ownerInterface) {
        this.ownerInterface = ownerInterface;
    }

    public boolean isFriendly() {
        return friendly;
    }

    public void setName(String name) {
        this.name = name;
    }

    protected static Sender getSender() {
        return sender;
    }

    public static void setSender(Sender aSender) {
        sender = aSender;
    }

    protected int getBuffId() {
        return buffId;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public boolean isDamageSensitive() {
        return false;
    }
    
    public void addEndListener(EndEventListener listener) {
        buffEndHandlers.add(listener);
    }
}
