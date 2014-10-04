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

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioSource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 *
 * @author william
 */
public class MusicManager extends AbstractAppState {

    private final List<String> heroMusic = new ArrayList<>(2);
    private AudioNode musicPlayer = null;
    private AssetManager assetManager;
    private int heroMusicIndex = 0;
    private float volume = 0.2f;
    private boolean playing = false;
    private final Application app;

    public MusicManager(Application app, AssetManager assetManager) {
        this.assetManager = assetManager;
        this.app = app;
    }

    private void playNext() {
        if (heroMusic.isEmpty()) {
            playing = false;
            return;
        }
        heroMusicIndex = (++heroMusicIndex) % heroMusic.size();
        String path = heroMusic.get(heroMusicIndex);

        musicPlayer = new AudioNode(assetManager, path, true);
        musicPlayer.setPositional(false);
        musicPlayer.setVolume(volume);
        musicPlayer.play();
    }

    public void start() {
        playing = !heroMusic.isEmpty();
    }

    public void setHero(String heroName) {
        heroMusic.clear();
        switch (heroName) {
            case "EmberMage":
                heroMusic.add(generateHeroMusicPath(heroName, "TheDarkAmulet"));
                heroMusic.add(generateHeroMusicPath(heroName, "SteepsOfDestiny"));
                break;
            case "EliteSoldier":
                heroMusic.add(generateHeroMusicPath(heroName, "Carmack"));
                heroMusic.add(generateHeroMusicPath(heroName, "ElectricQuake"));
                break;
            case "Venator":
                break;
        }
    }

    public void clearHeroMusic() {
        heroMusic.clear();
    }

    private static String generateHeroMusicPath(String heroName, String name) {
        return "Music/" + heroName + "/" + name + ".ogg";
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
        if (!playing) {
            return;
        }

        if (musicPlayer == null || musicPlayer.getStatus() == AudioSource.Status.Stopped) {
            playNext();
        }
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;

        if (!playing) {
            app.enqueue(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    if (musicPlayer != null) {
                        musicPlayer.stop();
                    }
                    return null;
                }
            });
        }
    }

    @Override
    public void stateDetached(AppStateManager stateManager) {
        super.stateDetached(stateManager);
        if (musicPlayer != null) {
            musicPlayer.stop();
        }
    }
}