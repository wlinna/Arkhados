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
import arkhados.controls.CProjectile;
import arkhados.spell.spells.shadowmancer.ShadowOrb;
import arkhados.spell.spells.elitesoldier.BlindingRay;
import arkhados.spell.spells.elitesoldier.LikeAPro;
import arkhados.spell.spells.elitesoldier.PlasmaGrenades;
import arkhados.spell.spells.elitesoldier.Plasmagun;
import arkhados.spell.spells.elitesoldier.Railgun;
import arkhados.spell.spells.elitesoldier.RocketJump;
import arkhados.spell.spells.elitesoldier.RocketLauncher;
import arkhados.spell.spells.elitesoldier.Shotgun;
import arkhados.spell.spells.embermage.EmberCircle;
import arkhados.spell.spells.embermage.EtherealFlame;
import arkhados.spell.spells.embermage.Fireball;
import arkhados.spell.spells.embermage.Firewalk;
import arkhados.spell.spells.embermage.Ignite;
import arkhados.spell.spells.embermage.MagmaBash;
import arkhados.spell.spells.embermage.MagmaRelease;
import arkhados.spell.spells.embermage.Meteor;
import arkhados.spell.spells.embermage.PurifyingFlame;
import arkhados.spell.spells.rockgolem.AngrySpiritStone;
import arkhados.spell.spells.rockgolem.Bedrock;
import arkhados.spell.spells.rockgolem.EarthQuake;
import arkhados.spell.spells.rockgolem.MineralArmor;
import arkhados.spell.spells.rockgolem.SealingBoulder;
import arkhados.spell.spells.rockgolem.SpiritStone;
import arkhados.spell.spells.rockgolem.StoneFist;
import arkhados.spell.spells.rockgolem.Toss;
import arkhados.spell.spells.shadowmancer.DarkEnergy;
import arkhados.spell.spells.shadowmancer.DarkSpear;
import arkhados.spell.spells.venator.BloodFrenzy;
import arkhados.spell.spells.venator.Dagger;
import arkhados.spell.spells.venator.DeepWounds;
import arkhados.spell.spells.venator.FeralScream;
import arkhados.spell.spells.venator.Leap;
import arkhados.spell.spells.venator.Rend;
import arkhados.spell.spells.venator.NumbingDagger;
import arkhados.spell.spells.venator.SurvivalInstinct;
import arkhados.util.EntityFactory;
import arkhados.util.AbstractNodeBuilder;
import arkhados.util.BuildParameters;
import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.util.HashMap;

/**
 *
 * @author william
 */
/**
 * Spell contains data of spell's base data. Each Spell is created only once
 * and their data does not change.
 */
public abstract class Spell {

    protected static AssetManager assetManager = null;
    protected static WorldManager worldManager = null;
    /**
     * Spells has all spells mapped by their name so that spell data can be
     * retrieved from anywhere
     */
    private static HashMap<Integer, Spell> Spells = new HashMap<>();
    private static HashMap<String, Integer> SpellNameCreationIdMap =
            new HashMap<>();

