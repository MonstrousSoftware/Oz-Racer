package com.monstrous.canyonracer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.math.Vector3;
import com.monstrous.canyonracer.CharacterOverlay;
import com.monstrous.canyonracer.GameView;
import com.monstrous.canyonracer.Settings;
import com.monstrous.canyonracer.World;
import com.monstrous.canyonracer.gui.GUI;
import com.monstrous.canyonracer.input.MyControllerAdapter;
import com.monstrous.canyonracer.terrain.TerrainDebug;


public class GameScreen implements Screen {

    private final Main game;
    private Music music;
    private GUI gui;
    public World world;
    public GameView gameView;
    private TerrainDebug terrainDebug;
    private MyControllerAdapter controllerAdapter;
    private Controller currentController;
    private CharacterOverlay overlay;
    private ParticleEffect fire;
    private boolean showFinished = false;
    private boolean showDead = false;
    private boolean wasCollided = false;
    private float startDistance = 500f;
    private int width, height;


    public GameScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {

        music = Main.assets.gameMusic;
        music.setLooping(true);
        if(Settings.musicOn)
            music.play();

        world = new World();
        terrainDebug = new TerrainDebug(world.terrain);
        world.playerController.update(world.racer, world, world.terrain, 0.1f); // force player transform to be updated
        gameView = new GameView(world);
        overlay = new CharacterOverlay();
        gui = new GUI(this);

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

        restart();
        startDistance = 200f;   // shorter cinematic zoom-in sequence after a restart

    }

    private void restart(){
        gui.clearMessages();
        gui.showMessage("READY", .25f, .4f, .5f);
        gui.showMessage("SET", .25f, 1.4f, .5f);
        gui.showMessage("GO!", .25f, 2.4f, 1);
        world.restart();
        gui.showScores();
        showFinished = false;
        showDead = false;
        fire = null;
        Main.assets.START_BEEP.play();

        gameView.cameraController.setDistance(startDistance);
    }



    @Override
    public void render(float deltaTime) {

        // exit with Escape or controller X button
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.Q) ||
                    (currentController != null && currentController.getButton(currentController.getMapping().buttonX))) {
            game.setScreen( new MainMenuScreen(game ));
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            Settings.musicOn = !Settings.musicOn;
            if(Settings.musicOn)
                music.play();
            else
                music.stop();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            Settings.showFPS = !Settings.showFPS;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            Settings.debugRockCollision = !Settings.debugRockCollision;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.T)) {
            Settings.settingsMenu = !Settings.settingsMenu;
            gui.showDebugMenu(Settings.settingsMenu);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R) ||
            (currentController != null && currentController.getButton(currentController.getMapping().buttonA))) {
            restart();
        }

        // Use F11 key to toggle full screen / windowed screen
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            if (!Gdx.graphics.isFullscreen()) {
                width = Gdx.graphics.getWidth();
                height = Gdx.graphics.getHeight();
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
                Gdx.app.log("To fullscreen", "from "+width+" x "+height);
            } else {
                Gdx.graphics.setWindowedMode(width, height);
                Gdx.app.log("To windowed mode", "" + width + " x " + height);
            }
        }

        if( World.finished && !showFinished) {
            gui.showMessage("FINISHED!\n   IN "+World.raceTimeString, .85f, 0, 5);
            gui.showMessage("PRESS [R] TO RESTART", .25f, 2f, 999);
            showFinished = true; // do this only once on finish
            gui.showScores();
        }
        if( World.healthPercentage <= 0 && !showDead) {
            gui.showMessage("RACER DESTROYED", .65f, 0, 5);
            gui.showMessage("PRESS [R] TO RESTART", .25f, 2f, 999);
            fire = gameView.particleEffects.addFire(world.playerPosition);
            showDead = true; // do this only once on finish
            gui.showScores();
        }

        if(World.racing){
            gui.hideScores();
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
            world.collidersView.debugRender(world.playerPosition, world.colliders);
    }


    // nauseating...
    private void adjustCameraFOV( float factor ){
        float fov = Settings.cameraFieldOfView;
        fov += .4f*factor*fov;
        gameView.cameraController.setFOV( fov );
    }

    @Override
    public void resize(int width, int height) {
        gameView.resize(width, height);
        gui.resize(width, height);
        overlay.resize(width, height);
        world.collidersView.resize(width, height);
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
