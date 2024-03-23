package com.monstrous.canyonracer;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.monstrous.canyonracer.terrain.Terrain;
// OBSOLETE
public class Path {

    private Terrain terrain;
    private Array<Vector3> wayPoints;
    private ModelBatch modelBatch;
    private Array<ModelInstance> instances;

    public Path(Terrain terrain) {
        this.terrain = terrain;
        wayPoints = new Array<>();

        wayPoints.add( addPoint(0, 20));
        wayPoints.add( addPoint(10, 70));
        wayPoints.add( addPoint(-50, 0));
        wayPoints.add( addPoint(-20, -150));
        wayPoints.add( addPoint(-10, -200));
        wayPoints.add( addPoint(0, 20));

        modelBatch = new ModelBatch();
        instances = new Array<>();
        buildInstances();

    }

    private Vector3 addPoint(float x, float z){
        float y = 15f+terrain.getHeight(x, z); // maybe chunks not loaded yet?
        Vector3 v = new Vector3(x, y, z);
        return v;
    }

    private Vector3 v2 = new Vector3();

    private void buildInstances(){
        ModelBuilder builder = new ModelBuilder();
        Model arrow = builder.createArrow( new Vector3(0,40,0), Vector3.Zero, new Material(), VertexAttributes.Usage.Position | VertexAttributes.Usage.ColorPacked );

        for(Vector3 v : wayPoints ){
            instances.add( new ModelInstance(arrow, v) );
        }

        Model arrowModel = builder.createXYZCoordinates(15f, new Material(), VertexAttributes.Usage.Position | VertexAttributes.Usage.ColorPacked);
        instances.add(new ModelInstance(arrowModel, Vector3.Zero));

    }

//    private ShapeRenderer sr = new ShapeRenderer();
//    private Vector3 v1 = new Vector3();

    public void render(Camera camera ){

        modelBatch.begin(camera);
        modelBatch.render(instances);
        modelBatch.end();


//        sr.setProjectionMatrix( camera.combined );
//        sr.begin( ShapeRenderer.ShapeType.Filled );
//        sr.setColor(Color.BLUE);
//        for(int i = 0; i < wayPoints.size-1; i++)
//            sr.line(wayPoints.get(i), wayPoints.get(i+1));
//        sr.setColor(Color.GREEN);
//        for(int i = 0; i < wayPoints.size-1; i++) {
//            v1.set(wayPoints.get(i));
//            v2.set(v1);
//            v2.y += 50f;
//            sr.line(v1, v2);
//        }
//        sr.setColor(Color.RED);
//        sr.point(20, 10, 50);
//        sr.end();

    }
}