    /**
     * Creates each spell and saves them to Spells-map. Should be called only
     * once
     *
     * @param assetManager will be saved to static variable assetManager
     * @param worldManager will be save to static variable worldManager
     */
    public static void initSpells(EntityFactory entityFactory,
            AssetManager assetManager, WorldManager worldManager) {
        Spell.assetManager = assetManager;
        Spell.worldManager = worldManager;

        AbstractNodeBuilder.setAssetManager(assetManager);
        AbstractNodeBuilder.setWorldManager(worldManager);

        CProjectile.setWorldManager(worldManager);

        // *************** INIT spells here ************************

        addSpell(entityFactory, Fireball.create());
        addSpell(entityFactory, MagmaBash.create());
        addSpell(entityFactory, MagmaRelease.create());
        addSpell(entityFactory, EmberCircle.create());
        addSpell(entityFactory, Meteor.create());
        addSpell(entityFactory, PurifyingFlame.create());
        addSpell(entityFactory, EtherealFlame.create());
        addSpell(entityFactory, Firewalk.create());
        addSpell(entityFactory, Ignite.create());

        addSpell(entityFactory, Rend.create());
        addSpell(entityFactory, Dagger.create());
        addSpell(entityFactory, NumbingDagger.create());
        addSpell(entityFactory, Leap.create());
        addSpell(entityFactory, FeralScream.create());
        addSpell(entityFactory, DeepWounds.create());
        addSpell(entityFactory, SurvivalInstinct.create());
        addSpell(entityFactory, BloodFrenzy.create());

        addSpell(entityFactory, Shotgun.create());              
        addSpell(entityFactory, Railgun.create());
        addSpell(entityFactory, BlindingRay.create());
        addSpell(entityFactory, Plasmagun.create());
        addSpell(entityFactory, PlasmaGrenades.create());
        addSpell(entityFactory, RocketLauncher.create());
        addSpell(entityFactory, LikeAPro.create());
        addSpell(entityFactory, RocketJump.create());

        addSpell(entityFactory, StoneFist.create());
        addSpell(entityFactory, SealingBoulder.create());
        addSpell(entityFactory, SpiritStone.create());
        addSpell(entityFactory, AngrySpiritStone.create());
        addSpell(entityFactory, Toss.create());
        addSpell(entityFactory, MineralArmor.create());
        addSpell(entityFactory, Bedrock.create());
        addSpell(entityFactory, EarthQuake.create());
        
        addSpell(entityFactory, ShadowOrb.create());
        addSpell(entityFactory, DarkEnergy.create());
        addSpell(entityFactory, DarkSpear.create());
    }

    private static void addSpell(EntityFactory entityFactory, Spell spell) {
        int nodeBuilderId = entityFactory.addNodeBuilder(spell.nodeBuilder);
        spell.setId(nodeBuilderId);
        Spells.put(nodeBuilderId, spell);
        SpellNameCreationIdMap.put(spell.getName(), nodeBuilderId);
    }

    public static Spell getSpell(int creationId) {
        return Spells.get(creationId);
    }

    public static Spell getSpell(String spellName) {
        Integer creationId = SpellNameCreationIdMap.get(spellName);

        if (creationId == null) {
            return null;
        }

        return Spells.get(creationId);
    }
    private final String name;
    private int id;
    private final float cooldown;
    private final float range;
    private final float castTime;
    private boolean canMoveWhileCasting = false;
    private boolean moveTowardsTarget = true;
    // HACK: This is used by SpellCastControl to determine if character should restore walking after casting action
    protected boolean multipart = false;
    protected String iconName = null;
    protected CastSpellActionBuilder castSpellActionBuilder;
    protected AbstractNodeBuilder nodeBuilder;

    /**
     * Creates spell with given parameters
     *
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

    public String getIconName() {
        return iconName;
    }

    /**
     * Constructs new EntityAction that will cast the spell.
     *
     * @param vec Initial direction or target location vector, depending on spell. Often not
     * necessary.
     * @return EntityAction that will cast the spell
     */
    public EntityAction buildCastAction(Node caster, Vector3f vec) {
        return castSpellActionBuilder.newAction(caster, vec);
    }

    public Node buildNode(BuildParameters params) {
        return nodeBuilder.build(params);
    }

    public boolean canMoveWhileCasting() {
        return canMoveWhileCasting;
    }

    public void setCanMoveWhileCasting(boolean canMoveWhileCasting) {
        this.canMoveWhileCasting = canMoveWhileCasting;
    }

    public boolean moveTowardsTarget() {
        return moveTowardsTarget;
    }

    public void setMoveTowardsTarget(boolean mustMoveTowardsViewDirection) {
        this.moveTowardsTarget = mustMoveTowardsViewDirection;
    }

    public boolean isMultipart() {
        return multipart;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}