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

import de.lessvoid.nifty.builder.ImageBuilder;
import de.lessvoid.nifty.builder.PanelBuilder;
import de.lessvoid.nifty.builder.TextBuilder;
import de.lessvoid.nifty.tools.Color;

class SpellIconBuilder extends ImageBuilder {

    static final Color overlayColor = new Color(0f, 0f, 0f, 0.8f);

    public SpellIconBuilder(final String id, final String path) {
        super(id);
        valignCenter();
        alignCenter();
        height("64px");
        width("64px");
        marginLeft("12px");
        filename(path);
        childLayoutOverlay();
        panel(new OverlayBuilder(id));
    }
}

class SpellCounterBuilder extends TextBuilder {

    public SpellCounterBuilder(String id) {
        id(id + "-spell-counter");
        text("");
        style("nifty-label");
    }
}

class OverlayBuilder extends PanelBuilder {

    public OverlayBuilder(String id) {
        id(id + "-overlay");
        height("64px");
        width("64px");
        backgroundColor(SpellIconBuilder.overlayColor);
        childLayoutOverlay();
        text(new SpellCounterBuilder(id));
    }
}
