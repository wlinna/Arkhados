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

import arkhados.controls.InfluenceInterfaceControl;
import com.jme3.math.FastMath;

/**
 *
 * @author william
 */
public class ArmorBuff extends AbstractBuff {

    private float amount;
    private float protectionPercent;

    public ArmorBuff(float amount, float protectionPercent, int buffGroupId, float duration) {
        super(buffGroupId, duration);
        this.amount = amount;
        this.protectionPercent = protectionPercent;
    }

    @Override
    public void attachToCharacter(InfluenceInterfaceControl targetInterface) {

        ArmorBuff armorBuff = null;
        for (AbstractBuff buff : targetInterface.getBuffs()) {
            if (buff instanceof ArmorBuff && buff != this) {
                armorBuff = (ArmorBuff) buff;
                if (armorBuff.getProtectionPercent() != this.getProtectionPercent()) {
                    armorBuff = null;
                } else {
                    break;
                }
            }
        }
        if (armorBuff == null) {
            super.attachToCharacter(targetInterface);
            System.out.println("Added ArmorBuff. Amount of armor: " + this.getAmount());
        } else {
            final float newAmount = FastMath.clamp(armorBuff.getAmount() + this.getAmount(), armorBuff.getAmount(), 100);
            armorBuff.setAmount(newAmount);
            System.out.println("Stacked ArmorBuff. Total armor: " + armorBuff.getAmount());
        }

    }

    public float mitigate(float damage) {
        final float portion = damage * this.protectionPercent;
        final float amountAbsorbed = FastMath.clamp(portion, 0, this.amount);
        damage -= amountAbsorbed;
        this.amount -= amountAbsorbed;
        return damage;
    }

    @Override
    public boolean shouldContinue() {
        return super.shouldContinue() && this.amount > 0;
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
}