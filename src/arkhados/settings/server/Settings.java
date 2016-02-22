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

    private static final Settings instance = new Settings();
    private final SettingsGeneral general;
    private final Deathmatch dm;
    private final TeamDeathmatch tdm;

    private Settings() {
        if (!(new File("settings.toml").exists())) {
            generateDefaultConfigFile();
        }

        Toml toml = new Toml().parse(new File("settings.toml"));

        general = toml.getTable("General").to(SettingsGeneral.class);
        dm = toml.getTable("GameModes.Deathmatch").to(Deathmatch.class);
        tdm = toml.getTable("GameModes.TeamDeathmatch")
                .to(TeamDeathmatch.class);
    }

    public final void generateDefaultConfigFile() {
        try (BufferedWriter bw =
                new BufferedWriter(new FileWriter("settings.toml"));) {
            bw.write(DEFAULT);
        } catch (Exception ex) {
            Logger.getLogger(Settings.class.getName())
                    .log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }

    public static Settings get() {
        return instance;
    }

    public SettingsGeneral General() {
        return general;
    }

    public Deathmatch Deathmatch() {
        return dm;
    }

    public TeamDeathmatch TeamDeathmatch() {
        return tdm;
    }

    static public class Deathmatch {

        private int killLimit;
        private float respawnTime;

        public int getKillLimit() {
            return killLimit;
        }

        public float getRespawnTime() {
            return respawnTime;
        }
    }

    static public class TeamDeathmatch {

        private int killLimit;
        private float respawnTime;

        public int getKillLimit() {
            return killLimit;
        }

        public float getRespawnTime() {
            return respawnTime;
        }
    }
    private final String DEFAULT = String.format(
            "[General]%n"
            + "port = 12345%n"
            + "physicsTicksPerSecond = 60.0%n"
            + "defaultSyncFrequency = 0.05%n"
            + "masterServerAddress = \"52.28.234.119\"%n"
            + "masterServerPort = 12346%n"
            + "gameMode = \"TeamDeathmatch\"%n%n"
            + "[GameModes]%n"
            + "[GameModes.Deathmatch]%n"
            + "killLimit = 25%n"
            + "respawnTime = 6.0%n%n"
            + "[GameModes.TeamDeathmatch]%n"
            + "killLimit = 25%n"
            + "respawnTime = 9.0%n");
}
