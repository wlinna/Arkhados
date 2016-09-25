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

import arkhados.messages.CmdServerLogin;
import arkhados.net.Command;
import arkhados.net.RecordingServerSender;
import com.jme3.network.serializing.Serializer;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReplayConverter {

    private static final Date _05 = new GregorianCalendar(2015, 6, 28).getTime();
    private static final Date _06 = new GregorianCalendar(2016, 1, 3).getTime();

    private final ReplaySerializer serializer = new ReplaySerializer();

    public void convert(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.position(2);
        ReplayData data = (ReplayData) Serializer.readClassAndObject(buffer);

        ExtraMetadata extra = findExtraMetadata(data);

        Path outputPath = Paths.get("converted", 
                Paths.get(path).getFileName().toString());

        ByteBuffer outBuffer = ByteBuffer.allocate(314572800); // 300 MB
//        serializer.convertFromOld(outBuffer, data, extra);
        outBuffer.flip();
        Path filePath = Files.createFile(outputPath);
        
        writeToFile(outBuffer, filePath);
    }

    private void writeToFile(ByteBuffer buffer, Path path) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(path.toString(), false);
            out.getChannel().write(buffer);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(RecordingServerSender.class.getName())
                    .log(Level.WARNING, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RecordingServerSender.class.getName())
                    .log(Level.WARNING, null, ex);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                    Logger.getLogger(RecordingServerSender.class.getName())
                            .log(Level.WARNING, null, ex);
                }
            }
        }

    }

    private ExtraMetadata findExtraMetadata(ReplayData replay) {
        ExtraMetadata extra = new ExtraMetadata();

        String version = versionFromDate(replay.getHeader().getDate());
        extra.version = version;

        if ("0.6".equals(version)) {
            extra.arena = "Pillar Arena";
        } else {
            extra.arena = "Lava Arena";
        }

        for (ReplayCmdData commandData : replay.getCommands()) {
            Command command = commandData.getCommand();
            if (command instanceof CmdServerLogin) {
                extra.gameMode = ((CmdServerLogin) command).getGameMode();
                break;
            }
        }

        return extra;
    }

    private String versionFromDate(Date date) {
        if (date.after(_06)) {
            return "0.6";
        } else if (date.after(_05)) {
            return "0.5";
        }

        return "";
    }
}

class ExtraMetadata {

    public String version;
    public String gameMode;
    public String arena;
}
