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
package arkhados.settings.server;

import com.moandjiezana.toml.Toml;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Teemu
 */
public class Settings {

    private static Settings instance = null;
    private SettingsGeneral sg;
    private SettingsDeathMatch sdm;

    private Settings() {
        if (!(new File("settings.toml").exists())) {
            generateDefaultConfigFile();
        }

        Toml toml = new Toml().parse(new File("settings.toml"));

        sg = toml.getTable("General").to(SettingsGeneral.class);
        sdm = toml.getTable("GameModes.DeathMatch")
                .to(SettingsDeathMatch.class);
    }

    public final void generateDefaultConfigFile() {
        try (BufferedWriter bw = 
                new BufferedWriter(new FileWriter("settings.toml"));) {
            String newLine = System.getProperty("line.separator");
            bw.write("[General]" + newLine
                    + "port = 12345" + newLine
                    + "physicsTicksPerSecond = 60.0" + newLine
                    + "defaultSyncFrequency = 0.05" + newLine + newLine
                    + "[GameModes]" + newLine
                    + "[GameModes.DeathMatch]" + newLine
                    + "killLimit = 25" + newLine
                    + "respawnTime = 6.0");
        } catch (Exception ex) {
            Logger.getLogger(Settings.class.getName())
                    .log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }

    public static Settings get() {
        if (instance == null) {
            instance = new Settings();
        }

        return instance;
    }

    public SettingsGeneral General() {
        return sg;
    }

    public SettingsDeathMatch DeathMatch() {
        return sdm;
    }
}
