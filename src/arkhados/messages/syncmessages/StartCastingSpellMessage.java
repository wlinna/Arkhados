

package arkhados.messages.syncmessages;

import com.jme3.math.Vector3f;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import com.jme3.scene.Spatial;
import arkhados.controls.CharacterAnimationControl;
import arkhados.controls.CharacterPhysicsControl;
import arkhados.controls.SpellCastControl;
import arkhados.spell.Spell;

/**
 *
 * @author william
 */
@Serializable
public class StartCastingSpellMessage extends AbstractSyncMessage {
    private String spellName;
    private Vector3f direction = new Vector3f();

    public StartCastingSpellMessage() {

    }

    public StartCastingSpellMessage(long id, String spellName, Vector3f direction) {
        super(id);
        this.spellName = spellName;
        this.direction.set(direction);
    }

    @Override
    public void applyData(Object target) {
        Spatial character = (Spatial) target;
        Spell spell = character.getControl(SpellCastControl.class).getSpell(this.spellName);
        character.getControl(CharacterAnimationControl.class).castSpell(spell);
        character.getControl(CharacterPhysicsControl.class).setViewDirection(this.direction);
    }
}
