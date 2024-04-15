package com.monstrous.canyonracer.terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Disposable;
import com.monstrous.canyonracer.Settings;
import com.monstrous.canyonracer.screens.Main;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.scene.Scene;


public class TerrainChunk implements Disposable {

    public static final int MAP_SIZE = 128;     // size of map in grid cells per axis
    public static final float SCALE  = Settings.chunkSize;       // terrain size in world units
    public static final float AMPLITUDE  = 200f; // amplitude in world units
    public static final float GRID_SCALE = 64;      // how many Perlin points across the map


    public GridPoint2 coord;
    public int creationTime;            // when was chunk created? used to delete old chunks when needed
    public int lastSeen;
    public BoundingBox bbox;

    private Model model;
    private static Texture terrainTexture;         //  shared between chunks
    private ModelInstance modelInstance;
    private Scene scene;
    private float[][] heightMap;
    private Vector3 position; // position of terrain in world coordinates



    public TerrainChunk(int xoffset, int yoffset, int creationTime) {
        //Gdx.app.log("TerrainChunk create:", ""+xoffset+" , "+yoffset);

        this.coord = new GridPoint2(xoffset, yoffset);
        this.creationTime = creationTime;
        position = new Vector3(xoffset * Settings.chunkSize, 0, yoffset * Settings.chunkSize);
        bbox = new BoundingBox();

        Noise noise = new Noise();


        heightMap = noise.generatePerlinMap(xoffset*MAP_SIZE, yoffset*MAP_SIZE, MAP_SIZE, MAP_SIZE,  GRID_SCALE, AMPLITUDE);

        if(terrainTexture == null) {
            terrainTexture = Main.assets.terrainTexture; //new Texture(Gdx.files.internal("textures/ground/smooth+sand+dunes-512x512.jpg"), true);
            terrainTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
            terrainTexture.setFilter(Texture.TextureFilter.MipMap, Texture.TextureFilter.Nearest);
        }

        Material material =  new Material();
        material.set(PBRTextureAttribute.createBaseColorTexture(terrainTexture));

        model = makeGridModel(heightMap, SCALE, MAP_SIZE, GL20.GL_TRIANGLES, material);
        modelInstance =  new ModelInstance(model, position);

        modelInstance.calculateBoundingBox(bbox);       // could be done faster
        bbox.mul(modelInstance.transform);
        scene = new Scene(modelInstance, false);
    }


    public Scene getScene() { return scene; }

    @Override
    public void dispose() {
        model.dispose();
    }


    // make a Model consisting of a square grid
    private Model makeGridModel(float[][] heightMap, float scale, int divisions, int primitive, Material material) {
        final int N = divisions;

        int attr = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates;

        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        MeshBuilder meshBuilder = (MeshBuilder) modelBuilder.part("face", primitive, attr, material);
        final int numVerts = (N + 1) * (N + 1);
        final int numTris = 2 * N * N;
        Vector3[] positions = new Vector3[numVerts];
        Vector3[] normals = new Vector3[numVerts];

        meshBuilder.ensureVertices(numVerts);
        meshBuilder.ensureTriangleIndices(numTris);

        Vector3 pos = new Vector3();
        float posz;

        for (int y = 0; y <= N; y++) {
            float posy = ((float) y / (float) N);        // y in [0.0 ..1.0]
            for (int x = 0; x <= N; x++) {
                float posx = ((float) x / (float) N);        // x in [0.0 .. 1.0]

                posz = heightMap[y][x];
                pos.set(posx * scale, posz, posy * scale);            // swapping z,y to orient horizontally

                positions[y * (N + 1) + x] = new Vector3(pos);
                normals[y * (N + 1) + x] = new Vector3(0, 0, 0);
            }
        }

        for (int y = 1; y <= N; y++) {
            short v0 = (short) ((y - 1) * (N + 1));    // vertex number at top left of this row
            for (int x = 0; x <= N-1; x++, v0++) {
                addRect(meshBuilder, positions, normals, (short) (v0 + N + 1), (short) (v0 + N + 2), (short) (v0 + 1), v0);
            }
        }

        // now normalize each normal (which is the sum of the attached triangle normals)
        // and pass vertex to meshBuilder
        MeshPartBuilder.VertexInfo vert = new MeshPartBuilder.VertexInfo();
        vert.hasColor = false;
        vert.hasNormal = true;
        vert.hasPosition = true;
        vert.hasUV = true;

        Vector3 normal = new Vector3();
        for (int i = 0; i < numVerts; i++) {
            normal.set(normals[i]);     // sum of normals to get smoothed normals
            normal.nor();               // take average

            int x = i % (N+1);	// e.g. in [0 .. 3] if N == 3
            int y = i / (N+1);

            float reps=64; //16
            float u = (x*reps)/(float)(N+1);
            float v = (y*reps)/(float)(N+1);
            vert.position.set(positions[i]);
            vert.normal.set(normal);
            vert.uv.x = u;					// texture needs to have repeat wrapping enables to handle u,v > 1
            vert.uv.y = v;
            meshBuilder.vertex(vert);
        }

        Model model = modelBuilder.end();
        return model;
    }

