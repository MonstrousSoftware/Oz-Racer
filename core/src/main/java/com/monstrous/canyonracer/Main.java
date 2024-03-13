package com.monstrous.canyonracer;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    @Override
    public void create() {

        Gdx.app.log("Gdx version", com.badlogic.gdx.Version.VERSION);
        Gdx.app.log("OpenGL version", Gdx.gl.glGetString(Gdx.gl.GL_VERSION));
        Gdx.app.log("Platform", ""+Gdx.app.getType());

        setScreen(new GameScreen());
        //setScreen(new SkyBoxConverter());
    }
}
