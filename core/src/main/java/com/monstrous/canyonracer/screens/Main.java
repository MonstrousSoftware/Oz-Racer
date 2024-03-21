package com.monstrous.canyonracer.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controllers;
import com.monstrous.canyonracer.Assets;
import com.monstrous.canyonracer.Settings;
import com.monstrous.canyonracer.input.MyControllerMappings;
import com.monstrous.canyonracer.screens.GameScreen;
import de.golfgl.gdx.controllers.mapping.ControllerToInputAdapter;

import static com.badlogic.gdx.Application.ApplicationType.Desktop;


public class Main extends Game {

    public static Assets assets;
    public ControllerToInputAdapter controllerToInputAdapter;


    @Override
    public void create() {

        assets = new Assets();

        Gdx.app.log("Gdx version", com.badlogic.gdx.Version.VERSION);
        Gdx.app.log("OpenGL version", Gdx.gl.glGetString(Gdx.gl.GL_VERSION));
        Gdx.app.log("Platform", ""+Gdx.app.getType());

        if (Settings.supportControllers) {
            controllerToInputAdapter = new ControllerToInputAdapter(new MyControllerMappings());
            // bind controller events to keyboard keys
            controllerToInputAdapter.addButtonMapping(MyControllerMappings.BUTTON_FIRE, Input.Keys.ENTER);
            controllerToInputAdapter.addAxisMapping(MyControllerMappings.AXIS_VERTICAL, Input.Keys.UP, Input.Keys.DOWN);
            Controllers.addListener(controllerToInputAdapter);
        }

        assets.finishLoading();
        if(Settings.release)
            setScreen(new LogoScreen( this ));
        else
            setScreen( new MainMenuScreen( this ));
        //setScreen(new SkyBoxConverter());
    }



    @Override
    public void dispose() {
        assets.dispose();
        super.dispose();
    }
}
