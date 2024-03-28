package com.monstrous.canyonracer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerPowerLevel;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.monstrous.canyonracer.Settings;


// todo handle F11 key presses on web by reflecting the correct state of the fullscreen checkbox


public class OptionsScreen extends MenuScreen {
    private Controller controller;
    private Label controllerLabel;
    private CheckBox fullScreen;

    public OptionsScreen(Main game) {
        super(game);
    }

    @Override
    public void show() {
        super.show();

        if(!Settings.supportControllers)
            return;

        for (Controller controller : Controllers.getControllers()) {
            Gdx.app.log("Controllers", controller.getName());
        }


        controller = Controllers.getCurrent();
        if(controller != null ) {

            Gdx.app.log("current controller", controller.getName());
            Gdx.app.log("unique id", controller.getUniqueId());
            Gdx.app.log("is connected", "" + controller.isConnected());
            ControllerPowerLevel powerLevel = controller.getPowerLevel();
            Gdx.app.log("power level", "" + powerLevel.toString());
            Gdx.app.log("can vibrate", "" + controller.canVibrate());
            if (controller.canVibrate()) {
                controller.startVibration(500, 1f);
            }
        }
        else
            Gdx.app.log("current controller", "none");
   }

   private void checkControllerChanges() {
       Controller currentController = Controllers.getCurrent();
       if(currentController != controller ) {
           controller = currentController;
           if (controller != null) {

               Gdx.app.log("current controller", controller.getName());
               Gdx.app.log("unique id", controller.getUniqueId());
               Gdx.app.log("is connected", "" + controller.isConnected());
               ControllerPowerLevel powerLevel = controller.getPowerLevel();
               Gdx.app.log("power level", "" + powerLevel.toString());
               Gdx.app.log("can vibrate", "" + controller.canVibrate());
               if (controller.canVibrate()) {
                   controller.startVibration(500, 1f);
               }
           } else
               Gdx.app.log("current controller", "none");

           if(controller != null)
               controllerLabel.setText(controller.getName());
           else
               controllerLabel.setText("None");
       }
   }

    @Override
    protected void rebuild() {
       stage.clear();

       Table screenTable = new Table();
       screenTable.setFillParent(true);


       fullScreen = new CheckBox("Full Screen (F11)", skin);
       fullScreen.setChecked(Settings.fullScreen);

       CheckBox fps = new CheckBox("Show FPS (F)", skin);
       fps.setChecked(Settings.showFPS);

       CheckBox particles = new CheckBox("Particle effects", skin);
       particles.setChecked(Settings.particleFX);

       CheckBox music = new CheckBox("Music (M)", skin);
       music.setChecked(Settings.musicOn);

       CheckBox settingsMenu = new CheckBox("Tweaker Menu (T)", skin);
       settingsMenu.setChecked(Settings.settingsMenu);

       TextButton done = new TextButton("Done", skin);

       controllerLabel = new Label("None", skin, "small");
       if(controller != null)
           controllerLabel.setText(controller.getName());


       int pad = 10;

       screenTable.add(new Label("Options", skin)).pad(2*pad).center().row();
       screenTable.add(fullScreen).pad(pad).left().row();
       screenTable.add(fps).pad(pad).left().row();
       screenTable.add(particles).pad(pad).left().row();
       screenTable.add(music).pad(pad).left().row();
       screenTable.add(settingsMenu).pad(pad).left().row();


       screenTable.add(done).pad(40,5,80,5).row();
       if(Settings.supportControllers) {
           Table ct = new Table();
           ct.add(new Label("Controller = ", skin, "small"));
           ct.add(controllerLabel);
           ct.pack();
           screenTable.add(ct).bottom().pad(2*pad);
       }


       screenTable.pack();
       screenTable.validate();

       screenTable.setColor(1,1,1,0);                   // set alpha to zero
       screenTable.addAction(Actions.fadeIn(1f));           // fade in

       stage.addActor(screenTable);

       // set up for keyboard/controller navigation
       if(Settings.supportControllers) {
           // add menu items in order
           stage.clearFocusableActors();
           stage.addFocusableActor(fullScreen);
           stage.addFocusableActor(fps);
           stage.addFocusableActor(particles);
           stage.addFocusableActor(music);
           stage.addFocusableActor(settingsMenu);
           stage.addFocusableActor(done);
           stage.setFocusedActor(fullScreen);
           stage.setEscapeActor(done);
           super.focusActor(fullScreen);    // highlight focused actor
       }

       // note: if user presses F11 this will force a resize and hence a rebuild which will update this checkbox

       fullScreen.addListener(new ChangeListener() {
           @Override
           public void changed(ChangeEvent event, Actor actor) {
               playSelectNoise();
               Settings.fullScreen = fullScreen.isChecked();
               if(Settings.fullScreen)
                   enterFullScreen();
               else
                   leaveFullScreen();
           }
       });

       fps.addListener(new ChangeListener() {
           @Override
           public void changed(ChangeEvent event, Actor actor) {
               playSelectNoise();
               Settings.showFPS = fps.isChecked();
           }
       });

       music.addListener(new ChangeListener() {
           @Override
           public void changed(ChangeEvent event, Actor actor) {
               playSelectNoise();
               Settings.musicOn = music.isChecked();
           }
       });

       settingsMenu.addListener(new ChangeListener() {
           @Override
           public void changed(ChangeEvent event, Actor actor) {
               playSelectNoise();
               Settings.settingsMenu = settingsMenu.isChecked();
           }
       });


       particles.addListener(new ChangeListener() {
           @Override
           public void changed(ChangeEvent event, Actor actor) {
               playSelectNoise();
               Settings.particleFX = particles.isChecked();
           }
       });

       done.addListener(new ClickListener() {
           @Override
           public void clicked(InputEvent event, float x, float y) {
               super.clicked(event, x, y);
               playSelectNoise();
//               if(gameScreen != null)
//                game.setScreen(new PauseMenuScreen( game, gameScreen ));
//               else
                game.setScreen(new MainMenuScreen( game ));
           }
       });

   }

    private static final Color bg = new Color(0xff9f8eff);

    @Override
    public void render(float delta) {
        if(Settings.supportControllers)
            checkControllerChanges();
        ScreenUtils.clear(bg);
        super.render(delta);
    }


}
