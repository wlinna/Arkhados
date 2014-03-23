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

package arkhados.messages.effect;

import com.jme3.math.Vector3f;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

/**
 *
 * @author william
 */

@Serializable
public class SoundEffectMessage extends AbstractMessage {
    private String soundName;
    private Vector3f location; 

    public SoundEffectMessage() {
    }

    public SoundEffectMessage(String soundName, Vector3f effectLocation) {
        this.location = effectLocation;
        this.soundName = soundName;
    }

    public String getSoundName() {
        return soundName;
    }

    public Vector3f getLocation() {
        return location;
    }        
}