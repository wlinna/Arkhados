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

import arkhados.effects.BuffEffect;
import arkhados.util.BuffTypeIds;
import com.jme3.asset.AssetManager;
import java.util.HashMap;

/**
 *
 * @author william
 */
public abstract class BuffInformation {

    protected static AssetManager assetManager = null;
    private static HashMap<Integer, BuffInformation> Buffs = new HashMap<>();

    public static void initBuffs() {
        BuffInformation slow = new SlowInformation();
        addBuff(BuffTypeIds.SLOW, slow);

        BuffInformation purifyingFlame = new PurifyingFlameInformation();
        addBuff(BuffTypeIds.PURIFYING_FLAME, purifyingFlame);

        BuffInformation ignite = new IgniteInformation();
        addBuff(BuffTypeIds.IGNITE, ignite);
        
        BuffInformation magmaRelease = new MagmaReleaseInformation();
        addBuff(BuffTypeIds.MAGMA_RELEASE, magmaRelease);

        BuffInformation survivalInstinct = new SurvivalInstinctInformation();
        addBuff(BuffTypeIds.SURVIVAL_INSTINCT, survivalInstinct);

        BuffInformation deepWounds = new DeepWoundsInformation();
        addBuff(BuffTypeIds.DEEP_WOUNDS, deepWounds);

        BuffInformation fear = new FearInformation();
        addBuff(BuffTypeIds.FEAR, fear);

        BuffInformation incapacitate = new IncapacitateInformation();
        addBuff(BuffTypeIds.INCAPACITATE, incapacitate);

        BuffInformation likeAPro = new LikeAProInformation();
        addBuff(BuffTypeIds.LIKE_A_PRO, likeAPro);

        PetrifyInformation petrify = new PetrifyInformation();
        addBuff(BuffTypeIds.PETRIFY, petrify);

        MineralArmorInformation mineralArmor = new MineralArmorInformation();
        addBuff(BuffTypeIds.MINERAL_ARMOR, mineralArmor);
        
        BedrockInformation bedrock = new BedrockInformation();
        addBuff(BuffTypeIds.BEDROCK, bedrock);
        
        BlindInformation blind = new BlindInformation();
        addBuff(BuffTypeIds.BLIND, blind);
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

    public abstract BuffEffect createBuffEffect(BuffInfoParameters params);

    public float getDuration() {
        return duration;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }
}