package com.monstrous.canyonracer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.monstrous.canyonracer.screens.Main;


// Project narrator character over the game screen to give intro etc.
// todo more dialogue

public class CharacterOverlay {
    public Stage stage;


    public CharacterOverlay() {
        stage = new Stage(new ScreenViewport());
        addActors();
    }


    private void addActors(){
        Image narrator = new Image(Main.assets.character);

        TextButton dialog = new TextButton("G'day! Welcome to the outback! My name is Krazy Kat.\n\nAre you ready for some high speed racing?\nUse W for throttle and A and D for steering.\n"+
            "Press SPACE for nitro boost.\nStay clear of the rocks, mate!\n\n[CLICK TO CONTINUE]", Main.assets.debugSkin);
        dialog.getStyle().fontColor.set(Color.BLACK);

        Table screenTable = new Table();
        screenTable.setFillParent(true);


        // combine image and text button in one table
        Table charTable = new Table();
        charTable.add(dialog).pad(20).row();
        charTable.add(narrator);
        charTable.pack();

        // put table at bottom right
        screenTable.add(charTable).bottom().right().expand();

        // fade in
        screenTable.setColor(1,1,1,0);                   // set alpha to zero
        screenTable.addAction(Actions.sequence(Actions.delay(1f), Actions.fadeIn(3f)));           // fade in
        stage.addActor(screenTable);

        dialog.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                Main.assets.MENU_CLICK.play();
                screenTable.addAction( Actions.fadeOut(0.5f));           // fade out
                dialog.setVisible(false);
            }
        });

    }

    public void render( float deltaTime ){
        stage.act(deltaTime);
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        stage.dispose();
    }
}
