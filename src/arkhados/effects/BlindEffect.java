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
package arkhados.effects;

import arkhados.Globals;
import arkhados.spell.buffs.buffinformation.BuffInfoParameters;


public class BlindEffect extends BuffEffect {

    public BlindEffect(float timeLeft) {
        super(timeLeft);
    }

    public void addToCharacter(BuffInfoParameters params) {
        BlindManager blindManager =
                Globals.app.getStateManager().getState(BlindManager.class);
        blindManager.addBlindIfSelf(this, params);        
    }

    @Override
    public void destroy() {
        super.destroy();
        BlindManager blindManager =
                Globals.app.getStateManager().getState(BlindManager.class);
        blindManager.removeBuffIfSelf(this);
    }    
}
