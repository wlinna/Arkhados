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

public class DamageBuff extends AbstractBuff {

    private float factor;

    private DamageBuff(float duration, float amount) {
        super(duration);
        factor = 1 + amount;
    }

    public float getFactor() {
        return factor;
    }

    @Override
    public boolean isFriendly() {
        return factor > 1f;
    }
    
    public static class MyBuilder extends AbstractBuffBuilder {

        private final float amount;

        public MyBuilder(float duration, float amount) {
            super(duration);
            this.amount = amount;
        }

        @Override
        public AbstractBuff build() {
            return set(new DamageBuff(duration, amount));
        }
    }
}
