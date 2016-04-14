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

import arkhados.CharacterInteraction;
import arkhados.util.BuffTypeIds;

public class MagmaReleaseBuff extends AbstractBuff {

    public static final float TICK_LENGTH = 0.2f;
    private static final float DAMAGE = 60f;
    private float currentTime = 0f;
    private int count = 0;

    private MagmaReleaseBuff(float duration) {
        super(duration);
    }

    @Override
    public void update(float time) {
        super.update(time);
        currentTime += time;

        if (count <= 0) {
            duration = 0f;
            return;
        }

        if (currentTime > TICK_LENGTH) {
            currentTime = 0f;
            --count;

            CharacterInteraction.harm(getOwnerInterface(),
                    targetInterface, DAMAGE, null, true);
        }
    }

    public static class MyBuilder extends AbstractBuffBuilder {

        public MyBuilder(float duration) {
            super(duration);
            setTypeId(BuffTypeIds.MAGMA_RELEASE);
        }

        @Override
        public AbstractBuff build() {
            return set(new MagmaReleaseBuff(duration));
        }
    }
}
