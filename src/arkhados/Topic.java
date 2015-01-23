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

/**
 *
 * @author william
 */
public class Topic {

    public static final int START_GAME = 0;
    public static final int BATTLE_STATISTICS_REQUEST = 1;
    public static final int UDP_HANDSHAKE_ACK = 2;    
    public static final int CONNECTION_ESTABLISHED = 3;
    public static final int UDP_HANDSHAKE_REQUEST = 4;
    public static final int CREATE_WORLD = 5;
    public static final int NEW_ROUND= 6;
    public static final int ROUND_FINISHED = 7;
    public static final int GAME_ENDED = 8;
    public static final int CLIENT_WORLD_CREATED = 9;
    public static final int FIRST_BLOOD_HAPPENED = 10;
}
