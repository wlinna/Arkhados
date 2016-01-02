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
package arkhados.spell.buffs.info;

import arkhados.Globals;
import arkhados.effects.BuffEffect;
import com.jme3.audio.AudioNode;

import com.jme3.scene.Node;

public class DrainInfo extends BuffInfo {

    {
        setIconPath("Interface/Images/SpellIcons/Drain.png");
    }

    @Override
    public BuffEffect createBuffEffect(BuffInfoParameters params) {
        DrainHitEffect hitEffect = new DrainHitEffect(params.duration);
        hitEffect.addToCharacter(params);
        return hitEffect;
    }
}

class DrainHitEffect extends BuffEffect {

    private Node characterNode;
    private AudioNode sound;

    public DrainHitEffect(float timeLeft) {
        super(timeLeft);
    }

    public void addToCharacter(BuffInfoParameters params) {
        characterNode = (Node) params.buffControl.getSpatial();

        if (params.justCreated) {
            sound = new AudioNode(Globals.assets, "Effects/Sound/DrainHit.wav");
            sound.setPositional(true);
            sound.setReverbEnabled(false);
            sound.setVolume(1f);
            characterNode.attachChild(sound);
            sound.play();
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (sound != null) {
            sound.removeFromParent();
        }
    }
}
