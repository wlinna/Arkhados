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

import arkhados.PlayerData;
import de.lessvoid.nifty.builder.PanelBuilder;
import de.lessvoid.nifty.builder.TextBuilder;

/**
 *
 * @author william
 */
public class PlayerStatisticsPanelBuilder extends PanelBuilder {

    public PlayerStatisticsPanelBuilder(int playerId) {
        super(String.valueOf(playerId) + "-statistics");
        alignLeft();
        valignTop();
        childLayoutHorizontal();
        style("nifty-panel");
        height("70px");
        width("100%");

        StatisticsTextBuilder nameBuilder =
                new StatisticsTextBuilder(playerId + "-name");
        nameBuilder.text(PlayerData.getStringData(playerId,
                PlayerData.NAME));
        text(nameBuilder);
        text(new StatisticsTextBuilder(playerId + "-damage"));
        text(new StatisticsTextBuilder(playerId + "-restoration"));
        text(new StatisticsTextBuilder(playerId + "-kills"));
    }
}

class StatisticsTextBuilder extends TextBuilder {

    public StatisticsTextBuilder(String id) {
        super(id);
        alignLeft();
        valignCenter();
        style("nifty-label");
        width("70px");
        marginLeft("80px");
        textHAlignLeft();
        color("#f00f");
    }
}