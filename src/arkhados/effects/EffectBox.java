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
package arkhados.effects;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author william
 */
public class EffectBox {

    private Map<Integer, WorldEffect> actionEffects = new HashMap<>();

    public void addActionEffect(int id, WorldEffect effect) {
        
        this.actionEffects.put(id, effect);
    }

    public void executeActionEffect(int actionTypeId, Node root, Vector3f location) {
        WorldEffect effect = this.actionEffects.get(actionTypeId);
        if (effect != null) {
            effect.execute(root, location, null);
        }
    }
}
