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

import arkhados.util.Timer;

/**
 *
 * @author william
 */
public class DeathMatchPlayerTracker {
    private Timer spawnTimer = new Timer(0f);
    private int killingSpree = 0;
    private int combo = 0;
    private Timer comboTimer = new Timer(0f);

    public DeathMatchPlayerTracker(float spawnDelay) {
        spawnTimer.setTimeLeft(spawnDelay);
        spawnTimer.setActive(true);
        comboTimer.setActive(true);
    }        
    
    public void update(float tpf) {
        comboTimer.update(tpf);
        spawnTimer.update(tpf);
        
        if (comboTimer.getTimeLeft() <= 0) {
            combo = 0;
        }
    }    
    
    public void death(float spawnDelay, boolean byEnvironment) {
        killingSpree = byEnvironment ? killingSpree : 0;
        combo = 0;
        spawnTimer.setTimeLeft(spawnDelay);
    }
    
    public void addKill() {
        ++killingSpree;
        ++combo;
        
        comboTimer.setTimeLeft(12f);
    }

    public int getKillingSpree() {
        return killingSpree;
    }

    public int getCombo() {
        return combo;
    }
    
    public float getSpawnTimeLeft() {
        return spawnTimer.getTimeLeft();
    }
}
