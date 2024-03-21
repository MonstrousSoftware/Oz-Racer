package com.monstrous.canyonracer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class PreGameScreen extends StdScreenAdapter {

    private Main game;

//    private SpriteBatch batch;
//    private BitmapFont font;
//    private OrthographicCamera cam;
//    private int width, height;
    private float timer = 0.5f;

    private Stage stage;

    public PreGameScreen(Main game) {
        Gdx.app.log("PreGameScreen constructor", "");
        this.game = game;
    }


    @Override
    public void show() {
        Gdx.app.log("PreGameScreen show()", "");

        stage = new Stage(new ScreenViewport());

        Table screenTable = new Table();
        screenTable.setFillParent(true);

        // put table at bottom right
        screenTable.add(new Label("Loading...", Main.assets.skin)).pad(50).bottom().expand();

        // fade in
        screenTable.setColor(1,1,1,0);                   // set alpha to zero
        screenTable.addAction(Actions.fadeIn(1f));           // fade in
        stage.addActor(screenTable);


//        batch = new SpriteBatch();
//        font = new BitmapFont();
//        cam = new OrthographicCamera();
    }


    @Override
    public void render(float deltaTime) {

        timer -= deltaTime;
        if(timer <= 0) {
            game.setScreen(new GameScreen(game));
            return;
        }

        ScreenUtils.clear(Color.BLACK);

        stage.act(deltaTime);
        stage.draw();
//
//        cam.update();
//
//        ScreenUtils.clear(Color.BLACK);
//
//        batch.setProjectionMatrix(cam.combined);
//        batch.begin();
//        font.draw(batch, "Loading...", width/2f, height/8f);
//        batch.end();

    }
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        stage.dispose();
    }

//    @Override
//    public void resize(int w, int h) {
//        this.width = w;
//        this.height = h;
//        Gdx.app.log("PreGameScreen resize()", "");
//        cam.setToOrtho(false, width, height);
//    }
//
//    @Override
//    public void hide() {
//        Gdx.app.log("PreGameScreen hide()", "");
//        dispose();
//    }
//
//    @Override
//    public void dispose() {
//        Gdx.app.log("PreGameScreen dispose()", "");
//        batch.dispose();
//        font.dispose();
//    }

}
