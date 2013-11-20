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
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;

/**
 *
 * @author william
 */
public class PurifyingFlameInformation extends BuffInformation {

    @Override
    public BuffEffect createBuffEffect(CharacterBuffControl buffControl) {
        final FlameShield flameShield = new FlameShield(super.getDuration());
        flameShield.addToCharacter(buffControl);
        return flameShield;
    }
}

class FlameShield extends BuffEffect {

    public FlameShield(float timeLeft) {
        super(timeLeft);
    }
    private Geometry geometry = null;

    public void addToCharacter(final CharacterBuffControl buffControl) {
        final Node characterNode = (Node) buffControl.getSpatial();

        final float radius = 12f;
        final Sphere sphere = new Sphere(32, 32, radius);
        this.geometry = new Geometry("shield-geom", sphere);

//        final Material material = BuffEffect.assetManager.loadMaterial("Materials/PurifyingMaterial.j3m");
//        final Material material = BuffEffect.assetManager.loadMaterial("Materials/EmberCircleGround.j3m");
        final Material material = new Material(BuffEffect.assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        final ColorRGBA color = ColorRGBA.Orange.clone();
        color.a = 0.2f;
        material.setColor("Color", color);

        material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

        geometry.setQueueBucket(RenderQueue.Bucket.Transparent);
//        material.setTexture("AlphaMap", null);
        geometry.setMaterial(material);

        characterNode.attachChild(geometry);
        geometry.move(0f, 10f, 0f);
    }

    @Override
    public void destroy() {
        assert this.geometry != null;
        this.geometry.removeFromParent();
    }
}
