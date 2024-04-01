package com.monstrous.canyonracer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.ModelCache;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;
import net.mgsx.gltf.scene3d.scene.Scene;

// Similar to Rocks.
// Because blades are rotating they are not in the model cache but are game objects.

public class Turbines {
    private static final int AREA_LENGTH = 10000;
    private static final int SEPARATION_DISTANCE = 500;

    private float angle;
    private final Array<GameObject> blades;
    private final Vector3 pos = new Vector3();
    public final ModelCache cache;

    // note: perhaps we should generate along with chunks to have an infinite amount

    public Turbines( World world ) {
        blades = new Array<>();

        MathUtils.random.setSeed(12345);

        // generate a random poisson distribution of instances over a rectangular area, meaning instances are never too close together
        PoissonDistribution poisson = new PoissonDistribution();
        Rectangle area = new Rectangle(1, 1, AREA_LENGTH, AREA_LENGTH);
        Array<Vector2> points = poisson.generatePoissonDistribution(SEPARATION_DISTANCE, area);


        float rocksAreaSize = Rocks.AREA_LENGTH;
        rocksAreaSize *= 1.2f;  // enlarge a bit for margin
        Rectangle rocksArea = new Rectangle(-rocksAreaSize/2, -rocksAreaSize/2, rocksAreaSize, rocksAreaSize);

        cache = new ModelCache();
        cache.begin();
        for(Vector2 point : points ) {
            float x = point.x-AREA_LENGTH/2f;
            float z = point.y-AREA_LENGTH/2f;
            if(!rocksArea.contains(x,z)) {
                cache.add(addTurbine(world, x, z));
                addTurbineBlades(world, x, z);
            }
        }
        cache.end();

        Gdx.app.log("Wind turbines:", String.valueOf(points.size));
    }

    private ModelInstance addTurbine(World world, float x, float z ){
        float y = world.terrain.getHeight(x,z);
        pos.set(x,y,z);

        Scene scene = world.loadNode("Turbine", true, pos);

        float h = world.terrain.getHeight(x,z);
        world.colliders.addCollider(scene.modelInstance, h+Settings.flyHeight);
        return scene.modelInstance;
    }

    private void addTurbineBlades(World world, float x, float z ){
        float y = world.terrain.getHeight(x,z);
        pos.set(x,y,z);
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
