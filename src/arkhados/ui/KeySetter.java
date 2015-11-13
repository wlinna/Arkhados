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
package arkhados.ui;

import arkhados.ClientMain;
import arkhados.InputSettings;
import arkhados.PlayerData;
import arkhados.TriggerPair;
import com.jme3.app.Application;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.KeyNames;
import com.jme3.input.RawInputListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.event.JoyAxisEvent;
import com.jme3.input.event.JoyButtonEvent;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.input.event.TouchEvent;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Button;
import de.lessvoid.nifty.controls.CheckBox;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;

public class KeySetter implements RawInputListener, ScreenController {

    private static KeyNames keyNames = new KeyNames();
    private InputManager inputManager;
    private Nifty nifty;
    private Screen screen;
    private Button currentKeyButton = null;
    private String currentInputName = null;
    private Application app;
    private InputSettings inputSettings;

    public KeySetter(Application app, InputManager inputManager,
            InputSettings inputSettings) {
        this.app = app;
        this.inputManager = inputManager;
        this.inputSettings = inputSettings;
    }

    @Override
    public void beginInput() {
    }

    @Override
    public void endInput() {
    }

    @Override
    public void onJoyAxisEvent(JoyAxisEvent evt) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void onJoyButtonEvent(JoyButtonEvent evt) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void onMouseMotionEvent(MouseMotionEvent evt) {
    }

    @Override
    public void onMouseButtonEvent(MouseButtonEvent evt) {
        if (currentInputName == null || currentKeyButton == null) {
            return;
        }
        inputManager.removeRawInputListener(this);

        final MouseButtonTrigger trigger =
                new MouseButtonTrigger(evt.getButtonIndex());
        inputManager.deleteMapping(currentInputName);
        inputManager.addMapping(currentInputName, trigger);
        inputSettings.saveInput(currentInputName, trigger);

        try {
            app.getContext().getSettings().save(ClientMain.PREFERENCES_KEY);
        } catch (BackingStoreException ex) {
            Logger.getLogger(KeySetter.class.getName())
                    .log(Level.WARNING, "Failed to save settings", ex);
        }

        app.enqueue(() -> {
            if (trigger.getMouseButton() == 0) {
                currentKeyButton.setText("Mouse Left");
            } else if (trigger.getMouseButton() == 1) {
                currentKeyButton.setText("Mouse Right");
            } else if (trigger.getMouseButton() == 2) {
                currentKeyButton.setText("Mouse Middle");
            } else {
                currentKeyButton.setText("Mouse Strange");
            }
            
            currentKeyButton = null;
            currentInputName = null;
            
            return null;
        });

        evt.setConsumed();
    }

    @Override
    public void onKeyEvent(KeyInputEvent evt) {
        if (currentInputName == null || currentKeyButton == null) {
            return;
        }
        if (evt.getKeyCode() == KeyInput.KEY_ESCAPE) {
            evt.setConsumed();
            return;
        }
        inputManager.removeRawInputListener(this);

        KeyTrigger trigger = new KeyTrigger(evt.getKeyCode());

        inputManager.deleteMapping(currentInputName);
        inputManager.addMapping(currentInputName, trigger);
        inputSettings.saveInput(currentInputName, trigger);
        try {
            app.getContext().getSettings().save(ClientMain.PREFERENCES_KEY);
        } catch (BackingStoreException ex) {
            Logger.getLogger(KeySetter.class.getName())
                    .log(Level.WARNING, "Failed to save settings", ex);
        }
        final String keyChar =
                Character.toString(evt.getKeyChar()).toUpperCase();
        evt.setConsumed();
        app.enqueue(() -> {
            currentKeyButton.setText(keyChar);
            
            currentKeyButton = null;
            currentInputName = null;
            return null;
        });
    }

    @Override
    public void onTouchEvent(TouchEvent evt) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void bind(Nifty nifty, final Screen screen) {
        this.nifty = nifty;
        this.screen = screen;
        
        app.enqueue(() -> {
            Map<String, String> buttonIdInputMappingMap =
                    inputSettings.getButtonIdInputMappingMap();
            for (Entry<String, String> entry :
                    buttonIdInputMappingMap.entrySet()) {
                String buttonName = entry.getKey();
                
                String inputMapping =
                        buttonIdInputMappingMap.get(buttonName);
                Button button =
                        screen.findNiftyControl(buttonName, Button.class);
                TriggerPair pair = inputSettings.loadInput(inputMapping);
                if (pair.isKeyboard) {
                    KeyTrigger keyTrigger = (KeyTrigger) pair.trigger;
                    String character = KeySetter.keyNames
                            .getName(keyTrigger.getKeyCode());
                    button.setText(character);
                } else {
                    MouseButtonTrigger mouseTrigger =
                            (MouseButtonTrigger) pair.trigger;
                    if (mouseTrigger.getMouseButton() == 0) {
                        button.setText("Mouse Left");
                    } else if (mouseTrigger.getMouseButton() == 1) {
                        button.setText("Mouse Right");
                    } else if (mouseTrigger.getMouseButton() == 2) {
                        button.setText("Mouse Middle");
                    } else {
                        button.setText("Mouse Strange");
                    }
                }
            }
            
            boolean moveInterrupts = app.getContext().getSettings()
                    .getBoolean(PlayerData.COMMAND_MOVE_INTERRUPTS);
            CheckBox cbox_moveInterrupts = screen.findNiftyControl(
                    "cbox_move_interrupts", CheckBox.class);
            cbox_moveInterrupts.setChecked(moveInterrupts);
            
            return null;
        });
    }

    @Override
    public void onStartScreen() {
    }

    @Override
    public void onEndScreen() {
        inputManager.removeRawInputListener(this);
    }

    public void waitAndSetKey(String buttonId, String inputName) {
        currentKeyButton = screen.findNiftyControl(buttonId, Button.class);
        currentInputName = inputName;
        inputManager.addRawInputListener(this);
        app.enqueue(() -> {
            currentKeyButton.setText("Press esc to cancel");
            return null;
        });

    }

    public void checked(final String cboxId) {
        final CheckBox cbox = screen.findNiftyControl(cboxId, CheckBox.class);
        if (cbox == null) {
            return;
        }
        app.enqueue(() -> {
            cbox.toggle();
            
            if ("cbox_move_interrupts".equals(cboxId)) {
                app.getContext().getSettings().putBoolean(
                        PlayerData.COMMAND_MOVE_INTERRUPTS,
                        cbox.isChecked());
            }
            try {
                app.getContext().getSettings()
                        .save(ClientMain.PREFERENCES_KEY);
            } catch (BackingStoreException ex) {
                Logger.getLogger(KeySetter.class.getName())
                        .log(Level.WARNING, ex.getMessage(), ex);
            }
            return null;
        });
    }

    public void gotoMenu(final String menu) {
        app.enqueue(() -> {
            nifty.gotoScreen(menu);
            return null;
        });
    }
}