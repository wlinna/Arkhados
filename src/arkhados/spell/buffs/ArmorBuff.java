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
package arkhados.spell.buffs;

import arkhados.controls.CInfluenceInterface;
import com.jme3.math.FastMath;

public class ArmorBuff extends AbstractBuff {

    private float amount;
    private final float protectionPercent;

    protected ArmorBuff(float amount, float protectionPercent, float duration) {
        super(duration);
        this.amount = amount;
        this.protectionPercent = protectionPercent;
    }

    @Override
    public void attachToCharacter(CInfluenceInterface targetInterface) {

        ArmorBuff armorBuff = null;
        for (AbstractBuff buff : targetInterface.getBuffs()) {
            if (buff instanceof ArmorBuff && buff != this) {
                armorBuff = (ArmorBuff) buff;
                if (armorBuff.getProtectionPercent()
                        != getProtectionPercent()) {
                    armorBuff = null;
                } else {
                    break;
                }
            }
        }
        if (armorBuff == null) {
            super.attachToCharacter(targetInterface);
        } else {
            float newAmount = FastMath.clamp(armorBuff.getAmount()
                    + getAmount(), armorBuff.getAmount(), 100);
            armorBuff.setAmount(newAmount);
        }

    }

    public float mitigate(float damage) {
        float portion = damage * protectionPercent;
        float amountAbsorbed = FastMath.clamp(portion, 0, amount);
        damage -= amountAbsorbed;
        amount -= amountAbsorbed;
        return damage;
    }

    @Override
    public boolean shouldContinue() {
        return super.shouldContinue() && amount > 0;
    }

    public float getAmount() {
        return amount;
    }

    public float getProtectionPercent() {
        return protectionPercent;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    static public class MyBuilder extends AbstractBuffBuilder {

        private float amount;
        private float protectionPercent;

        public MyBuilder(float duration, float amount,
                float protectionPercent) {
            super(duration);
            this.amount = amount;
            this.protectionPercent = protectionPercent;
        }

        @Override
        public ArmorBuff build() {
            return set(new ArmorBuff(amount, protectionPercent, duration));
        }
    }
}