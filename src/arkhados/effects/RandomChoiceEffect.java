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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomChoiceEffect implements WorldEffect {

    private final List<WorldEffect> choices = new ArrayList<>();
    private final Random random = new Random();

    public void add(WorldEffect alternative) {
        choices.add(alternative);
    }

    @Override
    public EffectHandle execute(Node root, Vector3f location,
            String parameter) {
        int i = random.nextInt(choices.size());
        choices.get(i).execute(root, location, parameter);
        return null;
    }
}