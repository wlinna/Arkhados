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
package arkhados.spell.spells.electrobot;

import arkhados.Globals;
import arkhados.actions.cast.ACastBuff;
import arkhados.controls.CEntityVariable;
import arkhados.controls.CInfluenceInterface;
import arkhados.messages.sync.CmdBuff;
import arkhados.spell.Spell;
import arkhados.spell.buffs.AbstractBuff;
import arkhados.spell.buffs.AbstractBuffBuilder;
import arkhados.spell.buffs.ArmorBuff;
import arkhados.util.BuffTypeIds;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class ElectronOrbit extends Spell {

    {
        iconName = "MineralArmor.png";
    }

    public ElectronOrbit(String name, float cooldown, float range,
            float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Spell create() {
        final float cooldown = 13f;
        final float range = 100f;
        final float castTime = 0f;

        final ElectronOrbit spell = new ElectronOrbit("Electron Orbit", cooldown,
                range, castTime);
        spell.castSpellActionBuilder = (Node caster, Vector3f vec) -> {
            ACastBuff action = new ACastBuff(spell, range);
            AbstractBuffBuilder armor
                    = new ElectronArmorBuff.MyBuilder(2000f, 0.5f, 4f);
            action.addBuff(armor);

            return action;
        };

        spell.nodeBuilder = null;

        return spell;
    }
}

class ElectronArmorBuff extends ArmorBuff {

    private int chargesLeft = 3;

    private ElectronArmorBuff(float amount, float protectionPercent,
            float duration) {
        super(amount, protectionPercent, duration, 3);
    }

    @Override
    public void attachToCharacter(CInfluenceInterface targetInterface) {
        // Copy pasted from AbstractBuff, because we don't want to combine
        // ElectronArmorBuffs
        // TODO: Consider hiding this behavior behind a flag in ArmorBuff
        this.targetInterface = targetInterface;
        targetInterface.addBuff(this);

        CmdBuff buffCommand = generateBuffCommand(true);
        if (buffCommand != null) {
            Spatial spatial = targetInterface.getSpatial();
            spatial.getControl(CEntityVariable.class).getAwareness()
                    .getFog().addCommand(spatial, buffCommand);
        }
    }

    @Override
    public float mitigate(float damage) {
        if (damage < 20f) {
            return damage;
        }

        --chargesLeft;
        if (chargesLeft <= 0) {
            setAmount(0f);
        } else {
            changeStackAmount(chargesLeft);
        }

        if (getAmount() > 0f) {
            Globals.app.enqueue(() -> {
                new PowerBuff(3f).attachToCharacter(getOwnerInterface());
            });
        }

        return super.mitigate(damage);
    }

    static class MyBuilder extends AbstractBuffBuilder {

        private final float amount;
        private final float protectionPercent;

        MyBuilder(float amount, float protectionPercent, float duration) {
            super(duration);
            setTypeId(BuffTypeIds.ELECTRON_ORBIT);
            this.amount = amount;
            this.protectionPercent = protectionPercent;
        }

        @Override
        public AbstractBuff build() {
            return set(new ElectronArmorBuff(
                    amount, protectionPercent, duration));
        }
    }
}

class PowerBuff extends AbstractBuff {

    public PowerBuff(float duration) {
        super(duration);
        setTypeId(BuffTypeIds.ELECTRON_CHARGE);
    }

    static class MyBuilder extends AbstractBuffBuilder {

        public MyBuilder(float duration) {
            super(duration);
        }

        @Override
        public AbstractBuff build() {
            return set(new PowerBuff(duration));
        }
    }
}
