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
import arkhados.controls.TimedExistenceControl;
import com.jme3.audio.AudioNode;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 *
 * @author william
 */
public class SimpleSoundEffect implements WorldEffect {
    private final String path;
    private float volume = 1f;

    public SimpleSoundEffect(String path) {
        this.path = path;
    }    
    
    @Override
    public void execute(Node root, Vector3f location, String parameter) {
        AudioNode sound = new AudioNode(Globals.assetManager, this.path);
        sound.setPositional(true);
        sound.setLocalTranslation(location);
        sound.addControl(new TimedExistenceControl(sound.getAudioData().getDuration()));
        sound.setReverbEnabled(false);
        sound.setVolume(volume);
        sound.play();
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }
}
