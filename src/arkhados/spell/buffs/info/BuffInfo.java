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
package arkhados.spell.buffs.info;

import arkhados.effects.BuffEffect;
import arkhados.util.BuffTypeIds;
import com.jme3.util.IntMap;

public abstract class BuffInfo {

    private static final IntMap<BuffInfo> Buffs = new IntMap<>();

    public static void initBuffs() {
        addBuff(BuffTypeIds.SLOW, new SlowInfo());
        addBuff(BuffTypeIds.PURIFYING_FLAME, new PurifyingFlameInfo());
        addBuff(BuffTypeIds.IGNITE, new IgniteInfo());
        addBuff(BuffTypeIds.MAGMA_RELEASE, new MagmaReleaseInfo());
        addBuff(BuffTypeIds.BACKLASH, new BacklashInfo());
        addBuff(BuffTypeIds.DEEP_WOUNDS, new DeepWoundsInfo());
        addBuff(BuffTypeIds.FEAR, new FearInfo());
        addBuff(BuffTypeIds.INCAPACITATE, new IncapacitateInfo());
        addBuff(BuffTypeIds.LIKE_A_PRO, new LikeAProInfo());
        addBuff(BuffTypeIds.PETRIFY, new PetrifyInfo());
        addBuff(BuffTypeIds.MINERAL_ARMOR, new MineralArmorInfo());
        addBuff(BuffTypeIds.BEDROCK, new BedrockInfo());
        addBuff(BuffTypeIds.BLIND, new BlindInfo());
        addBuff(BuffTypeIds.BLOOD_FRENZY, new BloodFrenzyInfo());
        addBuff(BuffTypeIds.SHADOW, new ShadowInfo());
        addBuff(BuffTypeIds.VOID_SPEAR, new VoidSpearInfo());
        addBuff(BuffTypeIds.DRAIN, new DrainInfo());
        addBuff(BuffTypeIds.ELECTRO_CHARGE, new ElectroChargeInfo());
        addBuff(BuffTypeIds.ELECTRON_ORBIT, new ElectronOrbitInfo());
        addBuff(BuffTypeIds.ELECTRON_CHARGE, new ElectronChargeInfo());
    }

    public static BuffInfo getBuffInfo(int typeId) {
        return Buffs.get(typeId);
    }

    private static void addBuff(int id, BuffInfo buffInfo) {
        Buffs.put(id, buffInfo);
    }
    
    private String iconPath = null;

    public abstract BuffEffect createBuffEffect(BuffInfoParameters params);

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }
}