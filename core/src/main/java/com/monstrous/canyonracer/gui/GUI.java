package com.monstrous.canyonracer.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.monstrous.canyonracer.GameScreen;

public class GUI implements Disposable {

    public Stage stage;
    public Skin skin;
    private GameScreen screen;
    private LightSettingsWindow lightSettings;

    public GUI( GameScreen screen ) {
        this.screen = screen;

        skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
        stage = new Stage(new ScreenViewport());
        addActors();
    }

    private void addActors(){
        lightSettings = new LightSettingsWindow("Light Settings", skin, screen);
        stage.addActor(lightSettings);
    }

    public void render( float deltaTime ){
        stage.act(deltaTime);
        stage.draw();
    }

    public void resize(int width, int height) {
         stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();

    }
}
