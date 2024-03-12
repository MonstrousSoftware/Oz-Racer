package com.monstrous.canyonracer.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.monstrous.canyonracer.Main;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) return; // This handles macOS support and helps on Windows.
        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        return new Lwjgl3Application(new Main(), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("CanyonRacer");
        configuration.useVsync(false);

        //// Limits FPS to the refresh rate of the currently active monitor.
        //configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        //// If you remove the above line and set Vsync to false, you can get unlimited FPS, which can be
        //// useful for testing performance, but can also be very stressful to some hardware.
        //// You may also need to configure GPU drivers to fully disable Vsync; this can cause screen tearing.
        configuration.setWindowedMode(1280, 960);
        configuration.setBackBufferConfig(8, 8, 8, 8, 16, 0, 4);
        configuration.setWindowIcon("monstrous128.png", "monstrous64.png", "monstrous32.png", "monstrous16.png");

        // Use OpenGL 4.3 to emulate GL ES 3.1
        configuration.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.GL31, 4,3);

        return configuration;
    }
}
