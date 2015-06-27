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

public abstract class AbstractBuffBuilder implements Builder<AbstractBuff> {

    public float duration;
    private int typeId = -1;
    private CInfluenceInterface ownerInterface;
    private String name;

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
        return buff;
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

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public CInfluenceInterface getOwnerInterface() {
        return ownerInterface;
    }

    public void setOwnerInterface(CInfluenceInterface ownerInterface) {
        this.ownerInterface = ownerInterface;
    }
}
