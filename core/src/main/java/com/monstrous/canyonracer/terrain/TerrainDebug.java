package com.monstrous.canyonracer.terrain;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.monstrous.canyonracer.Settings;


// Helper class to visualize terrain chunk creation/disposal as debug overlay
//

public class TerrainDebug {
    private static int SIZE = 30; // pixels per chunk

    private Terrain terrain;

    private SpriteBatch batch;
    private Texture texture;
    private TextureRegion textureRegionChunk;
    private TextureRegion textureRegionRacer;
    private TextureRegion textureRegionCam;
    private TextureRegion textureRegionChunkVisible;

    public TerrainDebug( Terrain terrain ) {
        this.terrain = terrain;

        batch = new SpriteBatch();

        // use a pixmap to create different solid colour texture regions (1 pixel each)
        Pixmap pixmap = new Pixmap(4, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 1, 0, 0.2f);
        pixmap.drawPixel(0,0);
        pixmap.setColor(1, 0, 0, 1);
        pixmap.drawPixel(1,0);
        pixmap.setColor(0, 0, 1, 1);
        pixmap.drawPixel(2,0);
        pixmap.setColor(0, 1, .5f, 0.5f);
        pixmap.drawPixel(3,0);

        texture = new Texture(pixmap);
        textureRegionChunk = new TextureRegion(texture, 0,0,1,1);
        textureRegionRacer = new TextureRegion(texture, 1,0,1,1);
        textureRegionCam = new TextureRegion(texture, 2,0,1,1);
        textureRegionChunkVisible = new TextureRegion(texture, 3,0,1,1);
    }


    private Vector3 pos = new Vector3();

    public void debugRender(Vector3 playerPos, Vector3 camPos) {
        if(!Settings.debugChunkAllocation)
            return;

        int size = SIZE;  // pixels per chunk
        batch.begin();
        for(TerrainChunk chunk : terrain.chunks.values() ) {
            pos.set(chunk.coord.x, 0, chunk.coord.y);
            convert(pos);
            if(chunk.lastSeen == terrain.timeCounter)
                batch.draw(textureRegionChunkVisible, pos.x, pos.y-SIZE, size-2, size-2);
            else
                batch.draw(textureRegionChunk, pos.x, pos.y-SIZE, size-2, size-2);
        }

        // racer
        pos.set(playerPos);
        pos.x /=Settings.chunkSize;
        pos.z /=Settings.chunkSize;
        convert(pos);
        batch.draw(textureRegionRacer, pos.x, pos.y, 4, 4);

        // camera
        pos.set(camPos);
        pos.x /=Settings.chunkSize;
        pos.z /=Settings.chunkSize;
        convert(pos);
        batch.draw(textureRegionCam, pos.x, pos.y, 4, 4);

        batch.end();
    }

    // convert x,z from chunk units to screen pixels x,y
    private void convert(Vector3 pos){
        pos.x  = 400 + SIZE*pos.x;
        pos.y  = 400 - SIZE*pos.z;
    }


    public void dispose() {
        batch.dispose();
        texture.dispose();
    }
}
