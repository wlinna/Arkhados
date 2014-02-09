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
import arkhados.util.PlayerDataStrings;
import de.lessvoid.nifty.builder.PanelBuilder;
import de.lessvoid.nifty.builder.TextBuilder;

/**
 *
 * @author william
 */
public class PlayerStatisticsPanelBuilder extends PanelBuilder {

    public PlayerStatisticsPanelBuilder(long playerId) {
        super(String.valueOf(playerId) + "-statistics");
        super.alignLeft();
        super.valignTop();
        super.childLayoutHorizontal();
        super.style("nifty-panel");
        System.out.println("Created PSPB for player: " + playerId);
        super.height("100px");
        super.width("100%");
        
        final StatisticsTextBuilder nameBuilder = new StatisticsTextBuilder(playerId + "-name");
        nameBuilder.text(PlayerData.getStringData(playerId, PlayerDataStrings.NAME));
        super.text(nameBuilder);
        super.text(new StatisticsTextBuilder(playerId + "-damage"));
        super.text(new StatisticsTextBuilder(playerId + "-restoration"));
        super.text(new StatisticsTextBuilder(playerId + "-kills"));
    }
}

class StatisticsTextBuilder extends TextBuilder {

    public StatisticsTextBuilder(String id) {
        super(id);
        super.alignLeft();
        super.valignCenter();
        super.style("nifty-label");
//        super.height("100px");
        super.width("70px");
        super.marginLeft("80px");
        super.textHAlignLeft();
        super.color("#f00f");
    }
}