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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

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

        if (flag == 0) {
            ArrayList replayCmds = seqSerializer.readObject(data, ArrayList.class);
            replayData.getCommands().addAll(replayCmds);
        } else if (flag == 1) {
            int decompressedLength = data.getInt();
            
            byte[] compressedBytes = new byte[data.remaining()];
            data.get(compressedBytes);
            Inflater inf = new Inflater();
            inf.setInput(compressedBytes, 0, compressedBytes.length);
            byte[] decompressedBytes = new byte[decompressedLength];
            try {
                if (inf.inflate(decompressedBytes) != decompressedLength) {
                    throw new AssertionError();
                }
            } catch (DataFormatException ex) {
                Logger.getLogger(ReplaySerializer.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
            
            inf.end();
            
            ByteBuffer decompressedBuffer = ByteBuffer.wrap(decompressedBytes);
            ArrayList replayCmds = seqSerializer
                    .readObject(decompressedBuffer, ArrayList.class);
            replayData.getCommands().addAll(replayCmds);
        }

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

        byte flag = 1; // Compression on (mode 1)
        buffer.put(flag);

        ByteBuffer sequenceBuffer = ByteBuffer.allocate(314572800);

        seqSerializer.writeObject(sequenceBuffer, replayData.getCommands());
        sequenceBuffer.flip();

        byte[] sequenceOutput = new byte[sequenceBuffer.remaining()];
        sequenceBuffer.get(sequenceOutput);
        
        buffer.putInt(sequenceOutput.length);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Deflater def = new Deflater(Deflater.BEST_SPEED);
        def.setInput(sequenceOutput);
        def.finish();

        byte[] buf = new byte[8192];
        while (!def.finished()) {
            int byteCount = def.deflate(buf);
            out.write(buf, 0, byteCount);
        }

        def.end();
        byte[] compressedBytes = out.toByteArray();

        buffer.put(compressedBytes);
    }
}
