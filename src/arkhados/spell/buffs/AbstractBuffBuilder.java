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

import arkhados.controls.CInfluenceInterface;
import arkhados.util.Builder;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBuffBuilder implements Builder<AbstractBuff> {
    public static interface EndListenerBuilder {
        public AbstractBuff.EndEventListener build(AbstractBuff buff);
        
        public static class Predefined implements EndListenerBuilder {
            private final AbstractBuff.EndEventListener listener;

            public Predefined(AbstractBuff.EndEventListener listener) {
                this.listener = listener;
            }

            @Override
            public AbstractBuff.EndEventListener build(AbstractBuff buff) {
                return listener;
            }
        }
    }

    public float duration;
    private int typeId = -1;
    private CInfluenceInterface ownerInterface;
    private String name;
    private final List<EndListenerBuilder> endListeners 
            = new ArrayList<>();

    public AbstractBuffBuilder(float duration) {
        this.duration = duration;
    }

    public AbstractBuffBuilder setDuration(float duration) {
        this.duration = duration;
        return this;
    }
    
    protected <T extends AbstractBuff> T set(T buff) {
        buff.setOwnerInterface(ownerInterface);
        buff.setTypeId(typeId);
        buff.name =  name;

        for (EndListenerBuilder endListener : endListeners) {
            buff.addEndListener(endListener.build(buff));
        }
        
        return buff;
    }
    
    public void addEndListenerBuilder(EndListenerBuilder listenerBuilder) {
        endListeners.add(listenerBuilder);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTypeId() {
        return typeId;
    }

    public final void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public CInfluenceInterface getOwnerInterface() {
        return ownerInterface;
    }

    public void setOwnerInterface(CInfluenceInterface ownerInterface) {
        this.ownerInterface = ownerInterface;
    }
}
