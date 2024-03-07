package com.monstrous.canyonracer.terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.monstrous.canyonracer.GameObject;
import com.monstrous.canyonracer.Settings;
import net.mgsx.gltf.scene3d.scene.Scene;


import java.util.HashMap;


// Infinite Terrain using terrain chunks that are generated on demand.
// It is subdivided into chunks of size Settings.chunkSize


public class Terrain implements Disposable {

    private HashMap<Integer, TerrainChunk> chunks;   // map of terrain chunk per grid point
    public Array<Scene> scenes;          // scenes to be rendered
    private SpriteBatch batch;
    private Texture texture;
    private TextureRegion textureRegionChunk;
    private TextureRegion textureRegionRacer;
    private TextureRegion textureRegionCam;
    private int timeCounter;

    public Terrain() {
        chunks = new HashMap<>();
        scenes = new Array<>();

        initDebug();
    }



    public boolean update(Vector3 cameraPos){
        timeCounter++;

        int px = (int)Math.floor(cameraPos.x/Settings.chunkSize);
        int pz = (int)Math.floor(cameraPos.z/Settings.chunkSize);

        // Add a 5x5 square of chunks to the scenes array
        // Create chunks as needed (but not more than one at a time)

        int added = 0;
        scenes.clear();
        for (int cx = px-2; cx <= px+2; cx++) {
            for (int cz = pz-2; cz <= pz+2; cz++) {

                int key = makeKey(cx, cz);

                TerrainChunk chunk = chunks.get(key);
                if(chunk == null) {
                    if (added == 0) {
                        chunk = new TerrainChunk(cx, cz, timeCounter);
                        chunks.put(key, chunk);
                        scenes.add(chunk.getScene());
                        Gdx.app.log("num chunks", "" + chunks.size());
                        added++;                             // avoid generating more than 1 chunk per frame to avoid stutter
                    }
                }
                else
                    scenes.add(chunk.getScene());
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
            int key = makeKey(oldest.coord.x, oldest.coord.y);
            chunks.remove(key);
            scenes.removeValue(oldest.getScene(), true);
            oldest.dispose();
            Gdx.app.log("deleting "+oldest.coord.toString(), "num chunks"+chunks.size());
            return true;
        }
        return added > 0;
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
        texture.dispose();
    }

    private void initDebug() {
        batch = new SpriteBatch();

        // use a pixmap to create different solid colour texture regions (1 pixel each)
        Pixmap pixmap = new Pixmap(3, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 1, 0, 0.5f);
        pixmap.drawPixel(0,0);
        pixmap.setColor(1, 0, 0, 1);
        pixmap.drawPixel(1,0);
        pixmap.setColor(0, 0, 1, 1);
        pixmap.drawPixel(2,0);

        texture = new Texture(pixmap);
        textureRegionChunk = new TextureRegion(texture, 0,0,1,1);
        textureRegionRacer = new TextureRegion(texture, 1,0,1,1);
        textureRegionCam = new TextureRegion(texture, 2,0,1,1);
    }


    private Vector3 pos = new Vector3();

    public void debugRender(Vector3 playerPos, Vector3 camPos) {
        if(!Settings.debugChunkAllocation)
            return;
        pos.set(playerPos);

        int size = 30;  // pixels per chunk

        batch.begin();
        for(TerrainChunk chunk : chunks.values() ) {
            int x = 400+size * chunk.coord.x;
            int y = 400+size * chunk.coord.y;
            batch.draw(textureRegionChunk, x, y, size-2, size-2);
        }

        // racer
        pos.x /=Settings.chunkSize;
        pos.z /=Settings.chunkSize;
        pos.x  = 400+size*pos.x;
        pos.z  = 400+size*pos.z;
        batch.draw(textureRegionRacer, pos.x, pos.z, 4, 4);

        // camera
        pos.set(camPos);
        pos.x /=Settings.chunkSize;
        pos.z /=Settings.chunkSize;
        pos.x  = 400+size*pos.x;
        pos.z  = 400+size*pos.z;
        batch.draw(textureRegionCam, pos.x, pos.z, 4, 4);

        batch.end();
    }
}
