package com.monstrous.canyonracer.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerAdapter;


// to handle game controllers in-game.
// relays events to player controller: button presses are mapped to key presses
// axis movements are passed to the player controller.


public class MyControllerAdapter extends ControllerAdapter {
    private final PlayerController playerController;

    public MyControllerAdapter(PlayerController playerController) {
        super();
        this.playerController = playerController;
    }

    @Override
    public boolean buttonDown(Controller controller, int buttonIndex) {
        Gdx.app.log("controller", "button down: "+buttonIndex);

        // map Dpad to WASD
        if(buttonIndex == controller.getMapping().buttonDpadUp)
            playerController.keyDown(Input.Keys.W);
        if(buttonIndex == controller.getMapping().buttonDpadDown)
            playerController.keyDown(Input.Keys.S);
        if(buttonIndex == controller.getMapping().buttonDpadLeft)
            playerController.keyDown(Input.Keys.A);
        if(buttonIndex == controller.getMapping().buttonDpadRight)
            playerController.keyDown(Input.Keys.D);

        if(buttonIndex == controller.getMapping().buttonL1) // jump
            playerController.keyDown(Input.Keys.UP);
        if(buttonIndex == controller.getMapping().buttonR1) // crouch
            playerController.keyDown(Input.Keys.UP);
        return super.buttonDown(controller, buttonIndex);
    }

    @Override
    public boolean buttonUp(Controller controller, int buttonIndex) {
        //Gdx.app.log("controller", "button up: "+buttonIndex);

        // map Dpad to WASD
        if(buttonIndex == controller.getMapping().buttonDpadUp)
            playerController.keyUp(Input.Keys.W);
        if(buttonIndex == controller.getMapping().buttonDpadDown)
            playerController.keyUp(Input.Keys.S);
        if(buttonIndex == controller.getMapping().buttonDpadLeft)
            playerController.keyUp(Input.Keys.A);
        if(buttonIndex == controller.getMapping().buttonDpadRight)
            playerController.keyUp(Input.Keys.D);

        if(buttonIndex == controller.getMapping().buttonL1)
            playerController.keyUp(Input.Keys.UP);
        if(buttonIndex == controller.getMapping().buttonR1)
            playerController.keyUp(Input.Keys.DOWN);
        return super.buttonUp(controller, buttonIndex);
    }

    @Override
    public boolean axisMoved(Controller controller, int axisIndex, float value) {
        Gdx.app.log("controller", "axis moved: "+axisIndex+" : "+value);

        if(axisIndex == controller.getMapping().axisRightX)     // right stick for steering left/right (X-axis)
            playerController.horizontalAxisMoved(-value);
        if(axisIndex == controller.getMapping().axisLeftY)     // left stick for forward/backwards (Y-axis)
            playerController.verticalAxisMoved(-value);
        if(axisIndex == 5)     // right button
            playerController.boostAxisMoved(value);
        return super.axisMoved(controller, axisIndex, value);
    }

    @Override
    public void connected(Controller controller) {
        Gdx.app.log("controller", "connected");
        super.connected(controller);
    }

    @Override
    public void disconnected(Controller controller) {
        Gdx.app.log("controller", "disconnected");
        super.disconnected(controller);
    }
}
