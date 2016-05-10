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
 * Crow Control buff that causes entity to not be able to move or cast spells.
 * If sensitive is set to false, Incapacitate is called Stun instead.
 * Note: CC is stands for Crowd Control
 */
public class IncapacitateCC extends CrowdControlBuff {

    {
        name = "Incapacitate";
    }

    private final boolean sensitive;

    protected IncapacitateCC(float duration, boolean sensitive) {
        super(duration);
        this.sensitive = sensitive;
    }

    @Override
    public boolean preventsCasting() {
        return true;
    }

    @Override
    public boolean preventsMoving() {
        return true;
    }

    @Override
    public boolean isDamageSensitive() {
        return sensitive;
    }

    public static class MyBuilder extends AbstractBuffBuilder {

        private boolean sensitive = true;

        public MyBuilder(float duration) {
            super(duration);
            setTypeId(BuffTypeIds.INCAPACITATE);
        }

        public MyBuilder setSensitive(boolean sensitive) {
            this.sensitive = sensitive;
            return this;
        }

        @Override
        public AbstractBuff build() {
            return set(new IncapacitateCC(duration, sensitive));
        }
    }
}
