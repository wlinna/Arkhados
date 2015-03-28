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
package arkhados.spell.buffs.buffinformation;

import arkhados.controls.CCharacterBuff;
import arkhados.effects.BuffEffect;
import com.jme3.material.Material;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.BillboardControl;
import com.jme3.scene.shape.Quad;

/**
 *
 * @author william
 */
public class FearInformation extends BuffInformation {
    {
        setIconPath("Textures/icons/skull.png");
    }

    @Override
    public BuffEffect createBuffEffect(CCharacterBuff buffControl, float duration) {
        FearEffect effect = new FearEffect(duration);
        effect.addToCharacter(buffControl);
        return effect;
    }
}

class FearEffect extends BuffEffect {    
    private Spatial fearIcon = null;

    public FearEffect(float timeLeft) {
        super(timeLeft);
    }

    public void addToCharacter(CCharacterBuff buffControl) {
        Quad blanket = new Quad(8, 8, true);
        fearIcon = new Geometry("fear-icon", blanket);
        fearIcon.scale(1f);
        Material skullMaterial = assetManager.loadMaterial("Materials/FearMaterial.j3m");
        fearIcon.setMaterial(skullMaterial);
        fearIcon.setQueueBucket(RenderQueue.Bucket.Transparent);


        Node characterNode = (Node) buffControl.getSpatial();
        characterNode.attachChild(fearIcon);
        fearIcon.center();
        fearIcon.setLocalTranslation(0f, 22f, 0f);

        BillboardControl billBoard = new BillboardControl();
        fearIcon.addControl(billBoard);
    }

    @Override
    public void destroy() {
        super.destroy();
        assert fearIcon != null;
        fearIcon.removeFromParent();
    }
}