package com.monstrous.canyonracer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.ModelCache;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import net.mgsx.gltf.scene3d.scene.Scene;

// Rocks are not categorized as GameObjects
// They are rendered directly from the model cache
// Collision detection is done with dedicated methods.

public class Rocks implements Disposable {
    public static int AREA_LENGTH = 5000;
    public static int AREA_LENGTH2 = 12000;
    private static int SEPARATION_DISTANCE = 200;  // 200
    private static int SEPARATION_DISTANCE2 = 1500;  // 200

    private static String names[] = { "Rock", "Rock.001", "Rock.002", "Rock.003", "Rock.004" };

    private Vector3 pos = new Vector3();
    public ModelCache cache;


    // note: perhaps we should generate along with chunks to have an infinite amount

    public Rocks(World world ) {

        MathUtils.random.setSeed(1234);

        // generate a random poisson distribution of instances over a rectangular area, meaning instances are never too close together
        PoissonDistribution poisson = new PoissonDistribution();
        Rectangle area = new Rectangle(1, 1, AREA_LENGTH, AREA_LENGTH);
        Array<Vector2> points = poisson.generatePoissonDistribution(SEPARATION_DISTANCE, area);
        for(Vector2 point : points ) {
            point.x -= AREA_LENGTH/2f;
            point.y -= AREA_LENGTH/2f;
        }
        // add more rocks with lower density in a wider outer area
        Rectangle area2 = new Rectangle(1, 1, AREA_LENGTH2, AREA_LENGTH2);
        Array<Vector2> points2 = poisson.generatePoissonDistribution(SEPARATION_DISTANCE2, area2);
        for(Vector2 point : points2 ) {
            if(!area.contains(point)) { // avoid putting more rocks in inner area
                point.x -= AREA_LENGTH2 / 2f;
                point.y -= AREA_LENGTH2 / 2f;
                points.add(point);
            }
        }
        points2.clear();

        cache = new ModelCache();
        cache.begin();

        for(Vector2 point : points ) {
            cache.add( addRock(world, point.x, point.y));
        }
        cache.end();
        Gdx.app.log("Rocks:", String.valueOf(points.size));
    }


    private ModelInstance addRock( World world, float x, float z ){
        int index = MathUtils.random( names.length-1 );
        float scale = MathUtils.random(0.5f, 5.5f);
        float rotation = MathUtils.random(0f, 360f);
        float y = world.terrain.getHeight(x,z);
        pos.set(x,y,z);

        Scene scene = world.loadNode(names[index], true, pos);
        scene.modelInstance.transform.scale(scale, scale, scale);
        scene.modelInstance.transform.rotate(Vector3.Y, rotation);

        world.colliders.addCollider(scene.modelInstance, y+5f);

        return scene.modelInstance;
    }

    @Override
    public void dispose() {
        cache.dispose();
    }
}
