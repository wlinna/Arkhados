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

import arkhados.util.BuffTypeIds;

/**
 *
 * @author william
 */
public class SlowCC extends CrowdControlBuff {

    private float slowFactor;

    private SlowCC(float duration, float slow) {
        super(duration);
        slowFactor = 1f - slow;
    }

    public float getSlowFactor() {
        return slowFactor;
    }

    public static class MyBuilder extends AbstractBuffBuilder {

        private float slowFactor;

        public MyBuilder(float duration, float slowFactor) {
            super(duration);
            setTypeId(BuffTypeIds.SLOW);
            this.slowFactor = slowFactor;
        }

        @Override
        public SlowCC build() {
            return set(new SlowCC(duration, slowFactor));
        }
    }
}
