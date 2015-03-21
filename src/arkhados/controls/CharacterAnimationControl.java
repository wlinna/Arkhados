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

import arkhados.spell.Spell;
import arkhados.util.AnimationData;
import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Original animation control for Mage. Most likely each character needs its own custom character
 * animation control and this will be changed to abstract class
 *
 * @author william
 */
public class CharacterAnimationControl extends AbstractControl {

    private AnimControl animControl;
    private CharacterPhysicsControl cPhysics;
    private CCharacterMovement cMovement;
    private AnimChannel channel;
    private float actionTime = 0f;
    private final HashMap<String, AnimationData> spellAnimationMap = new HashMap<>(6);
    private final ArrayList<AnimationData> actionAnimations = new ArrayList<>(8);
    // TODO: Allow mapping of animations to specific AnimChannels
    private AnimationData walkAnimation;
    private AnimationData deathAnimation;

    public CharacterAnimationControl(AnimControl animControl) {
        this.animControl = animControl;
    }

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        cPhysics = spatial.getControl(CharacterPhysicsControl.class);
        cMovement = spatial.getControl(CCharacterMovement.class);
        channel = animControl.createChannel();
        channel.setAnim(walkAnimation.getName());
        channel.setSpeed(walkAnimation.getSpeed());
    }

    @Override
    protected void controlUpdate(float tpf) {
        actionTime -= tpf;
        if (actionTime > 0f) {
            return;
        }

        if (!cMovement.getWalkDirection().equals(Vector3f.ZERO)
                && !cPhysics.isMotionControlled()) {
            if (!walkAnimation.getName().equals(channel.getAnimationName())) {
                channel.setAnim(walkAnimation.getName(), walkAnimation.getSpeed());
            }
            channel.setSpeed(walkAnimation.getSpeed());
        } else {
            channel.setSpeed(0.0f);
        }
    }

    public void death() {
        channel.setAnim(deathAnimation.getName());
        channel.setSpeed(deathAnimation.getSpeed());
        channel.setLoopMode(LoopMode.DontLoop);
        setEnabled(false);
    }

    public void castSpell(Spell spell) {
        AnimationData animationData = spellAnimationMap.get(spell.getName());
        if (animationData == null) {
            return;
        }

        actionTime = spell.getCastTime();

        channel.setAnim(animationData.getName());
        channel.setSpeed(animationData.getSpeed());
        channel.setLoopMode(animationData.getLoopMode());
    }

    public void animateAction(int actionId, float actionDuration) {
        if (actionId >= actionAnimations.size()) {
            return;
        }

        AnimationData data = actionAnimations.get(actionId);
        channel.setAnim(data.getName());
        channel.setSpeed(data.getSpeed());
        channel.setLoopMode(data.getLoopMode());
        if ((data.getLoopMode() == LoopMode.Loop
                || data.getLoopMode() == LoopMode.Cycle) && actionDuration != -1) {
            actionTime = actionDuration;
        } else {
            actionTime = channel.getAnimMaxTime() / data.getSpeed();
        }
    }

    public void animateAction(int actionId) {
        animateAction(actionId, -1);
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public void addSpellAnimation(String spellName, AnimationData animData) {
        spellAnimationMap.put(spellName, animData);
    }

    public void addActionAnimation(AnimationData data) {
        actionAnimations.add(data);
    }

    public AnimationData getWalkAnimation() {
        return walkAnimation;
    }

    public void setWalkAnimation(AnimationData walkAnimation) {
        this.walkAnimation = walkAnimation;
    }

    public AnimationData getDeathAnimation() {
        return deathAnimation;
    }

    public void setDeathAnimation(AnimationData deathAnimation) {
        this.deathAnimation = deathAnimation;
    }

    public AnimControl getAnimControl() {
        return animControl;
    }

    public float getActionTime() {
        return actionTime;
    }
}