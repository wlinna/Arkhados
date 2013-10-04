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
public class SlowCC extends CrowdControlBuff {

    private float slowFactor;

    public SlowCC(long id, float duration, float slow) {
        super(id, duration);
        this.slowFactor = 1f - slow;
    }

    public float getSlowFactor() {
        return this.slowFactor;
    }
}
