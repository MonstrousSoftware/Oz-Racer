package com.monstrous.canyonracer.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.monstrous.canyonracer.Settings;
import com.monstrous.canyonracer.World;
import com.monstrous.canyonracer.screens.GameScreen;
import com.monstrous.canyonracer.screens.Main;

public class GUI implements Disposable {

    public Stage stage;
    public Skin skin;
    public Skin debugSkin;
    private final GameScreen screen;
    private SettingsWindow settingsWindow;
    public LeaderBoardTable leaderBoard;
    private Label fps;
    private Label speed;
    private Label time;
    private ProgressBar nitro;
    private ProgressBar health;
    private final Array<Label> messages;
    private boolean showingScores;


    public GUI( GameScreen screen ) {
        this.screen = screen;

        skin = Main.assets.skin;
        debugSkin = Main.assets.debugSkin;
        stage = new Stage(new ScreenViewport());
        messages = new Array<>();
        addActors();
    }

    private void addActors(){
        stage.clear();
        settingsWindow = new SettingsWindow("Tweak Settings", Main.assets.debugSkin, screen);
        showDebugMenu(Settings.settingsMenu);

        time = new Label("0.00", skin);
        speed = new Label("-", skin);
        nitro = new ProgressBar(0,100, 1, true, skin);
        health = new ProgressBar(0,100, 1, true, skin, "health");
        health.setValue(75);

        Table table = new Table();
        table.setFillParent(true);
        table.add();
        table.add(time).width(300).top().right().row();
        table.add(nitro).pad(150,20,20,20).left().expand();
        table.add(health).pad(150,20,20,20).right().row();
        table.add();
        table.add(speed).width(500).bottom().right();
        stage.addActor(table);

        fps = new Label("0", debugSkin);

        Table table2 = new Table();
        table2.setFillParent(true);
        table2.add(fps).top().left().expand();
        stage.addActor(table2);

        showingScores = false;
    }

    public void showDebugMenu(boolean mode){
        if(mode)
            stage.addActor(settingsWindow);
        else
            settingsWindow.remove();
    }

    public void showScores(){
        if(showingScores)
            return;
        showingScores = true;

        leaderBoard = new LeaderBoardTable( Main.assets.skin, screen.world.leaderBoard);

        float x = 30;
        float y = stage.getHeight() - (100+leaderBoard.getHeight());
        float w = leaderBoard.getWidth();
        leaderBoard.addAction(Actions.sequence(Actions.moveTo(-(x+w),y), Actions.delay(0.5f), Actions.moveTo(x,y, 1f, Interpolation.swingIn)));

        stage.addActor(leaderBoard);
    }

    public void hideScores() {
        if(!showingScores)
            return;
        showingScores = false;
        float y = stage.getHeight() - (100+leaderBoard.getHeight());
        float w = leaderBoard.getWidth();
        leaderBoard.addAction(Actions.sequence(Actions.moveTo(-w, y, 1f, Interpolation.swingOut), Actions.removeActor()));
    }

    public void showMessage(String msg, float yRel, float delay, float deleteDelay){
        Label label = new Label(msg, skin);
        float w = label.getWidth();

        float x = stage.getWidth()/2;
        float y = stage.getHeight()*yRel;
        label.addAction(Actions.sequence(Actions.moveTo(-(x+w),y), Actions.delay(delay), Actions.moveTo(x-w/2f,y, 1f, Interpolation.bounceIn),
            Actions.delay(deleteDelay), Actions.fadeOut( 0.5f), Actions.removeActor()));

        stage.addActor(label);
        messages.add(label);
    }

    public void clearMessages(){
        for(Label msg : messages)
            msg.addAction(Actions.sequence(Actions.fadeOut(1f), Actions.removeActor()));
    }

    private float updateTimer;

    public void render( float deltaTime ) {
        updateTimer -= deltaTime;
        if (updateTimer < 0) {  // don't update each frame, but every so often
            updateTimer = 0.2f;

            // debug stuff
            if (Settings.showFPS)
                fps.setText("FPS: " + Gdx.graphics.getFramesPerSecond());
            else
                fps.setText("");

            // game stuff
            speed.setText("speed: " + (int) screen.world.playerController.getSpeed());
            time.setText(World.raceTimeString); // may be empty string before the race
            nitro.setValue(World.nitroLevel);
            health.setValue(World.healthPercentage);
        }
        stage.act(deltaTime);
        stage.draw();
    }

    public void resize(int width, int height) {
         stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
