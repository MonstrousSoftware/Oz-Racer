package com.monstrous.canyonracer;

public class Settings {
    static public float     ambientLightLevel = 0.75f;
    static public float     shadowLightLevel = 2.0f;
    static public float     shadowBias = 0.003f;
    static public boolean   showLightBox = true;

    static public boolean    cameraInverted = false;
    static public float      cameraDistance = 10f;
    static public float      cameraFieldOfView = 100f;
    static public float      cameraSlerpFactor = 10f; //80f;


    // Racer control
    static public float     dragFactor = 2.6f;
    static public float     acceleration = 360f;
    static public float     turnRate = 490f;
    static public float     bankFactor = 0.25f;
    static public float     maxTurn = 95f;
    static public float     heightLag = 25f;


    // Terrain
    static public float     chunkSize = 4096;
    static public int       chunkCacheSize = 50;
    static public boolean   debugChunkAllocation = false;

    // Graphics settings - aimed at desktop
    static public boolean   multiSamplingFrameBufferAvailable = true;
    static public boolean   useMultiSamplingFrameBuffer = true;
    static public boolean   usePostShader = true;

    static public boolean   particleFX = true;
}
