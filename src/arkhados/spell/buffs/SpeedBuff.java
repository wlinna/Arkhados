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

/**
 *
 * @author william
 */
public class SpeedBuff extends AbstractBuff {

    private float factor;
    private float constant;

    protected SpeedBuff(float percent, float constant, float duration) {
        super(duration);

        factor = 1 + percent;
        this.constant = constant;
    }

    public float getFactor() {
        return factor;
    }

    public float getConstant() {
        return constant;
    }

    public static class MyBuilder extends AbstractBuffBuilder {
        private final float constant;
        private final float percent;

        public MyBuilder(float percent, float constant, float duration) {
            super(duration);
            this.constant = constant;
            this.percent = percent;
        }

        @Override
        public AbstractBuff build() {
            return set(new SpeedBuff(percent, constant, duration));
        }
    }
}
