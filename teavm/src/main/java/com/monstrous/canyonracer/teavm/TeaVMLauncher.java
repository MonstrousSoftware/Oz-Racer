package com.monstrous.canyonracer.teavm;

import com.github.xpenatan.gdx.backends.teavm.TeaApplicationConfiguration;
import com.github.xpenatan.gdx.backends.teavm.TeaApplication;
import com.monstrous.canyonracer.Main;

/**
 * Launches the TeaVM/HTML application.
 */
public class TeaVMLauncher {
    public static void main(String[] args) {
        TeaApplicationConfiguration config = new TeaApplicationConfiguration("canvas");
        // change these to both 0 to use all available space, or both -1 for the canvas size.
        config.width = 0;
        config.height = 0;
        config.antialiasing = true;
        //config.useGL30 = true;
        new TeaApplication(new Main(), config);
    }
}
