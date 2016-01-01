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
package arkhados.effects;

import arkhados.Globals;
import arkhados.controls.CTimedExistence;
import com.jme3.audio.AudioNode;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

public class SimpleSoundEffect implements WorldEffect {
    private final String path;
    private float volume = 1f;
    private final boolean attach;

    public SimpleSoundEffect(String path, boolean attach) {
        this.path = path;
        this.attach = attach;
    }

    public SimpleSoundEffect(String path) {
        this(path, false);
    }    
    
    @Override
    public EffectHandle execute(Node root, Vector3f location, String parameter) {
        AudioNode sound = new AudioNode(Globals.assets, this.path);
        sound.setPositional(true);
        if (attach) {
            root.attachChild(sound);
        }
        sound.setLocalTranslation(location);
        sound.addControl(new CTimedExistence(sound.getAudioData().getDuration()));
        sound.setReverbEnabled(false);
        sound.setVolume(volume);
        sound.play();
        return null;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }
}
