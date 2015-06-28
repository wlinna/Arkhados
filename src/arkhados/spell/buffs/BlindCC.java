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

import arkhados.controls.CInfluenceInterface;
import arkhados.util.BuffTypeIds;

public class BlindCC extends CrowdControlBuff {

    private BlindCC(float duration) {
        super(duration);
    }

    @Override
    public void attachToCharacter(CInfluenceInterface targetInterface) {
        super.attachToCharacter(targetInterface);
        targetInterface.getBlinds().add(this);
    }

    @Override
    public void destroy() {
        super.destroy();
        targetInterface.getBlinds().remove(this);
    }

    public static class MyBuilder extends AbstractBuffBuilder {
        {
            setTypeId(BuffTypeIds.BLIND);
        }

        public MyBuilder(float duration) {
            super(duration);
        }

        @Override
        public AbstractBuff build() {
            return set(new BlindCC(duration));
        }
    }
}
