package com.monstrous.canyonracer.terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.monstrous.canyonracer.Settings;


import java.util.HashMap;


// The Terrain runs from [0 - Settings.worldSize] in x and in z
// It is subdivided into chunks of size Settings.chunkSize

public class Terrain implements Disposable {


    private HashMap<Integer, TerrainChunk> chunks;   // map of terrain chunk per grid point
    public Array<ModelInstance> instances;          // model instances to be rendered
    private SpriteBatch batch;                      // for debug map view

    public Terrain() {
        Gdx.app.log("terrain", "generate...");

        chunks = new HashMap<>();
        instances = new Array<>();

        int sideLength = (int) (Settings.worldSize / Settings.chunkSize);
        Gdx.app.log("terrain", ""+sideLength+" x "+sideLength+ " chunks");

        for (int cx = 0; cx < sideLength; cx++) {
            for (int cz = 0; cz < sideLength; cz++) {
                TerrainChunk chunk = new TerrainChunk(cx, cz);
                int key = makeKey(cx, cz);
                chunks.put(key, chunk);
                instances.add(chunk.getModelInstance());
            }
        }

        batch = new SpriteBatch();
    }

    private int makeKey(int cx, int cz) {
        return cx + 1000 * cz;
    }


    // get terrain height at (x,z)
    public float getHeight(float x, float z) {
        // work out what chunk we're in
        // and the relative position for that chunk
        //
        int cx = (int)Math.floor(x/Settings.chunkSize);
        int cz = (int)Math.floor(z/Settings.chunkSize);
        int key = makeKey(cx, cz);
        TerrainChunk chunk = chunks.get(key);
        if(chunk == null){
            Gdx.app.error("position outside chunks", "x:"+x+", z:"+z);
            return 0;
        }
        return chunk.getHeight(x - cx*Settings.chunkSize, z - cz*Settings.chunkSize);
    }


    @Override
    public void dispose() {
        for(TerrainChunk chunk : chunks.values())
            chunk.dispose();
        batch.dispose();
    }
}
