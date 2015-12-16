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
package arkhados.controls;

import arkhados.Globals;
import arkhados.World;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import java.util.HashMap;
import java.util.Map;
import arkhados.actions.ACastingSpell;
import arkhados.actions.AChannelingSpell;
import arkhados.actions.ATrance;
import arkhados.actions.EntityAction;
import arkhados.messages.sync.CmdSetCooldown;
import arkhados.messages.sync.CmdStartCastingSpell;
import arkhados.spell.Spell;
import arkhados.spell.SpellCastListener;
import arkhados.spell.SpellCastValidator;
import arkhados.spell.buffs.CastSpeedBuff;
import arkhados.util.UserData;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.util.IntMap;
import java.util.ArrayList;
import java.util.List;

public class CSpellCast extends AbstractControl {

    private static final Vector3f DOWN = Vector3f.UNIT_Y.negate();

    private final Vector3f _tempVec = new Vector3f();

    private static IntMap<Float> clientCooldowns;
    private final IntMap<Spell> spells = new IntMap<>();
    private IntMap<Float> cooldowns = new IntMap<>();
    private final IntMap<Spell> keySpellMappings = new IntMap<>();
    private final Map<String, Integer> secondaryInputMapping = new HashMap<>(2);
    private static final float GLOBAL_COOLDOWN = 0.2f;
    private boolean casting = false;
    private final List<SpellCastValidator> castValidators = new ArrayList<>();
    private final List<SpellCastListener> castListeners = new ArrayList<>();
    private float castSpeedFactor = 1f;

    public void thisIsOwnedByClient() {
        clientCooldowns = cooldowns;
    }

    public void restoreClientCooldowns() {
        cooldowns = clientCooldowns;
    }

    public void putSpell(Spell spell, Integer key) {
        spells.put(spell.getId(), spell);
        cooldowns.put(spell.getId(), 0f);
        if (key != null) {
            keySpellMappings.put(key, spell);
        }
    }

    /**
     * Add validator that checks whether it is valid to cast certain spell.
     *
     * @param castValidator Validator that checks casting conditions
     */
    public void addCastValidator(SpellCastValidator castValidator) {
        castValidators.add(castValidator);
    }

    /**
     * Add listener that is notified anytime that spell is cast
     *
     * @param listener
     */
    public void addCastListeners(SpellCastListener listener) {
        castListeners.add(listener);
    }

    public Spell getSpell(int id) {
        return spells.get(id);
    }

    /**
     * Interrupt spell casting so that spell's cooldown is not resetted.
     */
    // FIXME: safeInterrupt isn't safe anymore
    public void safeInterrupt() {
        EntityAction action
                = spatial.getControl(CActionQueue.class).getCurrent();

        if (action == null) {
            return;
        }

        if (action instanceof ACastingSpell) {
            casting = false;
            Spell spell = ((ACastingSpell) action).getSpell();
            spatial.getControl(CActionQueue.class).clear();
            setCooldown(spell.getId(), 0f);
        } else if (action instanceof AChannelingSpell) {
            AChannelingSpell channeling = (AChannelingSpell) action;
            putOnCooldown(channeling.getSpell());
            spatial.getControl(CActionQueue.class).clear();
        } else if (action instanceof ATrance) { // TODO: Refactor
            spatial.getControl(CActionQueue.class).clear();
        }
    }

    public void castIfDifferentSpell(int input, Vector3f targetLocation) {
        if (!enabled) {
            return;
        }

        Spell spell = keySpellMappings.get(input);

        if (!validateCast(spell)) {
            return;
        }

        EntityAction action
                = spatial.getControl(CActionQueue.class).getCurrent();
        if (action != null && ((action instanceof ACastingSpell)
                || (action instanceof AChannelingSpell))) {

            Spell currentSpell;
            if (action instanceof ACastingSpell) {
                currentSpell = ((ACastingSpell) action).getSpell();
            } else {
                currentSpell = ((AChannelingSpell) action).getSpell();
            }

            int currentSpellId = currentSpell.getId();
            // Let's not interrupt spell if you are already casting same spell
            if (spell.getId() == currentSpellId) {
                return;
            }

            spatial.getControl(CActionQueue.class).clear();
            if (action instanceof ACastingSpell) {
                setCooldown(currentSpellId, 0f);
            }
            action = null;
        }

        if (action == null) {
            cast(input, targetLocation);
        }
    }

    private boolean basicValidation(final Spell spell) {
        if (spell == null || cooldowns.get(spell.getId()) > 0f) {
            return false;
        }

        if (getSpatial().getControl(CEntityVariable.class)
                .getSender().isServer()) {
            if (!spatial.getControl(CInfluenceInterface.class)
                    .canCast()) {
                return false;
            }
        }
        return true;
    }

    private float calculateCastSpeedFactor() {
        List<CastSpeedBuff> buffs = spatial
                .getControl(CInfluenceInterface.class).getCastSpeedBuffs();
        float newFactor = 1f;
        for (CastSpeedBuff buff : buffs) {
            newFactor *= buff.getFactor();
        }

        return newFactor;
    }

