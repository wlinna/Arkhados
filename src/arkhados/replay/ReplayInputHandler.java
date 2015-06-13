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
package arkhados.replay;

import arkhados.Globals;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.FastMath;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReplayInputHandler extends AbstractAppState
        implements ActionListener {

    private static final String FASTER = "replay-speed-faster";
    private static final String SLOWER = "replay-speed-slower";
    private static final String DEFAULT = "replay-speed-default";
    private static final String LOG_TIME = "replay-log-time";
    private float speed;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        InputManager inputManager = app.getInputManager();
        inputManager.addMapping(FASTER, new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping(SLOWER, new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping(DEFAULT, new KeyTrigger(KeyInput.KEY_0));
        inputManager.addMapping(LOG_TIME, new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(this, FASTER, SLOWER, DEFAULT, LOG_TIME);
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (!isEnabled() || isPressed) {
            return;
        }

        ReplayReader reader =
                Globals.app.getStateManager().getState(ReplayReader.class);

        switch (name) {
            case FASTER:
                speed += 0.2f;
                reader.setSpeed(speed);
                break;
            case SLOWER:
                speed = FastMath.clamp(speed - 0.2f, 0f, speed);
                reader.setSpeed(speed);
                break;
            case DEFAULT:
                speed = 1f;
                reader.setSpeed(speed);
                break;
            case LOG_TIME:
                float time = reader.getTime();
                Logger.getLogger(ReplayReader.class.getName())
                        .log(Level.INFO, "Current time: {0}", time);
                break;
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();
        InputManager inputManager = Globals.app.getInputManager();
        inputManager.deleteMapping(FASTER);
        inputManager.deleteMapping(SLOWER);
        inputManager.deleteMapping(DEFAULT);
        inputManager.deleteMapping(LOG_TIME);
        inputManager.removeListener(this);
    }
}
