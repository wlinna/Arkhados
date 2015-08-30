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
package arkhados;

import com.jme3.system.AppSettings;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;

public class ClientSettings implements ScreenController {

    public static ClientSettings getClientSettings() {
        return clientSettings;
    }
    private Nifty nifty;
    private Screen screen;
    private static final ClientSettings clientSettings = new ClientSettings();
    private AppSettings appSettings = null;
    private float freeCameraSpeed = 500f;
    private ClientMain clientMain;

    public static void initialize(ClientMain clientMain) {
        clientSettings.clientMain = clientMain;
    }

    public static AppSettings getAppSettings() {
        return clientSettings.appSettings;
    }

    public static void setAppSettings(AppSettings aAppSettings) {
        clientSettings.appSettings = aAppSettings;
        if (getAppSettings().containsKey("free_camera_speed")) {
            float freeCamSpeed = getAppSettings()
                    .getInteger("free_camera_speed");
            if (freeCamSpeed > 0f) {
                clientSettings.freeCameraSpeed = freeCamSpeed;
            }
        }
    }

    public static float getFreeCameraSpeed() {
        return clientSettings.freeCameraSpeed;
    }

    public static void setFreeCameraSpeed(float freeCameraSpeed) {
        clientSettings.freeCameraSpeed = freeCameraSpeed;
        if (getAppSettings() != null) {
            getAppSettings().putInteger("free_camera_speed",
                    (int) freeCameraSpeed);
        }
    }

    public void bind(Nifty nifty, Screen screen) {
        this.nifty = nifty;
        this.screen = screen;

        TextField freeCameraSpeedTextField = screen
                .findNiftyControl("free_camera_speed", TextField.class);
        freeCameraSpeedTextField
                .setText(String.valueOf((int) getFreeCameraSpeed()));
    }

    @Override
    public void onStartScreen() {
    }

    @Override
    public void onEndScreen() {
    }

    public void gotoMenu(final String menu) {
        clientMain.enqueue(() -> {
            nifty.gotoScreen(menu);
            return null;
        });
    }

    public void applyGraphicsSettings() {
        TextField freeCameraSpeedField = nifty.getCurrentScreen()
                .findNiftyControl("free_camera_speed", TextField.class);
        String freeCameraSpeedString = freeCameraSpeedField.getDisplayedText();
        try {
            float freeCamSpeed = Integer.parseInt(freeCameraSpeedString);
            if (freeCamSpeed > 0f) {
                ClientSettings.setFreeCameraSpeed(freeCamSpeed);
            }
        } catch (NumberFormatException e) {
            // TODO: Show error message
        }
        try {
            appSettings.save(ClientMain.PREFERENCES_KEY);
        } catch (BackingStoreException ex) {
            Logger.getLogger(ClientSettings.class.getName()).log(Level.SEVERE,
                    "Failed to save graphic settings", ex);
        }
    }
}
