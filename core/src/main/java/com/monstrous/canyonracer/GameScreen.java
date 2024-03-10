package com.monstrous.canyonracer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.Vector3;
import com.monstrous.canyonracer.gui.GUI;
import com.monstrous.canyonracer.terrain.TerrainDebug;


public class GameScreen implements Screen {

    //private CameraInputController camController;
    private GUI gui;
    public World world;
    public GameView gameView;
    private Vector3 target;
    private int changes = 0;
    private TerrainDebug terrainDebug;

    @Override
    public void show() {

        gui = new GUI(this);

        world = new World();
        terrainDebug = new TerrainDebug(world.terrain);

        gameView = new GameView(world);

        // input multiplexer to input to GUI and to cam controller
        InputMultiplexer im = new InputMultiplexer();
        Gdx.input.setInputProcessor(im);
        //camController = new CameraInputController(gameView.getCamera());

        im.addProcessor(gui.stage);
        im.addProcessor(gameView.cameraController);
        im.addProcessor(world.playerController);

        target = new Vector3();
    }

    @Override
    public void render(float deltaTime) {

        world.terrain.update(gameView.getCamera());     // update terrain to camera position
        world.update(deltaTime);

        gameView.render( deltaTime);

        terrainDebug.debugRender(world.playerPosition, gameView.getCamera().position);
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
        terrainDebug.dispose();
    }

}
