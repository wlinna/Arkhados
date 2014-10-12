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
package arkhados.gamemode;

import arkhados.RoundManager;
import arkhados.net.Receiver;
import com.jme3.app.Application;

/**
 *
 * @author william
 */
public class LastManStanding extends GameMode {

    private RoundManager roundManager;

    @Override
    public void initialize(Application app) {
        super.initialize(app);

        roundManager = new RoundManager();
        roundManager.initialize(app, this);
        app.getStateManager().getState(Receiver.class).registerCommandHandler(roundManager);
    }

    @Override
    public void startGame() {
        roundManager.serverStartGame();
        setRunning(true);
    }

    @Override
    public void update(float tpf) {
        if (!isRunning()) {
            return;
        }

        roundManager.update(tpf);
    }

    @Override
    public void playerDied(int playerId) {
    }

    @Override
    public void gameEnded() {
        super.gameEnded();

        Receiver receiver = getApp().getStateManager().getState(Receiver.class);
        receiver.removeCommandHandler(roundManager);
    }
}
