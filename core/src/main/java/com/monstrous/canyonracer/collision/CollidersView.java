package com.monstrous.canyonracer.collision;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Disposable;


// provides top-down view of collider polygons for debugging

public class CollidersView implements Disposable {
    private static float DEBUG_VIEW_SCALE = 5;

    private ShapeRenderer sr;
    private int cx, cy;
    private Polygon poly;

    public CollidersView() {
        sr = new ShapeRenderer();
        poly = new Polygon();
        cx = Gdx.graphics.getWidth()/2;
        cy = Gdx.graphics.getHeight()/2;
    }

    public void resize(int width, int height){
        cx = width/2;
        cy = height/2;
    }

    public void debugRender( Vector3 playerPos, Colliders colliders ) {
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(Color.BLUE);

        for(Polygon p : colliders.collisionPolygons){
            // copy the polygon to convert it to screen view
            // relative tp racer position
            // world X is up on the screen (y)
           poly.setVertices(p.getVertices());
           poly.setRotation(90);
           poly.setScale(1f/ DEBUG_VIEW_SCALE, -1f/ DEBUG_VIEW_SCALE);
           poly.setPosition(cx-playerPos.z/ DEBUG_VIEW_SCALE, cy-playerPos.x/ DEBUG_VIEW_SCALE);

           sr.polygon(poly.getTransformedVertices());
        }


        // racer
        sr.setColor(Color.YELLOW);
        sr.circle(cx, cy, 5);
        sr.end();
    }


    @Override
    public void dispose() {
        sr.dispose();
    }
}
