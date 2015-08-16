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

import arkhados.Globals;
import arkhados.effects.WorldEffect;
import com.jme3.audio.AudioNode;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.control.AbstractControl;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Teemu
 */
public class CCharacterSound extends AbstractControl {

    private float suffering = 0;
    private String sufferPath;
    private String deathPath;
    
    private Map<Integer, WorldEffect> castSounds = new HashMap<>();

    @Override
    protected void controlUpdate(float tpf) {
        suffering += tpf;
    }

    public void suffer(float damage) {
        if (suffering <= 2 || sufferPath == null) {
            return;
        }

        AudioNode sound = new AudioNode(Globals.assets, sufferPath);
        sound.setPositional(true);
        sound.setReverbEnabled(false);
        sound.setVolume(1f);
        Node playerNode = (Node) getSpatial();
        playerNode.attachChild(sound);
        sound.play();
        suffering = 0;
    }

    public void death() {
        if (deathPath == null) {
            return;
        }

        AudioNode sound = new AudioNode(Globals.assets, deathPath);
        sound.setPositional(true);
        sound.setReverbEnabled(false);
        sound.setVolume(1f);
        Node playerNode = (Node) getSpatial();
        playerNode.attachChild(sound);
        sound.play();
    }
    
    public void addCastSound(int spellId, WorldEffect effect) {
        castSounds.put(spellId, effect);
    }
    
    public void castSound(int spellId) {
        WorldEffect effect = castSounds.get(spellId);
        if (effect != null) {
            effect.execute((Node) spatial, spatial.getLocalTranslation(), null);
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public void setSufferSound(String path) {
        this.sufferPath = path;
    }

    public void setDeathSound(String path) {
        this.deathPath = path;
    }
}