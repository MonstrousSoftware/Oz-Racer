package com.monstrous.canyonracer.terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.monstrous.canyonracer.Settings;
import net.mgsx.gltf.scene3d.scene.Scene;
import java.util.HashMap;



// Infinite Terrain using terrain chunks that are generated on demand.
// It is subdivided into chunks of size Settings.chunkSize


public class Terrain implements Disposable {
    private static int RANGE = 2;

    HashMap<Integer, TerrainChunk> chunks;      // map of terrain chunk per grid point
    public Array<Scene> scenes;                 // scenes to be rendered
    int timeCounter;
    private Noise noise = new Noise();

    public Terrain( Vector3 startPosition) {
        chunks = new HashMap<>();
        scenes = new Array<>();

        int px = (int)Math.floor(startPosition.x/Settings.chunkSize);
        int pz = (int)Math.floor(startPosition.z/Settings.chunkSize);

        // Add a NxN square of chunks to the scenes array (is RANGE is 2, this is 5x5)
        // Create chunks as needed (but not more than one at a time)

        for (int cx = px-RANGE; cx <= px+RANGE; cx++) {
            for (int cz = pz-RANGE; cz <= pz+RANGE; cz++) {

                Integer key = makeKey(cx, cz);

                TerrainChunk chunk = chunks.get(key);
                if(chunk == null) {
                    chunk = new TerrainChunk(cx, cz, timeCounter);
                    chunks.put(key, chunk);
                }
            }
        }
    }



    public boolean update(Camera cam){
        timeCounter++;

        int px = (int)Math.floor(cam.position.x/Settings.chunkSize);
        int pz = (int)Math.floor(cam.position.z/Settings.chunkSize);

        // Add a NxN square of chunks to the scenes array (is RANGE is 2, this is 5x5)
        // Create chunks as needed (but not more than one at a time)

        int added = 0;
        scenes.clear();
        for (int cx = px-RANGE; cx <= px+RANGE; cx++) {
            for (int cz = pz-RANGE; cz <= pz+RANGE; cz++) {

                Integer key = makeKey(cx, cz);

                TerrainChunk chunk = chunks.get(key);
                if(chunk == null && added == 0) {
                        chunk = new TerrainChunk(cx, cz, timeCounter);
                        chunks.put(key, chunk);
                        //Gdx.app.log("num chunks", "" + chunks.size());
                        added++;                             // avoid generating more than 1 chunk per frame to avoid stutter
                }
                if(chunk != null && cam.frustum.boundsInFrustum(chunk.bbox)) {  // frustum culling
                    scenes.add(chunk.getScene());
                    chunk.lastSeen = timeCounter;
                }
            }
        }

        // keep the chunk cache at a reasonable size
        // delete the oldest chunk if necessary

        if(added == 0 && chunks.size() > Settings.chunkCacheSize){
            // find the oldest chunk
            // alternative: last seen chunk
            TerrainChunk oldest = null;
            for(TerrainChunk chunk : chunks.values()){
                if(oldest == null || chunk.creationTime < oldest.creationTime)
                    oldest = chunk;
            }
            // now remove this chunk
            if(oldest != null) {
                Integer key =  makeKey(oldest.coord.x, oldest.coord.y);
                chunks.remove(key);
                scenes.removeValue(oldest.getScene(), true);
                oldest.dispose();
                //Gdx.app.log("deleting "+oldest.coord.toString(), "num chunks"+chunks.size());
                return true;
            }
        }
        return added > 0;
    }

    // convert chunk (X,Y) to a single integer for easy use as a key in the hash map
    private int makeKey(int cx, int cz) {
        return cx + 1000 * cz;
    }


    // get terrain height at (x,z)
    public float getHeight(float x, float z) {


//        x /= Settings.chunkSize;
//        z /= Settings.chunkSize;
//        //z += 1000* TerrainChunk.MAP_SIZE;
//        float px = x / (float)TerrainChunk.GRID_SCALE;
//        float pz = z / (float)TerrainChunk.GRID_SCALE;
//
//        float y = noise.PerlinNoise(px, pz);
//        y *= TerrainChunk.AMPLITUDE;
//        return y;



        // work out what chunk we're in
        // and the relative position for that chunk
        //
        int cx = (int)Math.floor(x/Settings.chunkSize);
        int cz = (int)Math.floor(z/Settings.chunkSize);
        Integer key = makeKey(cx, cz);
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
    }

}
