package com.monstrous.canyonracer.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.monstrous.canyonracer.Settings;
import com.monstrous.canyonracer.screens.GameScreen;
import com.monstrous.canyonracer.screens.Main;

public class GUI implements Disposable {

    public Stage stage;
    public Skin skin;
    public Skin debugSkin;
    private GameScreen screen;
    private SettingsWindow lightSettings;
    private Label fps;
    private Label speed;
    private Label gameObjects;


    public GUI( GameScreen screen ) {
        this.screen = screen;

        skin = Main.assets.skin;
        debugSkin = Main.assets.debugSkin;
        stage = new Stage(new ScreenViewport());
        addActors();
    }

    private void addActors(){
        lightSettings = new SettingsWindow("Tweak Settings", Main.assets.debugSkin, screen);
        if(Settings.settingsMenu)
            stage.addActor(lightSettings);

        fps = new Label("0", debugSkin);
        speed = new Label("-", skin);
        gameObjects = new Label("0", debugSkin);
        Table table = new Table();
        table.setFillParent(true);
        table.add(fps).top().left().row();
        //table.add(new Label("game objects:", skin)).top().left();
        table.add(gameObjects).top().left();
        table.row();
        table.add(speed).width(500).bottom().right().expand();
        //table.row();
        stage.addActor(table);
    }

    public void render( float deltaTime ){
        if(Settings.showFPS)
            fps.setText( "FPS: " +Gdx.graphics.getFramesPerSecond() );
        else
            fps.setText("");
        speed.setText( "speed: " +(int) screen.world.playerController.speed);
        gameObjects.setText( "game objects: " + screen.gameView.sceneManager.getRenderableProviders().size+"  "+screen.world.getNumGameObjects() );

        stage.act(deltaTime);
        stage.draw();
    }

    public void resize(int width, int height) {
         stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        //skin.dispose();

    }
}
