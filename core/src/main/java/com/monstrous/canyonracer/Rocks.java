package com.monstrous.canyonracer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;


public class Rocks {
    public static int AREA_LENGTH = 5000;
    private static int SEPARATION_DISTANCE = 200;
    private static String names[] = { "Rock", "Rock.001", "Rock.002", "Rock.003", "Rock.004", "Rock.005", "Rock.006" };

    private Array<GameObject> rocks;
    private Vector3 pos = new Vector3();

    // note: perhaps we should generate along with chunks to have an infinite amount

    public Rocks(World world ) {
        rocks = new Array<>();

        MathUtils.random.setSeed(1234);

        // generate a random poisson distribution of instances over a rectangular area, meaning instances are never too close together
        PoissonDistribution poisson = new PoissonDistribution();
        Rectangle area = new Rectangle(1, 1, AREA_LENGTH, AREA_LENGTH);
        Array<Vector2> points = poisson.generatePoissonDistribution(SEPARATION_DISTANCE, area);
        Gdx.app.log("Rocks:", ""+points.size);

        for(Vector2 point : points ) {
            addRock(world, point.x-AREA_LENGTH/2, point.y-AREA_LENGTH/2);
        }
    }

    private void addRock( World world, float x, float z ){
        int index = MathUtils.random( names.length-1 );
        float scale = MathUtils.random(0.5f, 5.5f);
        float rotation = MathUtils.random(0f, 360f);
        float y = world.terrain.getHeight(x,z);
        pos.set(x,y,z);
        GameObject rock = world.spawnObject(names[index], true, pos);
        rock.getScene().modelInstance.transform.scale(scale, scale, scale);
        rock.getScene().modelInstance.transform.rotate(Vector3.Y, rotation);
        rock.calculateBoundingBox();
    }

}
