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

import arkhados.util.InputMappingStrings;
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
    private final InputManager inputManager;
    private String currentHero = "";

    public MusicManager(Application app, InputManager inputManager, AssetManager assetManager) {
        this.assetManager = assetManager;
        this.app = app;
        this.inputManager = inputManager;

        inputManager.addMapping(InputMappingStrings.VOLUME_UP, new KeyTrigger(KeyInput.KEY_F10));
        inputManager.addMapping(InputMappingStrings.VOLUME_DOWN, new KeyTrigger(KeyInput.KEY_F9));

        inputManager.addListener(changeVolumeActionListener, InputMappingStrings.VOLUME_UP,
                InputMappingStrings.VOLUME_DOWN);
    }
    private ActionListener changeVolumeActionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (isPressed) {
                return;
            }
            float newVolume = volume;

            if (InputMappingStrings.VOLUME_UP.equals(name)) {
                newVolume += 0.1f;
            } else if (InputMappingStrings.VOLUME_DOWN.equals(name)) {
                newVolume -= 0.1f;
            }

            newVolume = FastMath.clamp(newVolume, 0, 1);
            volume = newVolume;

            app.enqueue(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    if (musicPlayer != null) {
                        musicPlayer.setVolume(volume);
                    }
                    return null;
                }
            });

        }
    };

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

    public void setHero(String heroName) {
        if (currentHero.equals(heroName)) {
            return;
        }

        if (musicPlayer != null) {
            musicPlayer.stop();
        }

        heroMusic.clear();

        currentHero = heroName;

        switch (heroName) {
            case "EmberMage":
                heroMusic.add(generateHeroMusicPath(heroName, "TheDarkAmulet"));
                heroMusic.add(generateHeroMusicPath(heroName, "SteepsOfDestiny"));
                break;
            case "EliteSoldier":
                heroMusic.add(generateHeroMusicPath(heroName, "Carmack"));
                heroMusic.add(generateHeroMusicPath(heroName, "ElectricQuake"));
                heroMusic.add(generateHeroMusicPath(heroName, "AntiGravity"));
                break;
            case "Venator":
                heroMusic.add(generateHeroMusicPath(heroName, "DarkHall"));
                heroMusic.add(generateHeroMusicPath(heroName, "GreyLand"));
                heroMusic.add(generateHeroMusicPath(heroName, "TimeOfBlood"));
                heroMusic.add(generateHeroMusicPath(heroName, "PredatorAttack"));
                break;
            case "RockGolem":
                heroMusic.add(generateHeroMusicPath(heroName, "GodsWar"));
                heroMusic.add(generateHeroMusicPath(heroName, "Olympus"));
                heroMusic.add(generateHeroMusicPath(heroName, "DwarvesGathering"));
                break;
            default:
                break;
        }
        if (heroMusic.size() > 0) {
            heroMusicIndex = new Random().nextInt(heroMusic.size());
        }
    }

    public void clearHeroMusic() {
        heroMusic.clear();
        currentHero = "";
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
        currentHero = "";
        if (musicPlayer != null) {
            musicPlayer.stop();
        }
    }
}