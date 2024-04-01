package com.monstrous.canyonracer.screens;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.monstrous.canyonracer.Settings;


public class RegionScreen extends MenuScreen {

    private final Color backgroundColor = new Color(38f/255f, 127f/255f, 102f/255f, 1.0f);

    public RegionScreen(Main game) {
        super(game);
    }


   @Override
   protected void rebuild() {
       stage.clear();

       Table screenTable = new Table();
       screenTable.setFillParent(true);

       Label title = new Label("Monitor Orientation", skin);

       Label intro = new Label("Indicate your region:", skin, "small");

       ButtonGroup<CheckBox> buttonGroup = new ButtonGroup<>();

       CheckBox australia = new CheckBox("Australia", skin);
       australia.setChecked(Settings.cameraInverted);
       buttonGroup.add(australia);

       CheckBox notAustralia = new CheckBox("not Australia", skin);
       notAustralia.setChecked(!Settings.cameraInverted);
       buttonGroup.add(notAustralia);

       TextButton done = new TextButton("Done", skin);

       int pad = 10;

       screenTable.add(title).pad(pad).row();
       screenTable.add(intro).pad(pad).row();
       screenTable.add(australia).pad(10,40,10,10  ).left().row();
       screenTable.add(notAustralia).pad(10,40,10,10).left().row();

       screenTable.add(done).pad(20).row();

       screenTable.pack();
       screenTable.validate();

       screenTable.setColor(1,1,1,0);                   // set alpha to zero
       screenTable.addAction(Actions.fadeIn(1f));           // fade in

       stage.addActor(screenTable);

       // set up for keyboard/controller navigation
       if(Settings.supportControllers) {
           // add menu items in order
           stage.clearFocusableActors();
           stage.addFocusableActor(australia);
           stage.addFocusableActor(notAustralia);
           stage.setFocusedActor(australia);
           stage.setEscapeActor(done);
           super.focusActor(australia);    // highlight focused actor
       }


       australia.addListener(new ChangeListener() {
           @Override
           public void changed(ChangeEvent event, Actor actor) {
               playSelectNoise();
               Settings.cameraInverted = australia.isChecked();
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

    @Override
    public void render(float delta) {
        ScreenUtils.clear(backgroundColor);
        super.render(delta);
    }


}
