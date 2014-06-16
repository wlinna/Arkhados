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
import arkhados.util.BuffTypeIds;
import com.jme3.asset.AssetManager;
import java.util.HashMap;

/**
 *
 * @author william
 */
public abstract class BuffInformation {
    private static int runningCounter = 0;
    
    protected static AssetManager assetManager = null;
    
    private static HashMap<Integer, BuffInformation> Buffs = new HashMap<>();

    public static void initBuffs() {

        final BuffInformation purifyingFlame = new PurifyingFlameInformation();
        addBuff(BuffTypeIds.PURIFYING_FLAME, purifyingFlame);

        final BuffInformation ignite = new IgniteInformation();
        addBuff(BuffTypeIds.IGNITE, ignite);

        final BuffInformation survivalInstinct = new SurvivalInstinctInformation();
        addBuff(BuffTypeIds.SURVIVAL_INSTINCT, survivalInstinct);

        final BuffInformation deepWounds = new DeepWoundsInformation();
        addBuff(BuffTypeIds.DEEP_WOUNDS, deepWounds);

        final BuffInformation fear = new FearInformation();
        addBuff(BuffTypeIds.FEAR, fear);

        final BuffInformation incapacitate = new IncapacitateInformation();
        addBuff(BuffTypeIds.INCAPACITATE, incapacitate);
        
        final BuffInformation likeAPro = new LikeAProInformation();
        addBuff(BuffTypeIds.LIKE_A_PRO, likeAPro);
    }

    public static BuffInformation getBuffInformation(int typeId) {
        final BuffInformation buffInformation = Buffs.get(typeId);
        return buffInformation;
    }
    
    private static void addBuff(int id, BuffInformation buffInfo) {
        Buffs.put(id, buffInfo);
    }
    
    private String name;
    private float duration;
    private String iconPath = null;

    public abstract BuffEffect createBuffEffect(CharacterBuffControl buffControl, float duration);

    public float getDuration() {
        return this.duration;
    }

    public String getIconPath() {
        return this.iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }
}