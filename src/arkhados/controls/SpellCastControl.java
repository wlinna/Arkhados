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

import arkhados.ServerFogManager;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import arkhados.WorldManager;
import arkhados.actions.CastingSpellAction;
import arkhados.actions.ChannelingSpellAction;
import arkhados.actions.EntityAction;
import arkhados.messages.syncmessages.SetCooldownCommand;
import arkhados.messages.syncmessages.StartCastingSpellCommand;
import arkhados.net.Sender;
import arkhados.spell.Spell;
import arkhados.spell.SpellCastListener;
import arkhados.spell.SpellCastValidator;
import arkhados.util.UserDataStrings;
import com.jme3.scene.Node;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author william
 */
public class SpellCastControl extends AbstractControl {

    private HashMap<Integer, Spell> spells = new HashMap<>();
    private HashMap<Integer, Float> cooldowns = new HashMap<>();
    private HashMap<Integer, Spell> keySpellMappings = new HashMap<>();
    private static final float GLOBAL_COOLDOWN = 0.2f;
    private boolean casting = false;
    private final List<SpellCastValidator> castValidators = new ArrayList<>();
    private final List<SpellCastListener> castListeners = new ArrayList<>();

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
     * @param ammunitionControl
     */
    public void addCastListeners(EliteSoldierAmmunitionControl ammunitionControl) {
        castListeners.add(ammunitionControl);
    }

    public Spell getSpell(int id) {
        return spells.get(id);
    }

    /**
     * Interrupt spell casting so that spell's cooldown is not resetted.
     */
    public void safeInterrupt() {
        EntityAction action = spatial.getControl(ActionQueueControl.class).getCurrent();
        if (action != null && action instanceof CastingSpellAction) {
            casting = false;
            final Spell spell = ((CastingSpellAction) action).getSpell();
            spatial.getControl(ActionQueueControl.class).clear();
            setCooldown(spell.getId(), 0f);
        } else if (action != null && action instanceof ChannelingSpellAction) {
            ChannelingSpellAction channeling = (ChannelingSpellAction) action;
            putOnCooldown(channeling.getSpell());
            spatial.getControl(ActionQueueControl.class).clear();
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

        EntityAction action = spatial.getControl(ActionQueueControl.class).getCurrent();
        if (action != null && ((action instanceof CastingSpellAction)
                || (action instanceof ChannelingSpellAction))) {

            Spell currentSpell;
            if (action instanceof CastingSpellAction) {
                currentSpell = ((CastingSpellAction) action).getSpell();
            } else {
                currentSpell = ((ChannelingSpellAction) action).getSpell();
            }

            int currentSpellId = currentSpell.getId();
            // Let's not interrupt spell if you are already casting same spell
            if (spell.getId() == currentSpellId) {
                return;
            }

            spatial.getControl(ActionQueueControl.class).clear();
            if (action instanceof CastingSpellAction) {
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

        if (getSpatial().getControl(EntityVariableControl.class).getSender().isServer()) {
            if (!spatial.getControl(InfluenceInterfaceControl.class).canCast()) {
                return false;
            }
        }
        return true;
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

        ServerEntityAwarenessControl awareness = getSpatial().getControl(ServerEntityAwarenessControl.class);
        if (awareness != null) {

            final CharacterPhysicsControl physics = spatial.getControl(CharacterPhysicsControl.class);
            physics.setViewDirection(physics.calculateTargetDirection());
            spatial.getControl(CharacterAnimationControl.class).castSpell(spell);
            spatial.getControl(ActionQueueControl.class).enqueueAction(new CastingSpellAction(spell, spell.isMultipart()));
//            activeCastTimeLeft = spell.getCastTime();
            final EntityAction castingAction = spell.buildCastAction((Node) spatial, targetLocation);
            spatial.getControl(ActionQueueControl.class).enqueueAction(castingAction);
            Vector3f direction = targetLocation.subtract(spatial.getLocalTranslation());
            awareness.getFogManager().addCommand(
                    new StartCastingSpellCommand((Integer) spatial.getUserData(UserDataStrings.ENTITY_ID),
                    spell.getId(), direction));
        }
        globalCooldown();
        putOnCooldown(spell);

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
        ServerEntityAwarenessControl awareness =
                getSpatial().getControl(ServerEntityAwarenessControl.class);

        if (awareness != null) {
            final Integer entityId = spatial.getUserData(UserDataStrings.ENTITY_ID);
            // TODO: Consider NOT sending this message to all players
            awareness.getFogManager().addCommand(new SetCooldownCommand(entityId, spellId, cooldown, true));
        }
    }

    public void putOnCooldown(Spell spell) {
        cooldowns.put(spell.getId(), spell.getCooldown());

        ServerEntityAwarenessControl awareness =
                getSpatial().getControl(ServerEntityAwarenessControl.class);

        if (awareness != null) {
            Integer entityId = spatial.getUserData(UserDataStrings.ENTITY_ID);
            awareness.getFogManager().addCommand(new SetCooldownCommand(entityId,
                    spell.getId(), spell.getCooldown(), true));
        }
    }

    public boolean isOnCooldown(String spellName) {
        final Float cooldown = cooldowns.get(Spell.getSpell(spellName).getId());
        return cooldown > 0f;
    }

    // Not removing this, because it may be useful for AI controlled units and for visual cues
    private Vector3f findClosestCastingLocation(final Vector3f targetLocation, float range) {
        Vector3f displacement = getSpatial().getLocalTranslation().subtract(targetLocation);
        if (displacement.lengthSquared() <= FastMath.sqr(range)) {
            return getSpatial().getLocalTranslation();
        }
        displacement.normalizeLocal().multLocal(range);
        return displacement.addLocal(targetLocation);
    }

    public Vector3f getClosestPointToTarget(Spell spell) {
        final Vector3f targetLocation = spatial.getControl(CharacterPhysicsControl.class).getTargetLocation();

        final float distance = targetLocation.distance(spatial.getLocalTranslation());
        float interpolationFactor = spell.getRange() / distance;
        if (interpolationFactor > 1f) {
            interpolationFactor = 1f;
        }

        final Vector3f target = spatial.getLocalTranslation().clone().interpolate(targetLocation, interpolationFactor);
        return target;
    }

    @Override
    protected void controlUpdate(float tpf) {
        for (Map.Entry<Integer, Float> entry : cooldowns.entrySet()) {
            entry.setValue(entry.getValue() - tpf);
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    public void globalCooldown() {
        for (Integer spell : cooldowns.keySet()) {
            if (cooldowns.get(spell) < GLOBAL_COOLDOWN) {
                cooldowns.put(spell, GLOBAL_COOLDOWN);
            }
        }
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
        EntityAction action = spatial.getControl(ActionQueueControl.class).getCurrent();
        return action instanceof ChannelingSpellAction;
    }

    public void setCasting(boolean casting) {
        this.casting = casting;
    }
}
