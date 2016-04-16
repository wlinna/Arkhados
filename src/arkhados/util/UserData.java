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

package arkhados.util;

public class UserData {
    public final static String SPEED_MOVEMENT_BASE = "speed-movement-base";
    public final static String SPEED_MOVEMENT = "speed-movement";
    public final static String HEALTH_CURRENT = "health-current";
    public final static String HEALTH_MAX = "health-max";
    public final static String HEALTH_LOW_RECORD = "health-low-record";
    public final static String ENTITY_ID = "entity-id";
    public final static String PLAYER_ID = "player-id";
    public final static String RADIUS = "radius";
    public final static String MASS = "mass";
    public final static String DAMAGE_FACTOR = "damage-modifier";
    public final static String LIFE_STEAL_BASE = "life-steal-base";
    public final static String LIFE_STEAL = "life-steal";
    public final static String NODE_BUILDER_ID = "node-builder-id";
    public final static String BIRTHTIME = "birthtime";
    /**
     * How much entity deals instant damage when it hits
     */
    public final static String DAMAGE = "damage";
    /**
     * How much damage it deals per second to be on touch with entity
     */
    public final static String DAMAGE_PER_SECOND = "damage-per-second";
    public final static String IMPULSE_FACTOR = "impulse-factor";

    
    public final static String INVISIBLE_TO_ALL = "invisible-all";
    public final static String INVISIBLE_TO_ENEMY = "invisible-enemy";
    
    // NOTE: INCAPACITATE_LENGTH has no functionality yet
    public final static String INCAPACITATE_LENGTH = "incapacitate-length";
    public final static String TEAM_ID = "team-id";
    
    public final static String FOLLOW_ME = "follow-me";
}
