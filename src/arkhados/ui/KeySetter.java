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
import arkhados.Globals;
import arkhados.util.InputMappingStrings;
import arkhados.util.PlayerDataStrings;
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
import de.lessvoid.nifty.controls.CheckBox;
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
    private HashMap<String, String> buttonIdInputMappingMap = new HashMap<>(10);

    public KeySetter(Application app, InputManager inputManager) {
        this.app = app;
        this.inputManager = inputManager;

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

        inputManager.deleteTrigger(SimpleApplication.INPUT_MAPPING_EXIT,
                new KeyTrigger(KeyInput.KEY_ESCAPE));

        inputManager.addMapping(SimpleApplication.INPUT_MAPPING_EXIT,
                new KeyTrigger(KeyInput.KEY_F4));

        for (String inputMapping : buttonIdInputMappingMap.values()) {
            Trigger trigger = loadInput(inputMapping).trigger;
            inputManager.addMapping(inputMapping, trigger);
        }
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
        saveInput(currentInputName, trigger);

        try {
            app.getContext().getSettings().save(ClientMain.PREFERENCES_KEY);
        } catch (BackingStoreException ex) {
            Logger.getLogger(KeySetter.class.getName())
                    .log(Level.WARNING, "Failed to save settings", ex);
        }

        this.app.enqueue(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
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
            }
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
        saveInput(currentInputName, trigger);
        try {
            app.getContext().getSettings().save(ClientMain.PREFERENCES_KEY);
        } catch (BackingStoreException ex) {
            Logger.getLogger(KeySetter.class.getName())
                    .log(Level.WARNING, "Failed to save settings", ex);
        }
        final String keyChar =
                Character.toString(evt.getKeyChar()).toUpperCase();
        evt.setConsumed();
        app.enqueue(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                currentKeyButton.setText(keyChar);

                currentKeyButton = null;
                currentInputName = null;
                return null;
            }
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

        app.enqueue(new Callable<Void>() {
            public Void call() throws Exception {
                for (String buttonName : buttonIdInputMappingMap.keySet()) {
                    String inputMapping =
                            buttonIdInputMappingMap.get(buttonName);
                    Button button =
                            screen.findNiftyControl(buttonName, Button.class);
                    TriggerPair pair = loadInput(inputMapping);
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
                        .getBoolean(PlayerDataStrings.COMMAND_MOVE_INTERRUPTS);
                CheckBox cbox_moveInterrupts = screen.findNiftyControl(
                        "cbox_move_interrupts", CheckBox.class);
                cbox_moveInterrupts.setChecked(moveInterrupts);

                return null;
            }
        });
    }

    @Override
    public void onStartScreen() {
    }

    @Override
    public void onEndScreen() {
        inputManager.removeRawInputListener(this);
    }

    public void waitAndSetKey(final String buttonId, final String inputName) {
        currentKeyButton = screen.findNiftyControl(buttonId, Button.class);
        currentInputName = inputName;
        inputManager.addRawInputListener(this);
        app.enqueue(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                currentKeyButton.setText("Press esc to cancel");
                return null;
            }
        });

    }

    public void checked(final String cboxId) {
        final CheckBox cbox = screen.findNiftyControl(cboxId, CheckBox.class);
        if (cbox == null) {
            return;
        }
        app.enqueue(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                cbox.toggle();

                if ("cbox_move_interrupts".equals(cboxId)) {
                    app.getContext().getSettings().putBoolean(
                            PlayerDataStrings.COMMAND_MOVE_INTERRUPTS,
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
            }
        });
    }

    public void gotoMenu(final String menu) {
        app.enqueue(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                nifty.gotoScreen(menu);
                return null;
            }
        });
    }

    private TriggerPair loadInput(final String inputMappingString) {
        String inputString = app.getContext().getSettings()
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

    private void saveInput(String inputMappingString, KeyTrigger trigger) {
        String inputString = "keyboard::"
                + Integer.toString(trigger.getKeyCode());
        app.getContext().getSettings()
                .putString(inputMappingString, inputString);
    }

    private void saveInput(String inputMappingString, MouseButtonTrigger trigger) {
        String inputString = "mouse::"
                + Integer.toString(trigger.getMouseButton());
        app.getContext().getSettings()
                .putString(inputMappingString, inputString);
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