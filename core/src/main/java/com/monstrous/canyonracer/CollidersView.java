package com.monstrous.canyonracer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Disposable;


public class CollidersView implements Disposable {
    private static float DEBUG_VIEW_SCALE = 10;

    private ShapeRenderer sr;
    private Vector2 vec2;

    public CollidersView() {
        sr = new ShapeRenderer();
        vec2 = new Vector2();
    }



    public void debugRender( Vector3 playerPos, Colliders colliders ) {
        sr.begin(ShapeRenderer.ShapeType.Line);

        float[] test = { -2500, 2500, 2500, 2500, 2500, -2500, -2500, -2500};
        Polygon poly = new Polygon(test);

        sr.setColor(Color.GREEN);
        poly.setScale(1f/ DEBUG_VIEW_SCALE, 1f/ DEBUG_VIEW_SCALE);
        poly.translate(600-playerPos.z/ DEBUG_VIEW_SCALE, 400-playerPos.x/ DEBUG_VIEW_SCALE);
        sr.polygon(poly.getTransformedVertices());

        //float[] test2 = { -25, 25, 25, 25, 25, -25, -25, -25};
        Polygon poly2 = new Polygon();

        sr.setColor(Color.BLUE);
        //Polygon p = collisionPolygons.get(0);
        //poly2.setVertices(p.getVertices());
        for(Polygon p : colliders.collisionPolygons){

           poly2.setVertices(p.getVertices());
           poly2.setScale(1f/ DEBUG_VIEW_SCALE, 1f/ DEBUG_VIEW_SCALE);
           poly2.setPosition(600-playerPos.z/ DEBUG_VIEW_SCALE, 400-playerPos.x/ DEBUG_VIEW_SCALE);

//           float[] verts = poly2.getTransformedVertices();
//           for(int j = 0; j < verts.length; j += 2){
//               sr.circle(verts[j], verts[j+1], 2);
//            }

            sr.polygon(poly2.getTransformedVertices());
        }


        // racer
        vec2.set(0,0);
        convert(vec2);
        sr.setColor(Color.YELLOW);
        sr.circle(vec2.y, vec2.x, 5);
        sr.end();
    }

    // convert x,z from chunk units to screen pixels x,y
    private void convert(Vector2 pos){
        pos.x  = 400 + pos.x/ DEBUG_VIEW_SCALE;
        pos.y  = 600 + pos.y/ DEBUG_VIEW_SCALE;
    }


    @Override
    public void dispose() {
        sr.dispose();
    }
}
