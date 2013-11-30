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
package arkhados.spell.buffs.buffinformation;

import arkhados.controls.CharacterBuffControl;
import arkhados.effects.BuffEffect;
import com.jme3.asset.AssetManager;
import java.util.HashMap;

/**
 *
 * @author william
 */
public abstract class BuffInformation {

    protected static AssetManager assetManager = null;
    private static HashMap<String, BuffInformation> Buffs = new HashMap<String, BuffInformation>();

    public static void initBuffs() {

        final BuffInformation purifyingFlame = new PurifyingFlameInformation();
        Buffs.put("Purifying Flame", purifyingFlame);

        final BuffInformation ignite = new IgniteInformation();
        Buffs.put("Ignite", ignite);

        final BuffInformation survivalInstinct = new SurvivalInstinctInformation();
        Buffs.put("Survival Instinct", survivalInstinct);

        final BuffInformation deepWounds = new DeepWoundsInformation();
        Buffs.put("Deep Wounds", deepWounds);

        final BuffInformation fear = new FearInformation();
        Buffs.put("Fear", fear);

        final BuffInformation incapacitate = new IncapacitateInformation();
        Buffs.put("Incapacitate", incapacitate);
    }

    public static BuffInformation getBuffInformation(final String name) {
        final BuffInformation buffInformation = Buffs.get(name);
        return buffInformation;
    }
    private String name;
    private float duration;

    public abstract BuffEffect createBuffEffect(CharacterBuffControl buffControl);

    public float getDuration() {
        return this.duration;
    }
}