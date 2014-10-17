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

import arkhados.ClientMain;
import com.jme3.app.Application;

/**
 *
 * @author william
 */
public abstract class GameMode {
    private Application app;
    private boolean running = false;
    
    public void initialize(Application app) {
        this.app = app;
    }

    public abstract void startGame();

    public abstract void update(float tpf);

    public void playerJoined(int playerId) { }
    
    public abstract void playerDied(int playerId, int killersPlayerId);
    
    public void cleanup() {        
    }  
    
    public void gameEnded() {        
        if (app instanceof ClientMain) {
            ClientMain clientMain = (ClientMain) app;
            clientMain.gameEnded();
        }
    }

    public Application getApp() {
        return app;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }    
}