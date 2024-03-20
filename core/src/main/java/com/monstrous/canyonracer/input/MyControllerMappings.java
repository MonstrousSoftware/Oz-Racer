package com.monstrous.canyonracer.input;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerMapping;
import de.golfgl.gdx.controllers.mapping.ConfiguredInput;
import de.golfgl.gdx.controllers.mapping.ControllerMappings;

public class MyControllerMappings extends ControllerMappings {

    // define input events you need in the game or menu's and give each a unique code
    public static final int BUTTON_FIRE = 0;
    public static final int BUTTON_JUMP = 1;
    public static final int BUTTON_CROUCH = 2;
    public static final int AXIS_VERTICAL = 3;
    public static final int AXIS_HORIZONTAL = 4;

    public MyControllerMappings() {
        super();
        addConfiguredInput(new ConfiguredInput(ConfiguredInput.Type.button, BUTTON_FIRE));
        addConfiguredInput(new ConfiguredInput(ConfiguredInput.Type.button, BUTTON_JUMP));
        addConfiguredInput(new ConfiguredInput(ConfiguredInput.Type.button, BUTTON_CROUCH));
        addConfiguredInput(new ConfiguredInput(ConfiguredInput.Type.axisDigital, AXIS_VERTICAL));
        addConfiguredInput(new ConfiguredInput(ConfiguredInput.Type.axisDigital, AXIS_HORIZONTAL));
        commitConfig();
    }

    @Override
    public boolean getDefaultMapping(MappedInputs defaultMapping, Controller controller) {
        ControllerMapping controllerMapping = controller.getMapping();

        defaultMapping.putMapping(new MappedInput(AXIS_VERTICAL, new ControllerAxis(controllerMapping.axisLeftY)));
        defaultMapping.putMapping(new MappedInput(AXIS_HORIZONTAL, new ControllerAxis(controllerMapping.axisLeftX)));
        defaultMapping.putMapping(new MappedInput(BUTTON_FIRE, new ControllerButton(controllerMapping.buttonA)));
        defaultMapping.putMapping(new MappedInput(BUTTON_JUMP, new ControllerButton(controllerMapping.buttonR1)));
        defaultMapping.putMapping(new MappedInput(BUTTON_CROUCH, new ControllerButton(controllerMapping.buttonL1)));
        return true;
    }
}
