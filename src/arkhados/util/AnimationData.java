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
package arkhados.util;

import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;

/**
 *
 * @author william
 */
public class AnimationData {
    private static int runningId = 0;
    
    private String name;
    private int id = runningId++;
    private float speed;
    private LoopMode loopMode;

    public AnimationData(String name, float speed, LoopMode loopMode) {
        this.name = name;       
        this.speed = speed;
        this.loopMode = loopMode;
    }

    public String getName() {
        return this.name;
    }

    public float getSpeed() {
        return this.speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public LoopMode getLoopMode() {
        return this.loopMode;
    }

    static public float calculateSpeedForAnimation(
            AnimControl animControl, String animationName,
            float relativeMoment, float timeWanted) {

        float animLength = animControl.getAnimationLength(animationName);
        float moment = relativeMoment * animLength;
        float speed = moment / timeWanted;
        return speed;
    }

    public int getId() {
        return id;
    }
}
