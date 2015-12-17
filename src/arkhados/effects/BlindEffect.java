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
import arkhados.spell.buffs.info.BuffInfoParameters;
import arkhados.util.UserData;


public class BlindEffect extends BuffEffect {
    public int entityId = -1;
    
    public BlindEffect(float timeLeft) {
        super(timeLeft);
    }

    public void addToCharacter(BuffInfoParameters params) {
        ClientBlind blind =
                Globals.app.getStateManager().getState(ClientBlind.class);
        blind.addBlindIfSelf(this, params);
        entityId = params.buffControl.getSpatial()
                .getUserData(UserData.ENTITY_ID);
    }

    @Override
    public void destroy() {
        super.destroy();
        ClientBlind blindManager =
                Globals.app.getStateManager().getState(ClientBlind.class);
        blindManager.removeBuffIfSelf(this);
    }    
}
