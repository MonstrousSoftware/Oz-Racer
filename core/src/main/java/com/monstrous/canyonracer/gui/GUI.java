package com.monstrous.canyonracer.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.monstrous.canyonracer.GameScreen;

public class GUI implements Disposable {

    public Stage stage;
    public Skin skin;
    private GameScreen screen;
    private SettingsWindow lightSettings;
    private Label fps;
    private Label speed;

    public GUI( GameScreen screen ) {
        this.screen = screen;

        skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
        stage = new Stage(new ScreenViewport());
        addActors();
    }

    private void addActors(){
        lightSettings = new SettingsWindow("Light Settings", skin, screen);
        stage.addActor(lightSettings);

        fps = new Label("0", skin);
        speed = new Label("-", skin);
        Table table = new Table();
        table.setFillParent(true);
        table.add(fps).top().left();
        table.row();
        table.add(speed).top().left().expand();
        //table.row();
        stage.addActor(table);
    }

    public void render( float deltaTime ){
        fps.setText( Gdx.graphics.getFramesPerSecond() );
        speed.setText((int) screen.world.playerController.speed);

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
