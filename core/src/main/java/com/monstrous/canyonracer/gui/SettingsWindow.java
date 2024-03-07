package com.monstrous.canyonracer.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.monstrous.canyonracer.GameScreen;
import com.monstrous.canyonracer.Settings;

public class SettingsWindow extends Window {

    private Skin skin;
    private GameScreen screen;

    public SettingsWindow(String title, Skin skin, GameScreen screen) {
        super(title, skin);
        this.skin = skin;
        this.screen = screen;
        rebuild();
    }

    private void rebuild() {


        final Slider ALslider = new Slider(0.0f, 1.0f, 0.05f, false, skin);
        ALslider.setValue(Settings.ambientLightLevel);
        //final Label label2 = new Label(String.valueOf(Settings.ambientLightLevel), skin);
        ALslider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Settings.ambientLightLevel = ALslider.getValue();
                Gdx.app.log("ambientLightLevel", ""+Settings.ambientLightLevel);
                screen.gameView.buildEnvironment();

                //label2.setText(String.valueOf(Settings.ambientLightLevel));
            }
        });
        final Label ALlabel = new Label("ambient light", skin);

        final Slider SLslider = new Slider(0.0f, 1.0f, 0.05f, false, skin);
        SLslider.setValue(Settings.shadowLightLevel);
        SLslider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Settings.shadowLightLevel = SLslider.getValue();
                Gdx.app.log("shadowLightLevel", ""+Settings.shadowLightLevel);
                screen.gameView.buildEnvironment();
            }
        });
        final Label SLlabel = new Label("shadow light", skin);

        final Slider SBslider = new Slider(0.0f, 0.1f, 0.0005f, false, skin);
        SBslider.setValue(Settings.shadowBias);
        SBslider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Settings.shadowBias = SBslider.getValue();
                Gdx.app.log("bias", ""+Settings.shadowBias);
                screen.gameView.buildEnvironment();
            }
        });
        final Label SBlabel = new Label("shadow bias", skin);

        final CheckBox LBcheckbox = new CheckBox("show light box", skin);
        LBcheckbox.setChecked(Settings.showLightBox);
        LBcheckbox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Settings.showLightBox = LBcheckbox.isChecked();
            }
        });

        final CheckBox TCcheckbox = new CheckBox("terrain chunks allocation", skin);
        TCcheckbox.setChecked(Settings.debugChunkAllocation);
        TCcheckbox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Settings.debugChunkAllocation = TCcheckbox.isChecked();
            }
        });

        add(ALslider); add(ALlabel);        row();
        add(SLslider); add(SLlabel);        row();
        add(SBslider); add(SBlabel);        row();
        add(LBcheckbox);        row();
        add(TCcheckbox);        row();
        pack();

    }
}
