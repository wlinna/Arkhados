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

import arkhados.util.InputMapping;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioSource;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.FastMath;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MusicManager extends AbstractAppState {

    private final List<String> music = new ArrayList<>(2);
    private AudioNode musicPlayer = null;
    private int heroMusicIndex = 0;
    private float volume = 0.4f;
    private boolean playing = false;
    private final Application app;
    private final InputManager inputManager;
    private String currentMusicCategory = "";

    public MusicManager(Application app, InputManager inputManager) {
        this.app = app;
        this.inputManager = inputManager;

        inputManager.addMapping(InputMapping.VOLUME_UP,
                new KeyTrigger(KeyInput.KEY_F10));
        inputManager.addMapping(InputMapping.VOLUME_DOWN,
                new KeyTrigger(KeyInput.KEY_F9));

        inputManager.addListener(changeVolumeActionListener,
                InputMapping.VOLUME_UP, InputMapping.VOLUME_DOWN);
    }
    private ActionListener changeVolumeActionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (isPressed) {
                return;
            }
            float newVolume = volume;

            if (InputMapping.VOLUME_UP.equals(name)) {
                newVolume += 0.1f;
            } else if (InputMapping.VOLUME_DOWN.equals(name)) {
                newVolume -= 0.1f;
            }

            newVolume = FastMath.clamp(newVolume, 0, 1);
            volume = newVolume;

            app.enqueue(() -> {
                if (musicPlayer != null) {
                    musicPlayer.setVolume(volume);
                }
                return null;
            });

        }
    };

    private void playNext() {
        if (music.isEmpty()) {
            playing = false;
            return;
        }
        heroMusicIndex = (++heroMusicIndex) % music.size();
        String path = music.get(heroMusicIndex);

        musicPlayer = new AudioNode(Globals.assets, path, true);
        musicPlayer.setPositional(false);
        musicPlayer.setVolume(volume);
        musicPlayer.play();
    }

    public void setMusicCategory(String category) {
        if (currentMusicCategory.equals(category)) {
            return;
        }

        if (musicPlayer != null) {
            app.enqueue(() -> {
                musicPlayer.stop();
                return null;
            });
        }

        music.clear();

        currentMusicCategory = category;

        switch (category) {
            case "Menu":
                music.add("Music/Menu.ogg");
                break;
            case "EmberMage":
                music.add(generateHeroMusicPath(category, "TheDarkAmulet"));
                music.add(generateHeroMusicPath(category, "SteepsOfDestiny"));
                break;
            case "EliteSoldier":
                music.add(generateHeroMusicPath(category, "Carmack"));
                music.add(generateHeroMusicPath(category, "ElectricQuake"));
                music.add(generateHeroMusicPath(category, "AntiGravity"));
                music.add(generateHeroMusicPath(category, "SantaF_ckedUp"));
                break;
            case "Venator":
                music.add(generateHeroMusicPath(category, "DarkHall"));
                music.add(generateHeroMusicPath(category, "GreyLand"));
                music.add(generateHeroMusicPath(category, "TimeOfBlood"));
                music.add(generateHeroMusicPath(category, "PredatorAttack"));
                break;
            case "RockGolem":
                music.add(generateHeroMusicPath(category, "GodsWar"));
                music.add(generateHeroMusicPath(category, "Olympus"));
                music.add(generateHeroMusicPath(category, "DwarvesGathering"));
                break;
            case "Shadowmancer":
                music.add(generateHeroMusicPath(category, "Transylvania"));
                music.add(generateHeroMusicPath(category, "MyDarkPassenger"));
                break;
            default:
                break;
        }
        if (music.size() > 0) {
            heroMusicIndex = new Random().nextInt(music.size());
        }
    }

    public void clearHeroMusic() {
        music.clear();
        currentMusicCategory = "";
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

        if (musicPlayer == null
                || musicPlayer.getStatus() == AudioSource.Status.Stopped) {
            playNext();
        }
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;

        if (!playing) {
            app.enqueue(() -> {
                if (musicPlayer != null) {
                    musicPlayer.stop();
                }
                return null;
            });
        }
    }

    @Override
    public void stateDetached(AppStateManager stateManager) {
        super.stateDetached(stateManager);
        currentMusicCategory = "";
        if (musicPlayer != null) {
            musicPlayer.stop();
        }
    }
}