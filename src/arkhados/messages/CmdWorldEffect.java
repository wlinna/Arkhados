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

package arkhados.messages;

import arkhados.net.Command;
import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;

/**
 *
 * @author william
 */

@Serializable
public class CmdWorldEffect implements Command {
    private short effectId;
    private Vector3f location = new Vector3f();

    public CmdWorldEffect() {
    }

    public CmdWorldEffect(int effectId, Vector3f location) {
        this.effectId = (short) effectId;
        this.location.set(location);
    }

    @Override
    public boolean isGuaranteed() {
        return true;
    }

    public short getEffectId() {
        return effectId;
    }

    public Vector3f getLocation() {
        return location;
    }    
}
