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

import arkhados.controls.CharacterBuffControl;
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
        super.setIconPath("Textures/icons/skull.png");
    }

    @Override
    public BuffEffect createBuffEffect(CharacterBuffControl buffControl) {
        final FearEffect effect = new FearEffect(super.getDuration());
        effect.addToCharacter(buffControl);
        return effect;
    }
}

class FearEffect extends BuffEffect {    
    private Spatial fearIcon = null;

    public FearEffect(float timeLeft) {
        super(timeLeft);
    }

    public void addToCharacter(CharacterBuffControl buffControl) {
        final Quad blanket = new Quad(8, 8, true);
        this.fearIcon = new Geometry("fear-icon", blanket);
        this.fearIcon.scale(1f);
        final Material skullMaterial = assetManager.loadMaterial("Materials/FearMaterial.j3m");
        this.fearIcon.setMaterial(skullMaterial);
        this.fearIcon.setQueueBucket(RenderQueue.Bucket.Transparent);


        final Node characterNode = (Node) buffControl.getSpatial();
        characterNode.attachChild(this.fearIcon);
        this.fearIcon.center();
        this.fearIcon.setLocalTranslation(0f, 22f, 0f);

        final BillboardControl billBoard = new BillboardControl();
        this.fearIcon.addControl(billBoard);
    }

    @Override
    public void destroy() {
        super.destroy();
        assert this.fearIcon != null;
        this.fearIcon.removeFromParent();
    }
}