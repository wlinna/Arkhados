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

import arkhados.PlayerData;
import arkhados.characters.EliteSoldierSyncData;
import com.jme3.network.serializing.Serializer;
import arkhados.messages.syncmessages.CmdAction;
import arkhados.messages.syncmessages.CmdAddEntity;
import arkhados.messages.syncmessages.CmdBuff;
import arkhados.messages.syncmessages.CmdBuffStacks;
import arkhados.messages.syncmessages.CmdRemoveEntity;
import arkhados.messages.syncmessages.CmdSetCooldown;
import arkhados.messages.syncmessages.CmdStartCastingSpell;
import arkhados.messages.syncmessages.statedata.CharacterSyncData;
import arkhados.messages.syncmessages.statedata.GenericSyncData;
import arkhados.messages.syncmessages.statedata.ProjectileSyncData;
import arkhados.messages.syncmessages.statedata.StateData;
import arkhados.messages.usercommands.CmdUcCastSpell;
import arkhados.messages.usercommands.CmdUcMouseTarget;
import arkhados.messages.usercommands.CmdUcWalkDirection;
import arkhados.net.Ack;
import arkhados.net.OneTrueMessage;
import arkhados.net.OtmIdCommandListPair;
import arkhados.util.PlayerRoundStats;

/**
 *
 * @author william
 */
public class MessageUtils {

    public static void registerDataClasses() {
        Serializer.registerClass(PlayerData.class);
        Serializer.registerClass(PlayerRoundStats.class);
    }

    public static void registerMessages() {
        Serializer.registerClass(OneTrueMessage.class);
        Serializer.registerClass(OtmIdCommandListPair.class);

        Serializer.registerClass(Ack.class);
        Serializer.registerClass(CmdTopicOnly.class);

        // <Lobby>
        Serializer.registerClass(CmdServerLogin.class);
        Serializer.registerClass(CmdClientLogin.class);
        Serializer.registerClass(CmdClientSettings.class);
        Serializer.registerClass(CmdPlayerDataTable.class);
        Serializer.registerClass(ChatMessage.class);
        Serializer.registerClass(CmdSelectHero.class);
        Serializer.registerClass(CmdPlayerStatusChange.class);
        // </Lobby>

        // <Sync>
        Serializer.registerClass(CmdAddEntity.class);
        Serializer.registerClass(CmdRemoveEntity.class);
        Serializer.registerClasses(StateData.class, CharacterSyncData.class,
                GenericSyncData.class, ProjectileSyncData.class);
        Serializer.registerClass(EliteSoldierSyncData.class);
        Serializer.registerClass(CmdStartCastingSpell.class);
        Serializer.registerClass(CmdSetCooldown.class);
        Serializer.registerClass(CmdAction.class);
        Serializer.registerClass(CmdBuff.class);
        Serializer.registerClass(CmdBuffStacks.class);
        // </Sync>

        // <UserCommands>
        Serializer.registerClass(CmdUcCastSpell.class);
        Serializer.registerClass(CmdUcWalkDirection.class);
        Serializer.registerClass(CmdUcMouseTarget.class);
        // </UserCommands>

        // <Other>
        Serializer.registerClass(CmdSetPlayersCharacter.class);
        Serializer.registerClass(BattleStatisticsResponse.class);
        Serializer.registerClass(CmdPlayerKill.class);
        Serializer.registerClass(CmdWorldEffect.class);        
        // </Other>
    }
}