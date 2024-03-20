package com.monstrous.canyonracer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class PreGameScreen extends StdScreenAdapter {

    private Main game;

    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera cam;
    private int width, height;
    private float timer = 0.5f;

    public PreGameScreen(Main game) {
        Gdx.app.log("PreGameScreen constructor", "");
        this.game = game;
    }


    @Override
    public void show() {
        Gdx.app.log("PreGameScreen show()", "");

        batch = new SpriteBatch();
        font = new BitmapFont();
        cam = new OrthographicCamera();
    }


    @Override
    public void render(float deltaTime) {
        super.render(deltaTime);

        timer -= deltaTime;
        if(timer <= 0) {
            game.setScreen(new GameScreen(game));
            return;
        }

        cam.update();

        ScreenUtils.clear(Color.BLACK);

        batch.setProjectionMatrix(cam.combined);
        batch.begin();
        font.draw(batch, "Loading...", width/2f, height/8f);
        batch.end();

    }

    @Override
    public void resize(int w, int h) {
        this.width = w;
        this.height = h;
        Gdx.app.log("PreGameScreen resize()", "");
        cam.setToOrtho(false, width, height);
    }

    @Override
    public void hide() {
        Gdx.app.log("PreGameScreen hide()", "");
        dispose();
    }

    @Override
    public void dispose() {
        Gdx.app.log("PreGameScreen dispose()", "");
        batch.dispose();
        font.dispose();
    }

}
