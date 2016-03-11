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

import com.jme3.network.serializing.Serializer;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ReplaySerializer extends Serializer {

    @Override
    public ReplayData readObject(ByteBuffer data, Class c) throws IOException {
        ReplayData replayData = new ReplayData();

        Serializer stringSerializer = getExactSerializer(String.class);
        Serializer dateSerializer = getExactSerializer(Date.class);
        Serializer mapSerializer = getExactSerializer(HashMap.class);
        Serializer seqSerializer = getExactSerializer(ArrayList.class);

        Date date = dateSerializer.readObject(data, Date.class);
        replayData.getHeader().setDate(date);

        String version = stringSerializer.readObject(data, String.class);
        replayData.getHeader().setVersion(version);
        
        String gameMode = stringSerializer.readObject(data, String.class);
        replayData.getHeader().setGameMode(gameMode);
        
        String arena = stringSerializer.readObject(data, String.class);
        replayData.getHeader().setArena(arena);

        HashMap map = mapSerializer.readObject(data, HashMap.class);
        replayData.getHeader().getPlayers().putAll(map);

        byte flag = data.get();

        ArrayList replayCmds = seqSerializer.readObject(data, ArrayList.class);
        replayData.getCommands().addAll(replayCmds);

        return replayData;
    }

    
    @Override
    public void writeObject(ByteBuffer buffer, Object object) throws IOException {
        if (!(object instanceof ReplayData)) {
            throw new IllegalArgumentException("object is not instance of ReplayData");
        }

        ReplayData replayData = (ReplayData) object;

        Serializer stringSerializer = getExactSerializer(String.class);
        Serializer dateSerializer = getExactSerializer(Date.class);
        Serializer mapSerializer = getExactSerializer(HashMap.class);
        Serializer seqSerializer = getExactSerializer(ArrayList.class);

        dateSerializer.writeObject(buffer, replayData.getHeader().getDate());               
        
        stringSerializer.writeObject(buffer, replayData.getHeader().getVersion());
        stringSerializer.writeObject(buffer, replayData.getHeader().getGameMode());        
        stringSerializer.writeObject(buffer, replayData.getHeader().getArena());        
        
        mapSerializer.writeObject(buffer, replayData.getHeader().getPlayers());

        byte flag = 0;
        buffer.put(flag);

        seqSerializer.writeObject(buffer, replayData.getCommands());
    }
}
