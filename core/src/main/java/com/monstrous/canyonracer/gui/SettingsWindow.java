package com.monstrous.canyonracer.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.monstrous.canyonracer.screens.GameScreen;
import com.monstrous.canyonracer.Settings;

// Debug window for some in-game tweaking, e.g. light level, camera distance, etc.
// Typically, these modify members of the Settings class.

public class SettingsWindow extends Window {

    private final Skin skin;
    private final GameScreen screen;

    public SettingsWindow(String title, Skin skin, GameScreen screen) {
        super(title, skin);
        this.skin = skin;
        this.screen = screen;
        rebuild();
    }

    private void rebuild() {


        final Slider ALslider = new Slider(0.0f, 5.0f, 0.05f, false, skin);
        ALslider.setValue(Settings.ambientLightLevel);
        ALslider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Settings.ambientLightLevel = ALslider.getValue();
                Gdx.app.log("ambientLightLevel", ""+Settings.ambientLightLevel);
                screen.gameView.adjustLighting();
            }
        });
        final Label ALlabel = new Label("ambient light", skin);
        add(ALslider); add(ALlabel);        row();

        final Slider SLslider = new Slider(0.0f, 5.0f, 0.05f, false, skin);
        SLslider.setValue(Settings.shadowLightLevel);
        SLslider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Settings.shadowLightLevel = SLslider.getValue();
                Gdx.app.log("shadowLightLevel", ""+Settings.shadowLightLevel);
                screen.gameView.adjustLighting();
            }
        });
        final Label SLlabel = new Label("shadow light", skin);
        add(SLslider); add(SLlabel);        row();

        final Slider SBslider = new Slider(0.0f, 0.01f, 0.0001f, false, skin);
        SBslider.setValue(Settings.shadowBias);
        SBslider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Settings.shadowBias = SBslider.getValue();
                Gdx.app.log("bias", ""+Settings.shadowBias);
                screen.gameView.adjustLighting();
            }
        });
        final Label SBlabel = new Label("shadow bias", skin);
        add(SBslider); add(SBlabel);        row();

        final CheckBox LBcheckbox = new CheckBox("show light box", skin);
        LBcheckbox.setChecked(Settings.showLightBox);
        LBcheckbox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Settings.showLightBox = LBcheckbox.isChecked();
            }
        });
        add(LBcheckbox).left();        row();

        final CheckBox TCcheckbox = new CheckBox("terrain chunks allocation", skin);
        TCcheckbox.setChecked(Settings.debugChunkAllocation);
        TCcheckbox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Settings.debugChunkAllocation = TCcheckbox.isChecked();
            }
        });
        add(TCcheckbox).left();        row();

        final CheckBox RCcheckbox = new CheckBox("show colliders map (P)", skin);
        RCcheckbox.setChecked(Settings.debugRockCollision);
        RCcheckbox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Settings.debugRockCollision = RCcheckbox.isChecked();
            }
        });
        add(RCcheckbox).left();        row();

        final CheckBox PostCheckbox = new CheckBox("post-processing shader", skin);
        PostCheckbox.setChecked(Settings.usePostShader);
        PostCheckbox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Settings.usePostShader = PostCheckbox.isChecked();
            }
        });
        add(PostCheckbox).left();        row();

        if(Settings.multiSamplingFrameBufferAvailable) {
            final CheckBox AAcheckbox = new CheckBox("multi-sample frame buffer", skin);
            AAcheckbox.setChecked(Settings.useMultiSamplingFrameBuffer);
            AAcheckbox.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    Settings.useMultiSamplingFrameBuffer = AAcheckbox.isChecked();
                }
            });
            add(AAcheckbox).left();
        }
        else
            add(new Label("multi-sample frame buffer not available", skin)).left();
        row();

        final CheckBox CIcheckbox = new CheckBox("camera inverted", skin);
        CIcheckbox.setChecked(Settings.cameraInverted);
        CIcheckbox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Settings.cameraInverted = CIcheckbox.isChecked();
                screen.gameView.cameraController.setCameraUpSideDown(Settings.cameraInverted);

            }
        });
        add(CIcheckbox).left();        row();

        final Slider FOVslider = new Slider(20.0f, 140f, 5f, false, skin);
        FOVslider.setValue(Settings.cameraFieldOfView);
        FOVslider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Settings.cameraFieldOfView = FOVslider.getValue();
                Gdx.app.log("camera FOV", ""+Settings.cameraFieldOfView);
                screen.gameView.getCamera().fieldOfView = Settings.cameraFieldOfView;
            }
        });
        final Label FOVlabel = new Label("cam field of view", skin);
        add(FOVslider); add(FOVlabel);        row();

        final Slider CDslider = new Slider(5.0f, 200f, 2f, false, skin);
        CDslider.setValue(Settings.cameraDistance);
        CDslider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Settings.cameraDistance = CDslider.getValue();
                Gdx.app.log("camera distance", ""+Settings.cameraDistance);
                screen.gameView.cameraController.setDistance(Settings.cameraDistance);
            }
        });
        final Label CDlabel = new Label("cam distance", skin);
        add(CDslider); add(CDlabel);        row();


        pack();

    }
}
