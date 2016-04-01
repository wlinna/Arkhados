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
package arkhados.spell.spells.venator;

import arkhados.CharacterInteraction;
import arkhados.actions.ATrance;
import arkhados.actions.EntityAction;
import arkhados.controls.CCharacterMovement;
import arkhados.controls.CInfluenceInterface;
import arkhados.spell.Spell;
import arkhados.spell.buffs.AbstractBuff;
import arkhados.spell.buffs.AbstractBuffBuilder;
import arkhados.spell.buffs.SpeedBuff;
import arkhados.util.BuffTypeIds;
import arkhados.util.UserData;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.List;

public class Backlash extends Spell {

    static final float BUFF_DURATION = 3f;

    {
        iconName = "survival_instinct.png";
    }

    static class TriggerBuffBuilder extends AbstractBuffBuilder {

        private final Buff backlash;

        public TriggerBuffBuilder(Buff backlash) {
            super(0f);
            this.backlash = backlash;
        }

        @Override
        public AbstractBuff build() {
            return set(new TriggerBuff(duration, backlash));
        }
    }

    static class Buff extends AbstractBuff {

        private Buff() {
            super(BUFF_DURATION);
        }

        public static class MyBuilder extends AbstractBuffBuilder {

            public MyBuilder() {
                super(BUFF_DURATION);
                setTypeId(BuffTypeIds.BACKLASH);
            }

            @Override
            public AbstractBuff build() {
                return set(new Buff());
            }
        }
    }

    public static AbstractBuffBuilder giveTriggerIfValid(Spatial spatial) {
        CInfluenceInterface cInfluence = spatial
                .getControl(CInfluenceInterface.class);
        List<AbstractBuff> buffs = cInfluence.getBuffs();

        Backlash.Buff backlash = null;
        for (AbstractBuff buff : buffs) {
            if (buff instanceof Backlash.Buff) {
                backlash = (Backlash.Buff) buff;
            }
        }

        return backlash != null
                ? new Backlash.TriggerBuffBuilder(backlash)
                : null;
    }

    public Backlash(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Spell create() {
        final float cooldown = 8f;
        final float range = 0f;
        final float castTime = 0.10f;

        Backlash spell = new Backlash("Backlash",
                cooldown, range, castTime);

        spell.castSpellActionBuilder = (Node caster, Vector3f vec)
                -> new ABacklash();

        return spell;
    }
}

class ABacklash extends EntityAction implements ATrance {

    private float timeLeft = 2f;
    private CInfluenceInterface cInfluence;
    private CCharacterMovement cMovement;

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        cInfluence = spatial.getControl(CInfluenceInterface.class);
        cMovement = spatial.getControl(CCharacterMovement.class);
    }

    @Override
    public boolean update(float tpf) {
        timeLeft -= tpf;
        if (timeLeft <= 0f) {
            return false;
        }

        if (!cInfluence.isAbleToCastWhileMoving()) {
            cMovement.stop();
        }

        return true;
    }

    @Override
    public void activate(Spatial activator) {
        timeLeft = 0f;

        Backlash.Buff.MyBuilder backlashBuilder = new Backlash.Buff.MyBuilder();
        backlashBuilder.setOwnerInterface(cInfluence);
        AbstractBuff backlash = backlashBuilder.build();
        backlash.attachToCharacter(cInfluence);

        SpeedBuff.MyBuilder speedBuilder = new SpeedBuff.MyBuilder(0.3f, 0f,
                Backlash.BUFF_DURATION);
        speedBuilder.setTypeId(BuffTypeIds.BACKLASH);
        AbstractBuff speed = speedBuilder.build();
        speed.attachToCharacter(cInfluence);
    }
}

class TriggerBuff extends AbstractBuff {

    private final Backlash.Buff backlash;

    public TriggerBuff(float duration, Backlash.Buff backlash) {
        super(duration);
        this.backlash = backlash;
    }

    @Override
    public void attachToCharacter(CInfluenceInterface targetInterface) {
        Spatial ownerSpatial = getOwnerInterface().getSpatial();
        float lifesteal = ownerSpatial.getUserData(UserData.LIFE_STEAL);

        ownerSpatial.setUserData(UserData.LIFE_STEAL, lifesteal + 1f);
        CharacterInteraction.harm(getOwnerInterface(),
                targetInterface, 100f, null, true);
        ownerSpatial.setUserData(UserData.LIFE_STEAL, lifesteal);

        backlash.destroy();
        getOwnerInterface().getBuffs().remove(backlash);
    }
}