    private boolean validateCast(final Spell spell) {
        if (!basicValidation(spell)) {
            return false;
        }
        for (SpellCastValidator spellCastValidator : castValidators) {
            if (!spellCastValidator.validateSpellCast(this, spell)) {
                return false;
            }
        }
        return true;
    }

    public void cast(int input, Vector3f targetLocation) {
        Spell spell = keySpellMappings.get(input);

        PlayerEntityAwareness awareness = getSpatial()
                .getControl(CEntityVariable.class).getAwareness();
        if (awareness != null) {

            CCharacterPhysics physics
                    = spatial.getControl(CCharacterPhysics.class);
            physics.setViewDirection(physics.calculateTargetDirection());
            spatial.getControl(CCharacterAnimation.class)
                    .castSpell(spell, castSpeedFactor);
            spatial.getControl(CActionQueue.class)
                    .enqueueAction(new ACastingSpell(spell,
                                    spell.isMultipart()));

            EntityAction castingAction
                    = spell.buildCastAction((Node) spatial, targetLocation);
            spatial.getControl(CActionQueue.class)
                    .enqueueAction(castingAction);
            Vector3f direction = targetLocation
                    .subtract(spatial.getLocalTranslation());
            awareness.getFog().addCommand(spatial,
                    new CmdStartCastingSpell(
                            (int) spatial.getUserData(UserData.ENTITY_ID),
                            spell.getId(), direction, castSpeedFactor));
            getSpatial().getControl(CResting.class).stopRegen();
        }

        globalCooldown();
        putOnCooldown(spell);

        // Spell might have primary and secondary
        Spell otherSpell = keySpellMappings.get(-input);
        if (otherSpell != null) {
            putOnCooldown(otherSpell);
        }

        for (SpellCastListener spellCastListener : castListeners) {
            spellCastListener.spellCasted(this, spell);
        }
    }

    public void putOnCooldown(int spellId) {
        final Spell spell = Spell.getSpell(spellId);
        putOnCooldown(spell);
    }

    public void setCooldown(int spellId, float cooldown) {
        cooldowns.put(spellId, cooldown);
        PlayerEntityAwareness awareness = getSpatial()
                .getControl(CEntityVariable.class).getAwareness();

        if (awareness != null) {
            int entityId = spatial.getUserData(UserData.ENTITY_ID);
            // TODO: Consider NOT sending this message to all players
            awareness.getFog().addCommand(spatial,
                    new CmdSetCooldown(entityId, spellId, cooldown, true));
        }
    }

    public void putOnCooldown(Spell spell) {
        cooldowns.put(spell.getId(), spell.getCooldown());

        PlayerEntityAwareness awareness = getSpatial()
                .getControl(CEntityVariable.class).getAwareness();

        if (awareness != null) {
            int entityId = spatial.getUserData(UserData.ENTITY_ID);
            awareness.getFog().addCommand(spatial,
                    new CmdSetCooldown(entityId, spell.getId(),
                            spell.getCooldown(), true));
        }
    }

    public boolean isOnCooldown(String spellName) {
        float cooldown = cooldowns.get(Spell.getSpell(spellName).getId());
        return cooldown > 0f;
    }

    public Vector3f getClosestPointToTarget(Spell spell) {
        Vector3f targetLocation = spatial
                .getControl(CCharacterPhysics.class).getTargetLocation();

        float distance = targetLocation.distance(spatial.getLocalTranslation());
        float interpolationFactor = spell.getRange() / distance;
        if (interpolationFactor > 1f) {
            interpolationFactor = 1f;
        }

        Vector3f target = spatial.getLocalTranslation().clone()
                .interpolateLocal(targetLocation, interpolationFactor);

        return target;
    }

    @Override
    protected void controlUpdate(float tpf) {
        for (IntMap.Entry<Float> entry : cooldowns) {
            cooldowns.put(entry.getKey(), entry.getValue() - tpf);
        }

        castSpeedFactor = calculateCastSpeedFactor();
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public void globalCooldown() {
        for (IntMap.Entry<Float> entry : cooldowns) {
            int spell = entry.getKey();

            if (cooldowns.get(spell) < GLOBAL_COOLDOWN) {
                cooldowns.put(spell, GLOBAL_COOLDOWN);
            }
        }
    }

    public Integer getInput(String key) {
        return secondaryInputMapping.get(key);
    }

    public void putSecondaryMapping(String key, int spellInputId) {
        secondaryInputMapping.put(key, spellInputId);
    }

    public Spell getKeySpellNameMapping(int key) {
        return keySpellMappings.get(key);
    }

    public float getCooldown(int spellId) {
        return cooldowns.get(spellId);
    }

    public boolean isCasting() {
        return casting;
    }

    public boolean isChanneling() {
        EntityAction action
                = spatial.getControl(CActionQueue.class).getCurrent();
        return action instanceof AChannelingSpell;
    }

    public void setCasting(boolean casting) {
        this.casting = casting;
    }

    public IntMap<Float> getCooldowns() {
        return cooldowns;
    }

    public void setCooldowns(IntMap<Float> cooldowns) {
        this.cooldowns = cooldowns;
    }

    public float getCastSpeedFactor() {
        return castSpeedFactor;
    }
}
