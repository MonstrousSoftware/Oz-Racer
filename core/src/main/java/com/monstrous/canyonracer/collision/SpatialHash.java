package com.monstrous.canyonracer.collision;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

import java.util.HashMap;

// Spatial Hash to speed up collision detection
// World space is (virtually) divided into a horizontal grid
// (Virtually because instead of a 2d matrix we use a hashmap with the grid coordinates as key, this allows
// an almost unlimited size grid without wasting memory).
// Each grid cell has a bucket with zero or more colliders.
// This way we only need to test the colliders in the grid cell where the racer is (the racer is approximated as a single point)
// A large collider may appear in multiple grid cells.

public class SpatialHash {
    private static final float cellSize = 500;  // tweak for best performance

    private final HashMap<Integer, Array<Polygon>> buckets;   // map of buckets per grid cell

    public SpatialHash() {
        buckets = new HashMap<>();
    }

    public void addPolygon( Polygon poly ){
        Rectangle rect = poly.getBoundingRectangle();
        for(float x = rect.x; x < rect.x+rect.width; x+=cellSize){
            for(float y = rect.y; y < rect.y+rect.height; y+=cellSize) {
                int key = makeKey(x, y);
                Array<Polygon> bucket = buckets.get(key);
                if(bucket == null){
                    bucket = new Array<>();
                    buckets.put(key, bucket);
                }
                bucket.add(poly);
            }
        }
    }

    public Array<Polygon> findPolygons( float x, float y ){

        int key = makeKey(x, y);
        Array<Polygon> bucket = buckets.get(key);
        return bucket;
    }

    // make a unique integer for the grid cell containing (x,y)
    private int makeKey( float x, float y ){
        int xi = gridCooordinate(x);
        int yi = gridCooordinate(y);
        return xi*1000+yi;
    }

    // convert float world coordinate to a (positive) discrete grid coordinate
    private int gridCooordinate( float x ){
        return (int)(x/cellSize)+500;
    }

}
