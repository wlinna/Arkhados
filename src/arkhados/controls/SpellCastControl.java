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
import arkhados.messages.syncmessages.SetCooldownMessage;
import arkhados.messages.syncmessages.StartCastingSpellMessage;
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

    private WorldManager worldManager;
    private HashMap<String, Spell> spells = new HashMap<>();
    private HashMap<String, Float> cooldowns = new HashMap<>();
    private HashMap<String, Spell> keySpellMappings = new HashMap<>();
    private static final float GLOBAL_COOLDOWN = 0.2f;
    private boolean casting = false;
    private final List<SpellCastValidator> castValidators = new ArrayList<>();
    private final List<SpellCastListener> castListeners = new ArrayList<>();

//    private float activeCastTimeLeft = 0f;
    public SpellCastControl(WorldManager worldManager) {
        this.worldManager = worldManager;
    }

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
    }

    public void putSpell(Spell spell, String key) {
        this.spells.put(spell.getName(), spell);
        this.cooldowns.put(spell.getName(), 0f);
        if (key != null) {
            this.keySpellMappings.put(key, spell);
        }
    }

    /**
     * Add validator that checks whether it is valid to cast certain spell.
     *
     * @param castValidator Validator that checks casting conditions
     */
    public void addCastValidator(SpellCastValidator castValidator) {
        this.castValidators.add(castValidator);
    }

    /**
     * Add listener that is notified anytime that spell is cast
     *
     * @param ammunitionControl
     */
    public void addCastListeners(EliteSoldierAmmunitionControl ammunitionControl) {
        this.castListeners.add(ammunitionControl);
    }

    public Spell getSpell(final String name) {
        return this.spells.get(name);
    }

    /**
     * Interrupt spell casting so that spell's cooldown is not resetted.
     */
    public void safeInterrupt() {
        EntityAction action = super.spatial.getControl(ActionQueueControl.class).getCurrent();
        if (action != null && action instanceof CastingSpellAction) {
            this.casting = false;
            final Spell spell = ((CastingSpellAction) action).getSpell();
            super.spatial.getControl(ActionQueueControl.class).clear();
            this.setCooldown(spell.getName(), 0f);
        } else if (action != null && action instanceof ChannelingSpellAction) {
            ChannelingSpellAction channeling = (ChannelingSpellAction) action;
            this.putOnCooldown(channeling.getSpell());
            super.spatial.getControl(ActionQueueControl.class).clear();
        }
    }

    public void castIfDifferentSpell(final String input, Vector3f targetLocation) {
        if (!this.enabled) {
            return;
        }

        Spell spell = this.keySpellMappings.get(input);

        if (!this.validateCast(spell)) {
            return;
        }

        EntityAction action = super.spatial.getControl(ActionQueueControl.class).getCurrent();
        if (action != null && (action instanceof CastingSpellAction)
                || (action instanceof ChannelingSpellAction)) {

            Spell currentSpell;
            if (action instanceof CastingSpellAction) {
                currentSpell = ((CastingSpellAction) action).getSpell();
            } else {
                currentSpell = ((ChannelingSpellAction) action).getSpell();
            }

            final String spellName = currentSpell.getName();
            // Let's not interrupt spell if you are already casting same spell
            if (spell.getName().equals(spellName)) {
                return;
            }

            super.spatial.getControl(ActionQueueControl.class).clear();
            this.cooldowns.put(spellName, 0f);
        }

        if (action == null) {
            this.cast(input, targetLocation);
        }
    }

    private boolean basicValidation(final Spell spell) {
        if (spell == null || this.cooldowns.get(spell.getName()) > 0f) {
            return false;
        }

        if (this.worldManager.isServer()) {
            if (!super.spatial.getControl(InfluenceInterfaceControl.class).canCast()) {
                return false;
            }
        }
        return true;
    }

    private boolean validateCast(final Spell spell) {
        if (!this.basicValidation(spell)) {
            return false;
        }
        for (SpellCastValidator spellCastValidator : castValidators) {
            if (!spellCastValidator.validateSpellCast(this, spell)) {
                return false;
            }
        }
        return true;
    }

    public void cast(final String input, Vector3f targetLocation) {
        final Spell spell = this.keySpellMappings.get(input);

        if (this.worldManager.isServer()) {

            final CharacterPhysicsControl physics = super.spatial.getControl(CharacterPhysicsControl.class);
            physics.setViewDirection(physics.calculateTargetDirection());
            super.spatial.getControl(CharacterAnimationControl.class).castSpell(spell);
            super.spatial.getControl(ActionQueueControl.class).enqueueAction(new CastingSpellAction(spell, spell.isMultipart()));
//            this.activeCastTimeLeft = spell.getCastTime();
            final EntityAction castingAction = spell.buildCastAction((Node) super.spatial, targetLocation);
            super.spatial.getControl(ActionQueueControl.class).enqueueAction(castingAction);
            Vector3f direction = targetLocation.subtract(super.spatial.getLocalTranslation());
            this.worldManager.getSyncManager().getServer().broadcast(
                    new StartCastingSpellMessage((Long) super.spatial.getUserData(UserDataStrings.ENTITY_ID), spell.getName(), direction));
        }
        this.globalCooldown();
        this.putOnCooldown(spell);

        for (SpellCastListener spellCastListener : this.castListeners) {
            spellCastListener.spellCasted(this, spell);
        }
    }

    public void putOnCooldown(final String spellName) {
        final Spell spell = Spell.getSpells().get(spellName);
        this.putOnCooldown(spell);
    }

    public void setCooldown(final String spellName, float cooldown) {
        this.cooldowns.put(spellName, cooldown);
        if (this.worldManager.isServer()) {
            final Long entityId = super.spatial.getUserData(UserDataStrings.ENTITY_ID);
            // TODO: Consider NOT sending this message to all players
            this.worldManager.getSyncManager().broadcast(new SetCooldownMessage(entityId, spellName, 0f, true));
        }
    }

    public void putOnCooldown(final Spell spell) {
        this.cooldowns.put(spell.getName(), spell.getCooldown());

        if (this.worldManager.isServer()) {
            final Long entityId = super.spatial.getUserData(UserDataStrings.ENTITY_ID);
            // TODO: Consider NOT sending this message to all players
            this.worldManager.getSyncManager().broadcast(new SetCooldownMessage(entityId, spell.getName(), spell.getCooldown(), true));
        }
    }

    public boolean isOnCooldown(final String spellName) {
        final Float cooldown = this.cooldowns.get(spellName);
        return cooldown > 0f;
    }

    // Not removing this, because it may be useful for AI controlled units and for visual cues
    private Vector3f findClosestCastingLocation(final Vector3f targetLocation, float range) {
        Vector3f displacement = super.getSpatial().getLocalTranslation().subtract(targetLocation);
        if (displacement.lengthSquared() <= FastMath.sqr(range)) {
            return super.getSpatial().getLocalTranslation();
        }
        displacement.normalizeLocal().multLocal(range);
        return displacement.addLocal(targetLocation);
    }

    public Vector3f getClosestPointToTarget(final Spell spell) {
        final Vector3f targetLocation = super.spatial.getControl(CharacterPhysicsControl.class).getTargetLocation();

        final float distance = targetLocation.distance(super.spatial.getLocalTranslation());
        float interpolationFactor = spell.getRange() / distance;
        if (interpolationFactor > 1f) {
            interpolationFactor = 1f;
        }

        final Vector3f target = super.spatial.getLocalTranslation().clone().interpolate(targetLocation, interpolationFactor);
        return target;
    }

    @Override
    protected void controlUpdate(float tpf) {
        for (Map.Entry<String, Float> entry : this.cooldowns.entrySet()) {
            entry.setValue(entry.getValue() - tpf);
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    @Override
    public Control cloneForSpatial(Spatial spatial) {
        SpellCastControl control = new SpellCastControl(this.worldManager);
        return control;
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule in = im.getCapsule(this);
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule out = ex.getCapsule(this);
    }

    public void globalCooldown() {
        for (String spell : this.cooldowns.keySet()) {
            if (this.cooldowns.get(spell) < GLOBAL_COOLDOWN) {
                this.cooldowns.put(spell, GLOBAL_COOLDOWN);
            }
        }
    }

    public Spell getKeySpellNameMapping(final String key) {
        return this.keySpellMappings.get(key);
    }

    public float getCooldown(final String spellName) {
        return this.cooldowns.get(spellName);
    }

    public boolean isCasting() {
        return casting;
    }

    public boolean isChanneling() {
        EntityAction action = super.spatial.getControl(ActionQueueControl.class).getCurrent();
        return action instanceof ChannelingSpellAction;
    }

    public void setCasting(boolean casting) {
        this.casting = casting;
    }
}
