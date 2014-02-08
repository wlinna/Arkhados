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
import arkhados.util.InputMappingStrings;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.KeyNames;
import com.jme3.input.RawInputListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.input.event.JoyAxisEvent;
import com.jme3.input.event.JoyButtonEvent;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.input.event.TouchEvent;
import com.jme3.system.AppSettings;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Button;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;

/**
 *
 * @author william
 */
public class KeySetter implements RawInputListener, ScreenController {

    private static KeyNames keyNames = new KeyNames();
    private InputManager inputManager;
    private Nifty nifty;
    private Screen screen;
    private Button currentKeyButton = null;
    private String currentInputName = null;
    private Application app;
    private HashMap<String, String> buttonIdInputMappingMap = new HashMap<String, String>(10);

    public KeySetter(Application app, InputManager inputManager) {
        this.app = app;
        this.inputManager = inputManager;

        this.buttonIdInputMappingMap.put("button_up", InputMappingStrings.MOVE_UP);
        this.buttonIdInputMappingMap.put("button_down", InputMappingStrings.MOVE_DOWN);
        this.buttonIdInputMappingMap.put("button_left", InputMappingStrings.MOVE_LEFT);
        this.buttonIdInputMappingMap.put("button_right", InputMappingStrings.MOVE_RIGHT);

        this.buttonIdInputMappingMap.put("button_m1", InputMappingStrings.M1);
        this.buttonIdInputMappingMap.put("button_m2", InputMappingStrings.M2);

        this.buttonIdInputMappingMap.put("button_q", InputMappingStrings.Q);
        this.buttonIdInputMappingMap.put("button_e", InputMappingStrings.E);
        this.buttonIdInputMappingMap.put("button_r", InputMappingStrings.R);
        this.buttonIdInputMappingMap.put("button_space", InputMappingStrings.SPACE);

        this.inputManager.deleteMapping(SimpleApplication.INPUT_MAPPING_EXIT);

        for (String inputMapping : this.buttonIdInputMappingMap.values()) {
            Trigger trigger = this.loadInput(inputMapping).trigger;
            this.inputManager.addMapping(inputMapping, trigger);
        }
    }

    public void beginInput() {
    }

    public void endInput() {
    }

    public void onJoyAxisEvent(JoyAxisEvent evt) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void onJoyButtonEvent(JoyButtonEvent evt) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void onMouseMotionEvent(MouseMotionEvent evt) {
    }

    public void onMouseButtonEvent(MouseButtonEvent evt) {
        if (this.currentInputName == null || this.currentKeyButton == null) {
            return;
        }
        this.inputManager.removeRawInputListener(this);

        final MouseButtonTrigger trigger = new MouseButtonTrigger(evt.getButtonIndex());
        this.inputManager.deleteMapping(this.currentInputName);
        this.inputManager.addMapping(this.currentInputName, trigger);
        this.saveInput(this.currentInputName, trigger);

        try {
            this.app.getContext().getSettings().save(ClientMain.PREFERENCES_KEY);
        } catch (BackingStoreException ex) {
            Logger.getLogger(KeySetter.class.getName()).log(Level.SEVERE, "Failed to save settings", ex);
        }

        this.app.enqueue(new Callable<Void>() {
            public Void call() throws Exception {
                if (trigger.getMouseButton() == 0) {
                    KeySetter.this.currentKeyButton.setText("Mouse Left");
                } else if (trigger.getMouseButton() == 1) {
                    KeySetter.this.currentKeyButton.setText("Mouse Right");
                } else if (trigger.getMouseButton() == 2) {
                    KeySetter.this.currentKeyButton.setText("Mouse Middle");
                } else {
                    KeySetter.this.currentKeyButton.setText("Mouse Strange");
                }

                KeySetter.this.currentKeyButton = null;
                KeySetter.this.currentInputName = null;

                return null;
            }
        });

        evt.setConsumed();
    }

