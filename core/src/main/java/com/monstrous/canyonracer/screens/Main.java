package com.monstrous.canyonracer.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.monstrous.canyonracer.Assets;
import com.monstrous.canyonracer.screens.GameScreen;


public class Main extends Game {

    public static Assets assets;


    @Override
    public void create() {

        assets = new Assets();

        Gdx.app.log("Gdx version", com.badlogic.gdx.Version.VERSION);
        Gdx.app.log("OpenGL version", Gdx.gl.glGetString(Gdx.gl.GL_VERSION));
        Gdx.app.log("Platform", ""+Gdx.app.getType());

        assets.finishLoading();
        setScreen(new MainMenuScreen( this ));
        //setScreen(new SkyBoxConverter());
    }



    @Override
    public void dispose() {
        assets.dispose();
        super.dispose();
    }
}
