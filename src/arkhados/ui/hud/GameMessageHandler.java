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
package arkhados.ui.hud;

import arkhados.Globals;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.builder.TextBuilder;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.tools.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 *
 * @author william
 */
public class GameMessageHandler {

    private Nifty nifty;
    private Screen screen;
    private Element root;
    private List<String> messages = new ArrayList<>();
    private List<Element> rows = new ArrayList<>(10);

    public void initialize(Nifty nifty) {
        this.nifty = nifty;
        screen = nifty.getScreen("default_hud");
        root = screen.findElementByName("messages");
    }

    public void createRows(int amount) {
        TextBuilder textBuilder = new TextBuilder();

        textBuilder.font("Interface/Fonts/Default.fnt");
        textBuilder.height("20px");
        textBuilder.width("100%");
        textBuilder.textHAlignLeft();
        textBuilder.wrap(true);

        for (int i = 0; i < amount; i++) {
            Element text = textBuilder.build(nifty, screen, root);
            rows.add(text);
        }
    }

    public void addMessage(String newMessage, Color color) {
        messages.add(newMessage);

        Globals.app.enqueue(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                int index = messages.size() - 1;
                for (int i = rows.size() - 1; i >= 0;) {
                    TextRenderer text = rows.get(i)
                            .getRenderer(TextRenderer.class);

                    if (index < 0) {
                        break;
                    }

                    String message = messages.get(index--);
                    text.setText(message);
                    String[] split = text.getWrappedText().split("\n");

                    for (int l = split.length - 1; l >= 0 && i >= 0; --l) {
                        rows.get(i--).getRenderer(TextRenderer.class)
                                .setText(split[l]);
                    }
                }
                return null;
            }
        });
    }
}
