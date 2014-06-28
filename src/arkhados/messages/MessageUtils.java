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
import arkhados.messages.roundprotocol.RoundStartCountdownCommand;
import arkhados.messages.syncmessages.ActionCommand;
import arkhados.messages.syncmessages.AddEntityCommand;
import arkhados.messages.syncmessages.BuffCommand;
import arkhados.messages.syncmessages.RemoveEntityCommand;
import arkhados.messages.syncmessages.RestoreTemporarilyRemovedEntityCommand;
import arkhados.messages.syncmessages.SetCooldownCommand;
import arkhados.messages.syncmessages.StartCastingSpellCommand;
import arkhados.messages.syncmessages.TemporarilyRemoveEntityCommand;
import arkhados.messages.syncmessages.statedata.CharacterSyncData;
import arkhados.messages.syncmessages.statedata.GenericSyncData;
import arkhados.messages.syncmessages.statedata.ProjectileSyncData;
import arkhados.messages.syncmessages.statedata.StateData;
import arkhados.messages.usercommands.UcCastSpellCommand;
import arkhados.messages.usercommands.UcMouseTargetCommand;
import arkhados.messages.usercommands.UcWalkDirection;
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
        Serializer.registerClass(TopicOnlyCommand.class);

        // <Lobby>
        Serializer.registerClass(ServerLoginCommand.class);
        Serializer.registerClass(ClientLoginCommand.class);
        Serializer.registerClass(ClientSettingsCommand.class);
        Serializer.registerClass(PlayerDataTableCommand.class);
        Serializer.registerClass(ChatMessage.class);
        Serializer.registerClass(ClientSelectHeroCommand.class);
        // </Lobby>

        // <RoundProtocol>
        Serializer.registerClass(RoundStartCountdownCommand.class);
        // </RoundProtocol>

        Serializer.registerClass(SetPlayersCharacterCommand.class);

        // <Sync>
        Serializer.registerClass(AddEntityCommand.class);
        Serializer.registerClass(RestoreTemporarilyRemovedEntityCommand.class);
        Serializer.registerClass(RemoveEntityCommand.class);
        Serializer.registerClass(TemporarilyRemoveEntityCommand.class);
        Serializer.registerClasses(StateData.class, CharacterSyncData.class, GenericSyncData.class, ProjectileSyncData.class);
        Serializer.registerClass(EliteSoldierSyncData.class);
        Serializer.registerClass(StartCastingSpellCommand.class);
        Serializer.registerClass(SetCooldownCommand.class);
        Serializer.registerClass(ActionCommand.class);
        Serializer.registerClass(BuffCommand.class);
        // </Sync>

        Serializer.registerClass(BattleStatisticsResponse.class);

        // <UserCommands>
        Serializer.registerClass(UcCastSpellCommand.class);
        Serializer.registerClass(UcWalkDirection.class);
        Serializer.registerClass(UcMouseTargetCommand.class);
        // </UserCommands>        
    }
}