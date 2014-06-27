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
package arkhados.messages;

import arkhados.net.Command;
import com.jme3.network.serializing.Serializable;

/**
 *
 * @author william
 */

@Serializable
public class TopicOnlyCommand implements Command {    
    public static final transient int START_GAME = 0;
    public static final transient int BATTLE_STATISTICS_REQUEST = 1;
    public static final transient int UDP_HANDSHAKE_ACK = 2;
    public static final transient int CONNECTION_ESTABLISHED = 3;
    public static final transient int UDP_HANDSHAKE_REQUEST = 4;
    
    private transient boolean isGuaranteed = true;
    private byte topicId;
   
    public TopicOnlyCommand() {
    }
    
    public TopicOnlyCommand(int topicId) {
        this.topicId = (byte) topicId;
    }

    public TopicOnlyCommand(int topicId, boolean isGuaranteed) {
        this.topicId = (byte) topicId;
        this.isGuaranteed = isGuaranteed;        
    }     

    @Override
    public boolean isGuaranteed() {
        return isGuaranteed;
    }

    public int getTopicId() {
        return topicId;
    }   
}