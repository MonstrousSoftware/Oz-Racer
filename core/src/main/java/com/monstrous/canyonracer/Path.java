package com.monstrous.canyonracer;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.monstrous.canyonracer.terrain.Terrain;

public class Path {

    private Terrain terrain;
    private Array<Vector3> wayPoints;

    public Path(Terrain terrain) {
        this.terrain = terrain;
        wayPoints = new Array<>();

        wayPoints.add( addPoint(0, 20));
        wayPoints.add( addPoint(10, 70));
        wayPoints.add( addPoint(-50, 0));
        wayPoints.add( addPoint(-200, -150));
        wayPoints.add( addPoint(-10, -200));
        wayPoints.add( addPoint(0, 50));

    }

    private Vector3 addPoint(float x, float z){
        float y = 15f+terrain.getHeight(x, z); // maybe chunks not loaded yet?
        Vector3 v = new Vector3(x, y, z);
        return v;
    }

    private ShapeRenderer sr = new ShapeRenderer();
    private Vector3 v1 = new Vector3(), v2 = new Vector3();

    public void render(Camera cam ){
        sr.setProjectionMatrix( cam.combined );
        sr.begin( ShapeRenderer.ShapeType.Filled );
        sr.setColor(Color.BLUE);
        for(int i = 0; i < wayPoints.size-1; i++)
            sr.line(wayPoints.get(i), wayPoints.get(i+1));
        sr.setColor(Color.GREEN);
        for(int i = 0; i < wayPoints.size-1; i++) {
            v1.set(wayPoints.get(i));
            v2.set(v1);
            v2.y = 50f+v2.x;
            sr.line(v1, v2);
        }
        sr.setColor(Color.RED);
        sr.point(20, 10, 50);
        sr.end();

    }
}
