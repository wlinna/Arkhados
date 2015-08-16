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
import com.jme3.app.SimpleApplication;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.system.AppSettings;
import java.util.HashMap;

public final class InputSettings {

    public static void setInputDefaultSettings(AppSettings settings) {
        setKey(settings, InputMapping.M1, false, MouseInput.BUTTON_LEFT);
        setKey(settings, InputMapping.M2, false, MouseInput.BUTTON_RIGHT);

        setKey(settings, InputMapping.MOVE_UP, true, KeyInput.KEY_W);
        setKey(settings, InputMapping.MOVE_DOWN, true, KeyInput.KEY_S);
        setKey(settings, InputMapping.MOVE_LEFT, true, KeyInput.KEY_A);
        setKey(settings, InputMapping.MOVE_RIGHT, true, KeyInput.KEY_D);

        setKey(settings, InputMapping.Q, true, KeyInput.KEY_Q);
        setKey(settings, InputMapping.E, true, KeyInput.KEY_E);
        setKey(settings, InputMapping.R, true, KeyInput.KEY_R);
        setKey(settings, InputMapping.SPACE, true, KeyInput.KEY_SPACE);

        setKey(settings, InputMapping.SEC1, true, KeyInput.KEY_1);
        setKey(settings, InputMapping.SEC2, true, KeyInput.KEY_2);

        if (!settings.containsKey(PlayerData.COMMAND_MOVE_INTERRUPTS)) {
            settings.putBoolean(PlayerData.COMMAND_MOVE_INTERRUPTS,
                    false);
        }
    }

    private static void setKey(AppSettings settings, String inputMapping,
            boolean isKeyboard, int code) {
        if (settings.containsKey(inputMapping)) {
            return;
        }
        String prefix = isKeyboard ? "keyboard::" : "mouse::";
        String setting = prefix + Integer.toString(code);
        settings.putString(inputMapping, setting);

    }
    private HashMap<String, String> buttonIdInputMappingMap = new HashMap<>(10);

    {
        buttonIdInputMappingMap.put("button_up", InputMapping.MOVE_UP);
        buttonIdInputMappingMap.put("button_down", InputMapping.MOVE_DOWN);
        buttonIdInputMappingMap.put("button_left", InputMapping.MOVE_LEFT);
        buttonIdInputMappingMap.put("button_right", InputMapping.MOVE_RIGHT);

        buttonIdInputMappingMap.put("button_m1", InputMapping.M1);
        buttonIdInputMappingMap.put("button_m2", InputMapping.M2);

        buttonIdInputMappingMap.put("button_q", InputMapping.Q);
        buttonIdInputMappingMap.put("button_e", InputMapping.E);
        buttonIdInputMappingMap.put("button_r", InputMapping.R);
        buttonIdInputMappingMap.put("button_space", InputMapping.SPACE);

        buttonIdInputMappingMap.put("button_sec1", InputMapping.SEC1);
        buttonIdInputMappingMap.put("button_sec2", InputMapping.SEC2);
    }

    public InputSettings(InputManager inputManager) {
        inputManager.deleteTrigger(SimpleApplication.INPUT_MAPPING_EXIT,
                new KeyTrigger(KeyInput.KEY_ESCAPE));

        inputManager.addMapping(InputMapping.HUD_TOGGLE_MENU,
                new KeyTrigger(KeyInput.KEY_ESCAPE));

        inputManager.addMapping(InputMapping.MODIFIER,
                new KeyTrigger(KeyInput.KEY_LSHIFT));

        for (String inputMapping : buttonIdInputMappingMap.values()) {
            Trigger trigger = loadInput(inputMapping).trigger;
            inputManager.addMapping(inputMapping, trigger);
        }
    }

    public TriggerPair loadInput(String inputMappingString) {
        String inputString = Globals.app.getContext().getSettings()
                .getString(inputMappingString);
        if (inputString == null) {
            return null;
        }
        String[] parts = inputString.split("::");
        Integer code = Integer.parseInt(parts[1]);

        TriggerPair triggerPair = null;

        if ("keyboard".equals(parts[0])) {
            triggerPair = new TriggerPair(true, new KeyTrigger(code));
        } else if ("mouse".equals(parts[0])) {
            triggerPair = new TriggerPair(false, new MouseButtonTrigger(code));
        }
        return triggerPair;
    }

    public void saveInput(String inputMappingString, KeyTrigger trigger) {
        String inputString = "keyboard::"
                + Integer.toString(trigger.getKeyCode());
        Globals.app.getContext().getSettings()
                .putString(inputMappingString, inputString);
    }

    public void saveInput(String inputMappingString,
            MouseButtonTrigger trigger) {
        String inputString = "mouse::"
                + Integer.toString(trigger.getMouseButton());
        Globals.app.getContext().getSettings()
                .putString(inputMappingString, inputString);
    }

    public HashMap<String, String> getButtonIdInputMappingMap() {
        return buttonIdInputMappingMap;
    }
}