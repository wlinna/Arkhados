/*    This file is part of JMageBattle.

    JMageBattle is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    JMageBattle is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with JMageBattle.  If not, see <http://www.gnu.org/licenses/>. */
package magebattle.actions;

/**
 *
 * @author william
 */
public class DelayAction extends EntityAction {
    private float delay;

    public DelayAction(float delay) {
        this.delay = delay;
    }

    @Override
    public boolean update(float tpf) {
        this.delay -= tpf;
        if (this.delay <= 0f) {
            return false;
        }
        return true;
    }

}
