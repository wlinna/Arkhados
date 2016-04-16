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
package arkhados.spell.buffs;

import arkhados.controls.CCharacterMovement;
import arkhados.controls.CCharacterPhysics;
import arkhados.controls.CInfluenceInterface;
import arkhados.util.BuffTypeIds;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

public class FearCC extends CrowdControlBuff {

    private Vector3f initialDirection = new Vector3f();

    {
        name = "Fear";
    }

    private FearCC(float duration) {
        super(duration);
    }

    @Override
    public void attachToCharacter(CInfluenceInterface influenceInterface) {
        super.attachToCharacter(influenceInterface);
        Spatial spatial = influenceInterface.getSpatial();

        spatial.getControl(CCharacterMovement.class)
                .setWalkDirection(initialDirection);

        CCharacterPhysics physics =
                spatial.getControl(CCharacterPhysics.class);
        physics.setViewDirection(initialDirection);
    }

    public void setInitialDirection(Vector3f initialDirection) {
        this.initialDirection = initialDirection.normalize();
    }

    @Override
    public void update(float time) {
        super.update(time);
        targetInterface.setCanControlMovement(false);
    }

    @Override
    public void destroy() {
        targetInterface.setCanControlMovement(true);
        super.destroy();

    }

    @Override
    public boolean preventsCasting() {
        return true;
    }

    @Override
    public boolean isDamageSensitive() {
        return true;
    }

    public static class MyBuilder extends AbstractBuffBuilder {

        public MyBuilder(float duration) {
            super(duration);
            setTypeId(BuffTypeIds.FEAR);
        }

        @Override
        public FearCC build() {
            return set(new FearCC(duration));
        }
    }
}
