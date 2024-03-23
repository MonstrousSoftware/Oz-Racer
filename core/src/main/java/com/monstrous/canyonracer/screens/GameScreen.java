package com.monstrous.canyonracer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.Vector3;
import com.monstrous.canyonracer.CharacterOverlay;
import com.monstrous.canyonracer.GameView;
import com.monstrous.canyonracer.Settings;
import com.monstrous.canyonracer.World;
import com.monstrous.canyonracer.gui.GUI;
import com.monstrous.canyonracer.input.MyControllerAdapter;
import com.monstrous.canyonracer.terrain.TerrainDebug;


public class GameScreen implements Screen {

    private Main game;
    private Music music;
    private GUI gui;
    public World world;
    public GameView gameView;
    private Vector3 target;
    private int changes = 0;
    private TerrainDebug terrainDebug;
    private MyControllerAdapter controllerAdapter;
    private Controller currentController;
    private CharacterOverlay overlay;
    private ParticleEffect fire;
    private boolean showFinished = false;
    private boolean showDead = false;
    private boolean wasCollided = false;


    public GameScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {


        music = Main.assets.gameMusic;
        music.setLooping(true);
        if(Settings.musicOn)
            music.play();

        gui = new GUI(this);

        world = new World();
        terrainDebug = new TerrainDebug(world.terrain);
        world.playerController.update(world.racer, world, world.terrain, 0.1f); // force player transform to be updated

        gameView = new GameView(world);
        overlay = new CharacterOverlay();

        // controller
        if (Settings.supportControllers) {
            currentController = Controllers.getCurrent();
            if (currentController != null) {
                Gdx.app.log("current controller", currentController.getName());
                controllerAdapter = new MyControllerAdapter(world.playerController);
                // we define a listener that listens to all controllers, in case the current controller gets disconnected and reconnected
                Controllers.removeListener(game.controllerToInputAdapter);          // remove adapter for menu navigation with controller
                Controllers.addListener(controllerAdapter);                         // add adapter for game play with controller
            } else
                Gdx.app.log("current controller", "none");
        }

        // input multiplexer to input to GUI and to cam controller
        InputMultiplexer im = new InputMultiplexer();
        Gdx.input.setInputProcessor(im);
        //camController = new CameraInputController(gameView.getCamera());

        im.addProcessor(gui.stage);
        im.addProcessor(overlay.stage);
        im.addProcessor(gameView.cameraController);
        im.addProcessor(world.playerController);

        target = new Vector3();

        restart();

    }

    private void restart(){
        gui.clearMessages();
        gui.showMessage("READY", .5f, 1f, .5f);
        gui.showMessage("SET", .5f, 2f, .5f);
        gui.showMessage("GO!", .5f, 3f, 1);
        world.restart();
        showFinished = false;
        showDead = false;
        fire = null;
        gameView.cameraController.setDistance(500f);
    }



    @Override
    public void render(float deltaTime) {
        // exit with Escape or controller X button
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) ||
                    (currentController != null && currentController.getButton(currentController.getMapping().buttonX))) {
            game.setScreen( new MainMenuScreen(game ));
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            markSpot(world.playerPosition);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R) ||
            (currentController != null && currentController.getButton(currentController.getMapping().buttonA))) {
            restart();
        }

        if( World.finished && !showFinished) {
            gui.showMessage("FINISHED!\n   IN "+World.raceTimeString, .75f, 0, 5);
            gui.showMessage("PRESS [R] TO RESTART", .25f, 2f, 999);
            showFinished = true; // do this only once on finish
        }
        if( World.healthPercentage <= 0 && !showDead) {
            gui.showMessage("RACER DESTROYED", .75f, 0, 5);
            gui.showMessage("PRESS [R] TO RESTART", .25f, 2f, 999);
            fire = gameView.particleEffects.addFire(world.playerPosition);
            showDead = true; // do this only once on finish
        }


        if(!wasCollided && world.collided )
            gameView.cameraController.startCameraShake();
        wasCollided =  world.collided;


        world.terrain.update(gameView.getCamera());     // update terrain to camera position
        world.update(deltaTime);

        if(fire != null )
            fire.setTransform(world.racer.getScene().modelInstance.transform);

        adjustCameraFOV( world.playerController.boostFactor );

        gameView.render( deltaTime );

        terrainDebug.debugRender(world.playerPosition, gameView.getCamera().position);

        if(Settings.showNarrator)
            overlay.render(deltaTime);
        gui.render(deltaTime);
        if(Settings.debugRockCollision)
            world.rocks.debugRender(world.playerPosition);
    }


    // nauseating...
    private void adjustCameraFOV( float factor ){
        float fov = Settings.cameraFieldOfView;
        fov += .4f*factor*fov;
        gameView.cameraController.setFOV( fov );
    }

    private void markSpot( Vector3 spot ){
        Vector3 fwd = world.playerController.forwardDirection;
        float angle = (float)Math.atan2(fwd.x, fwd.z);

        Gdx.app.log("marker", ""+spot+" angle:"+ angle*180f/Math.PI);
    }

    @Override
    public void resize(int width, int height) {
        gameView.resize(width, height);
        gui.resize(width, height);
        overlay.resize(width, height);
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
        music.stop();
        if(currentController != null) {
            Controllers.removeListener(controllerAdapter);          // remove adapter for game play with controller
            Controllers.addListener(game.controllerToInputAdapter); // adapter for menu navigation with controller
        }
    }

    @Override
    public void dispose() {
        gui.dispose();
        world.dispose();
        gameView.dispose();
        terrainDebug.dispose();
        overlay.dispose();
    }

}
