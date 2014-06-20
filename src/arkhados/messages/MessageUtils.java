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
import arkhados.messages.roundprotocol.RoundFinishedMessage;
import arkhados.messages.roundprotocol.NewRoundMessage;
import com.jme3.network.serializing.Serializer;
import arkhados.messages.roundprotocol.ClientWorldCreatedMessage;
import arkhados.messages.roundprotocol.PlayerReadyForNewRoundMessage;
import arkhados.messages.roundprotocol.CreateWorldMessage;
import arkhados.messages.roundprotocol.GameEndedMessage;
import arkhados.messages.roundprotocol.RoundStartCountdownMessage;
import arkhados.messages.syncmessages.ActionMessage;
import arkhados.messages.syncmessages.AddEntityMessage;
import arkhados.messages.syncmessages.BuffMessage;
import arkhados.messages.syncmessages.MassSyncMessage;
import arkhados.messages.syncmessages.RemoveEntityMessage;
import arkhados.messages.syncmessages.RestoreTemporarilyRemovedEntityMessage;
import arkhados.messages.syncmessages.SetCooldownMessage;
import arkhados.messages.syncmessages.StartCastingSpellMessage;
import arkhados.messages.syncmessages.TemporarilyRemoveEntityMessage;
import arkhados.messages.syncmessages.statedata.CharacterSyncData;
import arkhados.messages.syncmessages.statedata.GenericSyncData;
import arkhados.messages.syncmessages.statedata.ProjectileSyncData;
import arkhados.messages.syncmessages.statedata.StateData;
import arkhados.messages.usercommands.UcCastSpellMessage;
import arkhados.messages.usercommands.UcMouseTargetMessage;
import arkhados.messages.usercommands.UcWalkDirection;
import arkhados.net.Ack;
import arkhados.net.OneTrueMessage;
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
        
        // <Commands>
        Serializer.registerClass(Ack.class);
        
        // <Lobby>
        Serializer.registerClass(ConnectionEstablishedMessage.class);
        Serializer.registerClass(UDPHandshakeRequest.class);
        Serializer.registerClass(UDPHandshakeAck.class);
        Serializer.registerClass(ServerLoginMessage.class);
        Serializer.registerClass(ClientLoginMessage.class);
        Serializer.registerClass(ClientSettingsMessage.class);
        Serializer.registerClass(PlayerDataTableMessage.class);
        Serializer.registerClass(ChatMessage.class);
        Serializer.registerClass(StartGameMessage.class);
        Serializer.registerClass(ClientSelectHeroMessage.class);
        // </Lobby>

        // <RoundProtocol>
        Serializer.registerClass(CreateWorldMessage.class);
        Serializer.registerClass(ClientWorldCreatedMessage.class);
        Serializer.registerClass(PlayerReadyForNewRoundMessage.class);
        Serializer.registerClass(RoundStartCountdownMessage.class);
        Serializer.registerClass(NewRoundMessage.class);
        Serializer.registerClass(RoundFinishedMessage.class);
        Serializer.registerClass(GameEndedMessage.class);
        // </RoundProtocol>

        Serializer.registerClass(SetPlayersCharacterMessage.class);

        // <Sync>
        Serializer.registerClass(AddEntityMessage.class);
        Serializer.registerClass(RestoreTemporarilyRemovedEntityMessage.class);
        Serializer.registerClass(RemoveEntityMessage.class);
        Serializer.registerClass(TemporarilyRemoveEntityMessage.class);
        Serializer.registerClass(MassSyncMessage.class);
        Serializer.registerClasses(StateData.class, CharacterSyncData.class, GenericSyncData.class, ProjectileSyncData.class);
        Serializer.registerClass(EliteSoldierSyncData.class);
        Serializer.registerClass(StartCastingSpellMessage.class);
        Serializer.registerClass(SetCooldownMessage.class);
        Serializer.registerClass(ActionMessage.class);
        Serializer.registerClass(BuffMessage.class);
        // </Sync>

        Serializer.registerClass(BattleStatisticsRequest.class);
        Serializer.registerClass(BattleStatisticsResponse.class);

        // <UserCommands>
        Serializer.registerClass(UcCastSpellMessage.class);
        Serializer.registerClass(UcWalkDirection.class);
        Serializer.registerClass(UcMouseTargetMessage.class);
        // </UserCommands>        
    }
}