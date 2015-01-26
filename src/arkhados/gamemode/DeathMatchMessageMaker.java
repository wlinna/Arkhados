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

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author william
 */
public class DeathMatchMessageMaker {

    private static final Map<Integer, String> spreeMessages = new HashMap<>();
    private static final Map<Integer, String> spreeEndMessages =
            new HashMap<>();
    private static final Map<Integer, String> comboMessages = new HashMap<>();

    static {
        spreeMessages.put(3, "%s is on killing spree!");
        spreeMessages.put(4, "%s scored Mega Kill!");
        spreeMessages.put(5, "%s is Dominating!");
        spreeMessages.put(6, "%s is Owning!");
        spreeMessages.put(7, "%s is causing major Mayhem!");
        spreeMessages.put(8, "%s just isn't going to stop! CARNAGE");
        spreeMessages.put(9, "%s is GODLIKE!");
        
        spreeEndMessages.put(3, "%s ended %s killing spree");
        spreeEndMessages.put(4, "%s ended %s mega kill!");
        spreeEndMessages.put(5, "%s just put end to %s dominion!");
        spreeEndMessages.put(6, "%s just ended %s ownage!");
        spreeEndMessages.put(7, "%s just put order to %s mayhem");
        spreeEndMessages.put(8, "%s just avenged for everyone by killing %s");
        spreeEndMessages.put(9, "ALL FEAR THE GODSLAYER %s who took %s down!");
        
        comboMessages.put(2, "%s scored DOUBLE KILL");
        comboMessages.put(3, "%s scored TRIPLE KILL");
        comboMessages.put(4, "%s is on RAMGAGE!");
        comboMessages.put(5, "%s is MURDERING MADMAN!");
    }

    public static String spree(String name, int spree) {        
        return String.format(spreeMessages.get(spree), name);
    }
    
    public static String killed(String target, String killer,
            int spree) {
        String targetGenetive = target.endsWith("s")
                ? target + "'"
                : target + "'s";
        
        // TODO: Remove this repetition of if-else-if
        if (spree < 3) {
            return String.format("%s just pwned %s head", killer,
                    targetGenetive);
        } else if (spree > 9) {
            spree = 9;
        }

        String template = spreeEndMessages.get(spree);

        switch (spree) {
            case 8:
            case 9:
                return String.format(template, killer, target);
            default:
                return String.format(template, killer, targetGenetive);
        }
    }
    
    public static String combo(String killer, int combo) {
        if (combo < 2) {
            return "";
        } else if (combo > 5) {
            combo = 5;
        }
        
        return String.format(comboMessages.get(combo), killer);                        
    }
}
