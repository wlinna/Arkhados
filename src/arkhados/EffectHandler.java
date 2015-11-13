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
package arkhados;

import arkhados.effects.EffectBox;
import arkhados.effects.WorldEffect;
import arkhados.messages.CmdWorldEffect;
import arkhados.messages.sync.CmdAction;
import arkhados.net.Command;
import arkhados.net.CommandHandler;
import arkhados.util.UserData;
import com.jme3.app.Application;
import com.jme3.scene.Spatial;
import com.jme3.util.IntMap;
import java.util.ArrayList;
import java.util.List;

public class EffectHandler implements CommandHandler {

    private final Application app;
    private World world;
    private final IntMap<EffectBox> actionEffects = new IntMap<>();
    private static int runningIndex = 0;
    private static final List<WorldEffect> worldEffects = new ArrayList<>();

    public static int addWorldEffect(WorldEffect effect) {
        worldEffects.add(effect);
        return runningIndex++;
    }

    public static void clearWorldEffects() {
        runningIndex = 0;
        worldEffects.clear();
    }

    public EffectHandler(Application app) {
        this.app = app;
    }

    public void addEffectBox(int id, EffectBox effectBox) {
        actionEffects.put(id, effectBox);
    }

    private void handleAction(final CmdAction actionCommand) {
        final Spatial entity = world.getEntity(actionCommand.getSyncId());
        if (entity == null) {
            return;
        }

        int nodeBuilderId = entity.getUserData(UserData.NODE_BUILDER_ID);

        final EffectBox box = actionEffects.get(nodeBuilderId);
        if (box == null) {
            return;
        }

        app.enqueue(() -> {
            box.executeActionEffect(actionCommand.getActionId(),
                    world.getWorldRoot(),
                    entity.getLocalTranslation());
            return null;
        });
    }

    private void handleWorldEffect(final CmdWorldEffect command) {
        if (command.getEffectId() >= worldEffects.size()) {
            return;
        }

        app.enqueue(() -> {
            WorldEffect worldEffect =
                    worldEffects.get(command.getEffectId());
            worldEffect.execute(world.getWorldRoot(),
                    command.getLocation(), null);
            return null;
        });

    }

    public void setWorld(World world) {
        this.world = world;
    }

    @Override
    public void readGuaranteed(Object source, Command guaranteed) {
        if (guaranteed instanceof CmdAction) {
            handleAction((CmdAction) guaranteed);
        } else if (guaranteed instanceof CmdWorldEffect) {
            handleWorldEffect((CmdWorldEffect) guaranteed);
        }
    }

    @Override
    public void readUnreliable(Object source, Command unreliable) {
    }
}