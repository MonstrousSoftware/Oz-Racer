package com.monstrous.canyonracer;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.monstrous.canyonracer.terrain.Terrain;

public class Turbines {
    private static int AREA_LENGTH = 5000;
    private static int SEPARATION_DISTANCE = 500;

    private float angle;
    private Array<GameObject> blades;
    private Vector3 pos = new Vector3();



    public Turbines( World world, Terrain terrain) {
        blades = new Array<>();

        // generate a random poisson distribution of instances over a rectangular area, meaning instances are never too close together
        PoissonDistribution poisson = new PoissonDistribution();
        Rectangle area = new Rectangle(1, 1, AREA_LENGTH, AREA_LENGTH);
        Array<Vector2> points = poisson.generatePoissonDistribution(SEPARATION_DISTANCE, area);

        for(Vector2 point : points ) {
            addTurbine(world, point.x-AREA_LENGTH/2, point.y-AREA_LENGTH/2);
        }
    }

    private void addTurbine( World world, float x, float z ){
        pos.set(x,0,z);
        world.spawnObject("Turbine", true, pos);
        pos.y += 52;
        blades.add( world.spawnObject("Blades", false, pos) );
    }


    // turn blades
    public void update( float deltaTime ) {
        angle += deltaTime * 25f;

        for(GameObject blade : blades ) {

            Matrix4 transform = blade.getScene().modelInstance.transform;

            transform.getTranslation(pos);
            transform.setToRotation(Vector3.Z, angle);
            transform.setTranslation(pos);
        }
    }
}
