package com.monstrous.canyonracer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;


// base class for screens to support full-size toggle with F11

public class StdScreenAdapter extends ScreenAdapter {
    private static int width;           // static so it is retained between different screens
    private static int height;

    @Override
    public void render(float delta) {
        super.render(delta);

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
    }
}
