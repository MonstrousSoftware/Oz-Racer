package com.monstrous.canyonracer;

import com.badlogic.gdx.Gdx;

import static com.badlogic.gdx.Application.ApplicationType.Desktop;

public class Settings {
    static public boolean   release = false;
    static public String    version = "v1.04 (March 24, 2024)";

    static public float     ambientLightLevel = 0.75f;
    static public float     shadowLightLevel = 5.0f;
    static public float     shadowBias = 0.00005f;
    static public boolean   showLightBox = false;
    static public boolean   cascadedShadows = (Gdx.app.getType() == Desktop);

    static public boolean    cameraInverted = release;
    static public float      cameraDistance = 10f;
    static public float      cameraFieldOfView = 100f;
//    static public float      cameraSlerpFactor = 10f;

    // Racer control
    static public float     dragFactor = 1.6f;
    static public float     acceleration = 360f;
    static public float     turnRate = 490f;
    static public float     bankFactor = 0.25f;
    static public float     maxTurn = 95f;
    static public float     heightLag = 50f;


    static public float     nitroConsumption = 20f;
    static public float     nitroReplenishment = 10f;

    static public float     collisionDamage = 0.25f;    // multiplied by speed, max health is 100


    // Terrain
    static public float     chunkSize = 4096;
    static public int       chunkCacheSize = 50;
    static public boolean   debugChunkAllocation = false;

    static public boolean   debugRockCollision = false;

    // Graphics settings - aimed at desktop
    static public boolean   multiSamplingFrameBufferAvailable = true;
    static public boolean   useMultiSamplingFrameBuffer = true;
    static public boolean   usePostShader = true;

    static public boolean   particleFX = true;


    static public boolean   supportControllers = (Gdx.app.getType() == Desktop);

    static public boolean   fullScreen = false;
    static public boolean   showFPS = !release;
    static public boolean   musicOn = release;
    static public boolean   settingsMenu = false;

    static public boolean   showNarrator = release;
}
