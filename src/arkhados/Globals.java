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

import com.jme3.app.Application;
import com.jme3.asset.AssetManager;

/**
 * This has static references to few instances that are used globally.
 * @author william
 */
public class Globals {
    public static String SERVER = "localhost";
    public static int PORT = 12345;
    public static float DEFAULT_SYNC_FREQUENCY = 0.05f;
    public static boolean replayMode = false;
    
    public static void setPacketsPerSecond(float packets) {
        DEFAULT_SYNC_FREQUENCY = 1f / packets;
    }
    
    public static AssetManager assetManager = null;
    public static Application app = null;
    
    // FIXME: Make own class for variables that describe world state
    public static boolean worldRunning = false;
}