    public void onKeyEvent(KeyInputEvent evt) {
        if (this.currentInputName == null || this.currentKeyButton == null) {
            return;
        }
        if (evt.getKeyCode() == KeyInput.KEY_ESCAPE) {
            return;
        }
        this.inputManager.removeRawInputListener(this);

        KeyTrigger trigger = new KeyTrigger(evt.getKeyCode());

        this.inputManager.deleteMapping(this.currentInputName);
        this.inputManager.addMapping(this.currentInputName, trigger);
        this.saveInput(this.currentInputName, trigger);
        try {
            this.app.getContext().getSettings().save(ClientMain.PREFERENCES_KEY);
        } catch (BackingStoreException ex) {
            Logger.getLogger(KeySetter.class.getName()).log(Level.SEVERE, "Failed to save settings", ex);
        }
        final String keyChar = Character.toString(evt.getKeyChar()).toUpperCase();
        evt.setConsumed();
        this.app.enqueue(new Callable<Void>() {
            public Void call() throws Exception {
                KeySetter.this.currentKeyButton.setText(keyChar);

                KeySetter.this.currentKeyButton = null;
                KeySetter.this.currentInputName = null;
                return null;
            }
        });
    }

    public void onTouchEvent(TouchEvent evt) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void bind(Nifty nifty, Screen screen) {
        this.nifty = nifty;
        this.screen = screen;

        this.app.enqueue(new Callable<Void>() {
            public Void call() throws Exception {
                for (String buttonName : KeySetter.this.buttonIdInputMappingMap.keySet()) {
                    final String inputMapping = KeySetter.this.buttonIdInputMappingMap.get(buttonName);
                    Button button = KeySetter.this.screen.findNiftyControl(buttonName, Button.class);
                    TriggerPair pair = KeySetter.this.loadInput(inputMapping);
                    if (pair.isKeyboard) {
                        KeyTrigger keyTrigger = (KeyTrigger) pair.trigger;
                        String character = KeySetter.keyNames.getName(keyTrigger.getKeyCode());
                        button.setText(character);
                    } else {
                        MouseButtonTrigger mouseTrigger = (MouseButtonTrigger) pair.trigger;
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
                return null;
            }
        });
    }

    public void onStartScreen() {
    }

    public void onEndScreen() {
        this.inputManager.removeRawInputListener(this);
    }

    public void waitAndSetKey(final String buttonId, final String inputName) {
        this.currentKeyButton = this.screen.findNiftyControl(buttonId, Button.class);
        this.currentInputName = inputName;
        this.inputManager.addRawInputListener(this);
        this.app.enqueue(new Callable<Void>() {
            public Void call() throws Exception {
                currentKeyButton.setText("Press esc to cancel");
                return null;
            }
        });

    }

    public void gotoMenu(final String menu) {
        this.app.enqueue(new Callable<Void>() {
            public Void call() throws Exception {
                KeySetter.this.nifty.gotoScreen(menu);
                return null;
            }
        });
    }

    private TriggerPair loadInput(final String inputMappingString) {
        final String inputString = this.app.getContext().getSettings().getString(inputMappingString);
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

    private void saveInput(final String inputMappingString, KeyTrigger trigger) {
        final String inputString = "keyboard::" + Integer.toString(trigger.getKeyCode());
        this.app.getContext().getSettings().putString(inputMappingString, inputString);
    }

    private void saveInput(final String inputMappingString, MouseButtonTrigger trigger) {
        final String inputString = "mouse::" + Integer.toString(trigger.getMouseButton());
        this.app.getContext().getSettings().putString(inputMappingString, inputString);
    }
}

class TriggerPair {

    public final boolean isKeyboard;
    public final Trigger trigger;

    public TriggerPair(boolean isKeyboard, Trigger trigger) {
        this.isKeyboard = isKeyboard;
        this.trigger = trigger;
    }
}