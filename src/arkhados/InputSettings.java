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
import arkhados.util.PlayerDataStrings;
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
        setKey(settings, InputMappingStrings.M1, false, MouseInput.BUTTON_LEFT);
        setKey(settings, InputMappingStrings.M2, false,
                MouseInput.BUTTON_RIGHT);

        setKey(settings, InputMappingStrings.MOVE_UP, true, KeyInput.KEY_W);
        setKey(settings, InputMappingStrings.MOVE_DOWN, true, KeyInput.KEY_S);
        setKey(settings, InputMappingStrings.MOVE_LEFT, true, KeyInput.KEY_A);
        setKey(settings, InputMappingStrings.MOVE_RIGHT, true, KeyInput.KEY_D);

        setKey(settings, InputMappingStrings.Q, true, KeyInput.KEY_Q);
        setKey(settings, InputMappingStrings.E, true, KeyInput.KEY_E);
        setKey(settings, InputMappingStrings.R, true, KeyInput.KEY_R);
        setKey(settings, InputMappingStrings.SPACE, true, KeyInput.KEY_SPACE);
        
        setKey(settings, InputMappingStrings.SEC1, true, KeyInput.KEY_1);
        setKey(settings, InputMappingStrings.SEC2, true, KeyInput.KEY_2);
        
        if (!settings.containsKey(PlayerDataStrings.COMMAND_MOVE_INTERRUPTS)) {
            settings.putBoolean(PlayerDataStrings.COMMAND_MOVE_INTERRUPTS,
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
        buttonIdInputMappingMap.put("button_up", InputMappingStrings.MOVE_UP);
        buttonIdInputMappingMap.put("button_down",
                InputMappingStrings.MOVE_DOWN);
        buttonIdInputMappingMap.put("button_left",
                InputMappingStrings.MOVE_LEFT);
        buttonIdInputMappingMap.put("button_right",
                InputMappingStrings.MOVE_RIGHT);

        buttonIdInputMappingMap.put("button_m1", InputMappingStrings.M1);
        buttonIdInputMappingMap.put("button_m2", InputMappingStrings.M2);

        buttonIdInputMappingMap.put("button_q", InputMappingStrings.Q);
        buttonIdInputMappingMap.put("button_e", InputMappingStrings.E);
        buttonIdInputMappingMap.put("button_r", InputMappingStrings.R);
        buttonIdInputMappingMap.put("button_space", InputMappingStrings.SPACE);
        
        buttonIdInputMappingMap.put("button_sec1", InputMappingStrings.SEC1);
        buttonIdInputMappingMap.put("button_sec2", InputMappingStrings.SEC2);
    }

    public InputSettings(InputManager inputManager) {
         inputManager.deleteTrigger(SimpleApplication.INPUT_MAPPING_EXIT,
                new KeyTrigger(KeyInput.KEY_ESCAPE));

        inputManager.addMapping(SimpleApplication.INPUT_MAPPING_EXIT,
                new KeyTrigger(KeyInput.KEY_F4));

        inputManager.addMapping(InputMappingStrings.MODIFIER,
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