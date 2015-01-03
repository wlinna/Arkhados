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
import arkhados.messages.WorldEffectCommand;
import arkhados.messages.syncmessages.ActionCommand;
import arkhados.net.Command;
import arkhados.net.CommandHandler;
import arkhados.util.UserDataStrings;
import com.jme3.app.Application;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

/**
 *
 * @author william
 */
public class EffectHandler implements CommandHandler {

    private Application app;
    private WorldManager worldManager;
    private HashMap<Integer, EffectBox> actionEffects = new HashMap<>();
    private static int runningIndex = 0;
    private static List<WorldEffect> worldEffects = new ArrayList<>();

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

    private void handleAction(final ActionCommand actionCommand) {
        final Spatial entity =
                worldManager.getEntity(actionCommand.getSyncId());
        if (entity == null) {
            return;
        }

        int nodeBuilderId = entity.getUserData(UserDataStrings.NODE_BUILDER_ID);

        final EffectBox box = actionEffects.get(nodeBuilderId);
        if (box == null) {
            return;
        }

        app.enqueue(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                box.executeActionEffect(actionCommand.getActionId(),
                        worldManager.getWorldRoot(),
                        entity.getLocalTranslation());
                return null;
            }
        });
    }

    private void handleWorldEffect(final WorldEffectCommand command) {
        if (command.getEffectId() >= worldEffects.size()) {
            return;
        }

        app.enqueue(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                WorldEffect worldEffect =
                        worldEffects.get(command.getEffectId());
                worldEffect.execute(worldManager.getWorldRoot(),
                        command.getLocation(), null);
                return null;
            }
        });

    }

    public void setWorldManager(WorldManager worldManager) {
        this.worldManager = worldManager;
    }

    @Override
    public void readGuaranteed(Object source, Command guaranteed) {
        if (guaranteed instanceof ActionCommand) {
            handleAction((ActionCommand) guaranteed);
        } else if (guaranteed instanceof WorldEffectCommand) {
            handleWorldEffect((WorldEffectCommand) guaranteed);
        }
    }

    @Override
    public void readUnreliable(Object source, Command unreliable) {
    }
}