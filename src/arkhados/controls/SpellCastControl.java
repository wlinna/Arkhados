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
import arkhados.actions.EntityAction;
import arkhados.messages.syncmessages.StartCastingSpellMessage;
import arkhados.spell.Spell;
import arkhados.util.UserDataStrings;

/**
 *
 * @author william
 */
public class SpellCastControl extends AbstractControl {

    private WorldManager worldManager;
    private HashMap<String, Spell> spells = new HashMap<String, Spell>();
    private HashMap<String, Float> cooldowns = new HashMap<String, Float>();
    private HashMap<String, Spell> keySpellMappings = new HashMap<String, Spell>();
    private static final float GLOBAL_COOLDOWN = 0.2f;

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
        this.keySpellMappings.put(key, spell);
    }

    public Spell getSpell(final String name) {
        return this.spells.get(name);
    }

//    public boolean isCasting() {
//        if (activeCastTimeLeft <= 0f) {
//            return true;
//        }
//        return false;
//    }
    public void safeInterrupt() {
        EntityAction action = super.spatial.getControl(ActionQueueControl.class).getCurrent();
        if (action != null && action instanceof CastingSpellAction) {

            final String spellName = ((CastingSpellAction) action).getSpellName();
            super.spatial.getControl(ActionQueueControl.class).clear();
            this.cooldowns.put(spellName, 0f);
        }
    }

    public void castIfDifferentSpell(final String input, Vector3f targetLocation) {
        Spell spell = this.keySpellMappings.get(input);

        EntityAction action = super.spatial.getControl(ActionQueueControl.class).getCurrent();
        if (action != null && action instanceof CastingSpellAction) {

            final String spellName = ((CastingSpellAction) action).getSpellName();
            // Let's not interrupt spell if you are already casting same spell
            if (spell.getName().equals(spellName)) {
                return;
            }
            super.spatial.getControl(ActionQueueControl.class).clear();
            this.cooldowns.put(spellName, 0f);
        }

        this.cast(input, targetLocation);
    }

    public void cast(final String input, Vector3f targetLocation) {
        if (!this.enabled) {
            return;
        }

        Spell spell = this.keySpellMappings.get(input);
        if (spell != null && this.cooldowns.get(spell.getName()) > 0.0f) {
            return;
        }

        this.globalCooldown();

        if (this.worldManager.isServer()) {
            if (!super.spatial.getControl(InfluenceInterfaceControl.class).canCast()) {
                return;
            }

            super.spatial.getControl(CharacterPhysicsControl.class).setWalkDirection(Vector3f.ZERO);
            super.spatial.getControl(CharacterAnimationControl.class).castSpell(spell);
            super.spatial.getControl(ActionQueueControl.class).enqueueAction(new CastingSpellAction(spell.getName(), spell.getCastTime()));
//            this.activeCastTimeLeft = spell.getCastTime();
            EntityAction castingAction = spell.buildCastAction(targetLocation);
            super.spatial.getControl(ActionQueueControl.class).enqueueAction(castingAction);
            Vector3f direction = targetLocation.subtract(super.spatial.getLocalTranslation());
            this.worldManager.getSyncManager().getServer().broadcast(
                    new StartCastingSpellMessage((Long) super.spatial.getUserData(UserDataStrings.ENTITY_ID), spell.getName(), direction));
        }
        this.cooldowns.put(spell.getName(), spell.getCooldown());
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

    @Override
    protected void controlUpdate(float tpf) {
        for (Map.Entry<String, Float> entry : this.cooldowns.entrySet()) {
            entry.setValue(entry.getValue() - tpf);
        }
//
//        this.activeCastTimeLeft -= tpf;
//        if (super.spatial.getControl(ActionQueueControl.class).getCurrent() == null) {
//            this.activeCastTimeLeft = 0f;
//        }
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

    private void globalCooldown() {
        for (String spell : this.cooldowns.keySet()) {
            if (this.cooldowns.get(spell) < GLOBAL_COOLDOWN) {
                this.cooldowns.put(spell, GLOBAL_COOLDOWN);
            }
        }
    }
}
