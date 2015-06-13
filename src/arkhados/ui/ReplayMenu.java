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
package arkhados.ui;

import arkhados.ClientMain;
import arkhados.Globals;
import arkhados.replay.ReplayInputHandler;
import arkhados.replay.ReplayReader;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.menu.PopupMenuControl;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReplayMenu implements ScreenController {

    private Nifty nifty;
    private Screen screen;
    private Element popup;

    @Override
    public void bind(Nifty nifty, Screen screen) {
        this.nifty = nifty;
        this.screen = screen;
        popup = nifty.createPopup("select_player");
    }

    public void selectReplay() {
        ListBox replayBox =
                screen.findNiftyControl("replay_list", ListBox.class);
        List selection = replayBox.getSelection();
        if (selection.isEmpty()) {
            return;
        }

        String replay = selection.get(0).toString();

        Path path = Paths.get("replays", replay);
        try {
            ReplayReader replayReader = Globals.app.getStateManager()
                    .getState(ReplayReader.class);
            replayReader.loadReplay(path.toString());
            Map<Integer, String> playerMap =
                    replayReader.getData().getHeader().getPlayers();
            List<Integer> playersList = new ArrayList<>(playerMap.keySet());
            Collections.sort(playersList);
            ListBox playerBox =
                    popup.findNiftyControl("player_list", ListBox.class);
            playerBox.clear();

            for (int playerId : playersList) {
                String playerName = playerMap.get(playerId);
                ReplayPlayerLBModel model =
                        new ReplayPlayerLBModel(playerId, playerName);
                playerBox.addItem(model);
            }

            nifty.showPopup(screen, popup.getId(), null);

        } catch (IOException ex) {
            Logger.getLogger(ReplayMenu.class.getName())
                    .log(Level.WARNING, null, ex);
        }
    }

    public void selectPlayer() {
        ListBox box = popup.findNiftyControl("player_list", ListBox.class);
        List selection = box.getSelection();

        ReplayPlayerLBModel model = (ReplayPlayerLBModel) selection.get(0);

        final ReplayReader replayReader = Globals.app.getStateManager()
                .getState(ReplayReader.class);
        replayReader.selectPlayer(model.getPlayerId());
        nifty.gotoScreen("default_hud");
        new java.util.Timer().schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                replayReader.setEnabled(true);
                Globals.app.getStateManager().getState(ReplayInputHandler.class)
                        .setEnabled(true);
            }
        }, 500);

    }

    @Override
    public void onStartScreen() {
        Globals.replayMode = true;
        ((ClientMain) Globals.app).prepareForReplay();
        findReplays();
    }

    @Override
    public void onEndScreen() {
        popup.findNiftyControl("player_list", ListBox.class).clear();
        if (screen.findElementByName(popup.getId()) != null) {
            nifty.closePopup(popup.getId());
        }

        ListBox replayBox =
                screen.findNiftyControl("replay_list", ListBox.class);
        replayBox.clear();
    }
    private FilenameFilter filenameFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".rep");
        }
    };

    private void findReplays() {
        ListBox box = screen.findNiftyControl("replay_list", ListBox.class);
        box.clear();
        File folder = new File("replays");
        File[] fileList = folder.listFiles(filenameFilter);
        Arrays.sort(fileList, Collections.reverseOrder());
        for (File file : fileList) {
            box.addItem(file.getName());
        }
    }

    public void gotoMenu(final String menu) {
        Globals.app.enqueue(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                nifty.gotoScreen(menu);
                return null;
            }
        });
    }
}
