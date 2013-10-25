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
package arkhados.spell;

import arkhados.WorldManager;
import arkhados.actions.EntityAction;
import arkhados.controls.ProjectileControl;
import arkhados.spell.spells.embermage.EmberCircle;
import arkhados.spell.spells.embermage.Fireball;
import arkhados.spell.spells.embermage.Firewalk;
import arkhados.spell.spells.embermage.MagmaBash;
import arkhados.spell.spells.embermage.PurifyingFlame;
import arkhados.spell.spells.venator.Dagger;
import arkhados.spell.spells.venator.DeepWounds;
import arkhados.spell.spells.venator.FeralScream;
import arkhados.spell.spells.venator.Leap;
import arkhados.spell.spells.venator.Rend;
import arkhados.spell.spells.venator.SurvivalInstinct;
import arkhados.util.NodeBuilder;
import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.util.HashMap;

/**
 *
 * @author william
 */

/**
 * Spell contains data of spell's base data. Each Spell is created only once and
 * their data does not change.
 */
public abstract class Spell {

    protected static AssetManager assetManager = null;
    protected static WorldManager worldManager = null;

    /**
     * Spells has all spells mapped by their name so that spell data can be
     * retrieved from anywhere
     */
    private static HashMap<String, Spell> Spells = new HashMap<String, Spell>();

    /**
     * Creates each spell and saves them to Spells-map. Should be called only
     * once
     * @param assetManager will be saved to static variable assetManager
     * @param worldManager will be save to static variable worldManager
     */
    public static void initSpells(AssetManager assetManager, WorldManager worldManager) {
        Spell.assetManager = assetManager;
        Spell.worldManager = worldManager;

        NodeBuilder.setAssetManager(assetManager);
        NodeBuilder.setWorldManager(worldManager);

        ProjectileControl.setWorldManager(worldManager);

        // *************** INIT spells here ************************

        Spell fireball = Fireball.create();
        Spells.put(fireball.getName(), fireball);

        Spell magmaBash = MagmaBash.create();
        Spells.put(magmaBash.getName(), magmaBash);

        Spell emberCircle = EmberCircle.create();
        Spells.put(emberCircle.getName(), emberCircle);

        Spell purifyingFlame = PurifyingFlame.create();
        Spells.put(purifyingFlame.getName(), purifyingFlame);

        Spell firewalk = Firewalk.create();
        Spells.put(firewalk.getName(), firewalk);

        // Venator spells
        Spell rend = Rend.create();
        Spells.put(rend.getName(), rend);

        Spell dagger = Dagger.create();
        Spells.put(dagger.getName(), dagger);

        Spell leap = Leap.create();
        Spells.put(leap.getName(), leap);

        Spell feralScream = FeralScream.create();
        Spells.put(feralScream.getName(), feralScream);

        Spell deepWounds = DeepWounds.create();
        Spells.put(deepWounds.getName(), deepWounds);

        Spell survivalInstinct = SurvivalInstinct.create();
        Spells.put(survivalInstinct.getName(), survivalInstinct);
    }

    /**
     * Call this method to get all Spell-data.
     * @return Spells-map
     */
    public static HashMap<String, Spell> getSpells() {
        return Spells;
    }

    private final String name;
    private final float cooldown;
    private final float range;
    private final float castTime;
    private boolean canMoveWhileCasting = false;
    protected CastSpellActionBuilder castSpellActionBuilder;
    protected NodeBuilder nodeBuilder;

    /**
     * Creates spell with given parameters
     * @param name visible to player so give human friendly name
     * @param cooldown Time it takes to 'reload' spell so that it can be used again
     * @param range range of spell. NOTE: Currently not used in projectiles
     * @param castTime Time it takes to cast spell
     */
    protected Spell(String name, float cooldown, float range, float castTime) {
        this.name = name;
        this.cooldown = cooldown;
        this.range = range;
        this.castTime = castTime;
    }

    public String getName() {
        return this.name;
    }

    public float getCooldown() {
        return this.cooldown;
    }

    public float getRange() {
        return this.range;
    }

    public float getCastTime() {
        return this.castTime;
    }

    /**
     * Constructs new EntityAction that will cast the spell.
     * @param vec Initial direction or target location vector, depending on
     * spell. Often not necessary.
     * @return EntityAction that will cast the spell
     */
    public EntityAction buildCastAction(Node caster, Vector3f vec) {
        return this.castSpellActionBuilder.newAction(caster, vec);
    }

    public Node buildNode() {
        return this.nodeBuilder.build();
    }

    public boolean canMoveWhileCasting() {
        return this.canMoveWhileCasting;
    }

    public void setCanMoveWhileCasting(boolean canMoveWhileCasting) {
        this.canMoveWhileCasting = canMoveWhileCasting;
    }
}