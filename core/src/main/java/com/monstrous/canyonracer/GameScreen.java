package com.monstrous.canyonracer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.monstrous.canyonracer.gui.GUI;



public class GameScreen implements Screen {

    private CameraInputController camController;
    private GUI gui;
    private World world;
    public GameView gameView;

    @Override
    public void show() {

        gui = new GUI(this);

        world = new World();

        gameView = new GameView(world);

        // input multiplexer to input to GUI and to cam controller
        InputMultiplexer im = new InputMultiplexer();
        Gdx.input.setInputProcessor(im);
        camController = new CameraInputController(gameView.getCamera());
        im.addProcessor(gui.stage);
        im.addProcessor(camController);
    }

    @Override
    public void render(float deltaTime) {
        world.update(deltaTime);

        gameView.render( deltaTime);
        gui.render(deltaTime);
    }

    @Override
    public void resize(int width, int height) {
        gameView.resize(width, height);
        gui.resize(width, height);
    }

    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void hide() {
        // This method is called when another screen replaces this one.
    }

    @Override
    public void dispose() {
        gui.dispose();
        world.dispose();
        gameView.dispose();
    }

}
