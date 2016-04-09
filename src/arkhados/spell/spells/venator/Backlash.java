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
import arkhados.Globals;
import arkhados.actions.ATrance;
import arkhados.actions.EntityAction;
import arkhados.characters.Venator;
import arkhados.controls.CCharacterMovement;
import arkhados.controls.CCharacterPhysics;
import arkhados.controls.CInfluenceInterface;
import arkhados.effects.EffectHandle;
import arkhados.effects.WorldEffect;
import arkhados.spell.Spell;
import arkhados.spell.buffs.AbstractBuff;
import arkhados.spell.buffs.AbstractBuffBuilder;
import arkhados.spell.buffs.SpeedBuff;
import arkhados.util.BuffTypeIds;
import arkhados.util.UserData;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Cylinder;
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

    public static class CastEffect implements WorldEffect {
        
        @Override
        public EffectHandle execute(Node root, Vector3f loc, String param) {
            CCharacterPhysics phys = root.getControl(CCharacterPhysics.class);
            float height = phys.getCapsuleShape().getHeight();
            float radius = phys.getCapsuleShape().getRadius();
            
            Mesh mesh = new Cylinder(2, 32, 1f, 0.1f, true);
            Geometry geom = new Geometry("backlash-action", mesh);
            geom.scale(radius * 1.75f, height, radius * 1.75f);
            geom.setLocalTranslation(0f, 1f, 0f);
            
            Material mat = new Material(Globals.assets,
                    "MatDefs/Backlash/Backlash.j3md");
            mat.getAdditionalRenderState()
                    .setBlendMode(RenderState.BlendMode.AlphaAdditive);
            mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
            geom.setQueueBucket(RenderQueue.Bucket.Transparent);
            geom.setMaterial(mat);

            root.attachChild(geom);
            
            return () -> {
                geom.removeFromParent();
            };
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

    {
        setTypeId(Venator.ACTION_BACKLASH);
    }

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

    @Override
    public void end() {
        super.end();
        announceEnd();
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
                targetInterface, 100f, null, false);
        ownerSpatial.setUserData(UserData.LIFE_STEAL, lifesteal);

        backlash.destroy();
        getOwnerInterface().getBuffs().remove(backlash);
    }
}