    private void addRect(MeshBuilder meshBuilder, final Vector3[] vertices, Vector3[] normals, short v0, short v1, short v2, short v3) {
        meshBuilder.rect(v0, v1, v2, v3);
        calcNormal(vertices, normals, v0, v1, v2);
        calcNormal(vertices, normals, v2, v3, v0);
        // 6 indices to make 2 triangles, follows order of meshBuilder.rect()
        //
        //     v3 --v2
        //      | /  |
        //     v0 --v1
        // triangle v0,v1,v2 and v2, v3, v0

    }

    /*
     * Calculate the normal
     */
    private Vector3 u = new Vector3();
    private Vector3 v = new Vector3();
    private Vector3 n = new Vector3();

    private void calcNormal(final Vector3[] vertices, Vector3[] normals, short v0, short v1, short v2) {

        Vector3 p0 = vertices[v0];
        Vector3 p1 = vertices[v1];
        Vector3 p2 = vertices[v2];

        v = new Vector3(p2).sub(p1);
        u = new Vector3(p0).sub(p1);
        n = new Vector3(v).crs(u).nor();

        normals[v0].add(n);
        normals[v1].add(n);
        normals[v2].add(n);
    }


    private Vector2 baryCoord = new Vector2();

    // x, z relative to terrain chunk
    public float getHeight(float relx, float relz) {
        // position relative to terrain origin

        // position in grid (rounded down) : grid cell coordinates [0.. MAP_SIZE-1]
        int mx = (int)Math.floor((relx * MAP_SIZE) / Settings.chunkSize);
        int mz = (int)Math.floor((relz * MAP_SIZE) / Settings.chunkSize);

        if(mx < 0 ||mx >= MAP_SIZE || mz < 0 || mz >= MAP_SIZE){
            Gdx.app.error("getHeight", "coord out of bounds");
            return 0;
        }

        float cellSize = Settings.chunkSize / (float)MAP_SIZE;
        float xCoord = (relx % cellSize)/cellSize;
        float zCoord = (relz % cellSize)/cellSize;
        float ht;
        if( xCoord < 1f - zCoord) {   // top triangle
            baryCoord.set(xCoord, zCoord);
            ht = GeometryUtils.fromBarycoord(baryCoord, heightMap[mz][mx], heightMap[mz][mx+1], heightMap[mz+1][mx]);
        }
        else { // bottom triangle
            baryCoord.set(1f-xCoord, 1f-zCoord);
            ht =  GeometryUtils.fromBarycoord(baryCoord, heightMap[mz+1][mx+1], heightMap[mz+1][mx], heightMap[mz][mx+1]);
        }
        return ht;
    }

}
