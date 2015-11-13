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

package arkhados.replay;

import com.jme3.network.serializing.Serializable;
import com.jme3.util.IntMap;
import java.util.Date;

@Serializable
public class ReplayHeader {
    private Date date = new Date();
    private final IntMap<String> players = new IntMap<>();

    public ReplayHeader() {
    }        
        
    public IntMap<String> getPlayers() {
        return players;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
