package com.monstrous.canyonracer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;


public class Turbines {
    private static int AREA_LENGTH = 10000;
    private static int SEPARATION_DISTANCE = 500;

    private float angle;
    private Array<GameObject> blades;
    private Vector3 pos = new Vector3();



    // note: perhaps we should generate along with chunks to have an infinite amount

    public Turbines( World world ) {
        blades = new Array<>();

        MathUtils.random.setSeed(12345);

        // generate a random poisson distribution of instances over a rectangular area, meaning instances are never too close together
        PoissonDistribution poisson = new PoissonDistribution();
        Rectangle area = new Rectangle(1, 1, AREA_LENGTH, AREA_LENGTH);
        Array<Vector2> points = poisson.generatePoissonDistribution(SEPARATION_DISTANCE, area);
        Gdx.app.log("Wind turbines:", ""+points.size);

        float rocksAreaSize = Rocks.AREA_LENGTH;
        rocksAreaSize *= 1.2f;  // enlarge a bit for margin
        Rectangle rocksArea = new Rectangle(-rocksAreaSize/2, -rocksAreaSize/2, rocksAreaSize, rocksAreaSize);
        for(Vector2 point : points ) {
            float x = point.x-AREA_LENGTH/2;
            float z = point.y-AREA_LENGTH/2;
            if(!rocksArea.contains(x,z))
                addTurbine(world, x, z);
        }
    }

    private void addTurbine( World world, float x, float z ){
        float y = world.terrain.getHeight(x,z);
        pos.set(x,y,z);
        world.spawnObject("Turbine", true, pos);

        //pos.y += 52;
        blades.add( world.spawnObject("Blades", false, pos) );
    }


    // turn blades
    public void update(float deltaTime ) {
        angle += deltaTime * 25f;       // all wind turbines are in synch

        for(GameObject blade : blades ) {

            Matrix4 transform = blade.getScene().modelInstance.transform;

            transform.getTranslation(pos);
            transform.setToRotation(Vector3.Z, angle);
            transform.setTranslation(pos);
        }
    }
}
