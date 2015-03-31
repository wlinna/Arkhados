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

import com.jme3.asset.AssetManager;

public class Preloader {

    public static void loadServer(AssetManager manager) {
        loadCommon(manager);
    }

    public static void loadClient(AssetManager manager) {
        loadCommon(manager);

        String[] sounds = new String[]{
            "EmberCircle.wav",
            "FireballExplosion.wav",
            "Firewalk.wav",
            "MagmaBash.wav",
            "MeteorBoom.wav",
            "PurifyingFlame.wav",
            "Shotgun.wav",
            "Rend1.wav",
            "Rend2.wav",
            "Rend3.wav",
            "RockGolemPain.wav",
            "VenatorDeath.wav",
            "VenatorPain.wav",
            "EmberMageDeath.wav",
            "EmberMagePain.wav",
            "EliteSoldierDeath.wav",
            "EliteSoldierPain.wav",
            "DeepWounds.wav",
            "Petrify.wav",
            "Railgun.wav"
        };

        preloadSounds(manager, sounds);
    }

    private static void loadCommon(AssetManager manager) {
        String[] models = new String[]{
            "Models/EliteSoldier.j3o",
            "Models/Mage.j3o",
            "Models/Warwolf.j3o",
            "Models/RockGolem.j3o",
            "Models/Circle.j3o",
            "Models/DamagingDagger.j3o",
            "Models/SealingBoulder.j3o",
            "Models/SpiritStone.j3o",
            "Scenes/LavaArenaWithFogWalls.j3o"
        };

        preloadModels(manager, models);
    }

    private static void preloadModels(AssetManager manager, String[] names) {
        for (String path : names) {
            manager.loadModel(path);
        }
    }

    private static void preloadSounds(AssetManager manager, String[] sounds) {
        for (String audioPath : sounds) {
            manager.loadAudio("Effects/Sound/" + audioPath);
        }
    }
}
