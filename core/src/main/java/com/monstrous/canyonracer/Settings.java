package com.monstrous.canyonracer;

public class Settings {
    static public float     ambientLightLevel = 0.25f;
    static public float     shadowLightLevel = 1.0f;
    static public float     shadowBias = 0.003f;
    static public boolean   showLightBox = false;


    static public float      cameraSlerpFactor = 80f;


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


    static public boolean   particleFX = true;
}
