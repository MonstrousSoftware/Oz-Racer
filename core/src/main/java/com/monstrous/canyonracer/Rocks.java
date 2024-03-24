package com.monstrous.canyonracer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;
import com.monstrous.canyonracer.terrain.Terrain;
import com.monstrous.canyonracer.terrain.TerrainChunk;


public class Rocks {
    private static float SCALE = 1;
    public static int AREA_LENGTH = 5000;
    private static int SEPARATION_DISTANCE = 200;


    private static String names[] = { "Rock", "Rock.001", "Rock.002", "Rock.003", "Rock.004" };
    private static float radius[] = { 20f,      21f,        10f,        20f,        14f };


    private ShapeRenderer sr = new ShapeRenderer();

    private Array<Rock> rocks;
    private Vector3 pos = new Vector3();

    static class Rock{
        Vector2 position;   // in horizontal (XZ) plane
        float radius2;

        public Rock(Vector2 position, float radius) {
            this.position = position;
            this.radius2 = radius*radius;
        }
    }

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
            point.x -= AREA_LENGTH/2;
            point.y -= AREA_LENGTH/2;
            addRock(world, point.x, point.y);
        }
    }

    private void addRock( World world, float x, float z ){
        int index = MathUtils.random( names.length-1 );
        float scale = MathUtils.random(0.5f, 5.5f);
        float rotation = MathUtils.random(0f, 360f);
        float y = world.terrain.getHeight(x,z);
        pos.set(x,y,z);
        GameObject gameObject = world.spawnObject(names[index], true, pos);
        gameObject.isRock = true;
        gameObject.getScene().modelInstance.transform.scale(scale, scale, scale);
        gameObject.getScene().modelInstance.transform.rotate(Vector3.Y, rotation);
        gameObject.calculateBoundingBox();  // for frustum culling, update after scaling and rotating

        if(index == 0) { // rock
            // this rock is rectangular, so we use 3 collision circles
            float offset = 30f*scale;
            rocks.add( new Rock( new Vector2(x,z), 15 * scale) );
            rocks.add( new Rock( new Vector2(0, offset).rotateDeg(-rotation).add(x,z), radius[index] * scale) );
            rocks.add( new Rock( new Vector2(0, -offset).rotateDeg(-rotation).add(x,z), radius[index] * scale) );
        }
        else if(index == 3) { // rock.003
            // this rock has a hole, so we use 2 collision circles
            float offset = 45*scale;
            rocks.add( new Rock( new Vector2(0, offset).rotateDeg(-rotation).add(x,z), radius[index] * scale) );
            rocks.add( new Rock( new Vector2(0, -offset).rotateDeg(-rotation).add(x,z), radius[index] * scale) );
        }
        else
            rocks.add( new Rock( new Vector2(x, z), radius[index] * scale) );
    }

    private Vector2 vec2 = new Vector2();

    public boolean inCollision(Vector3 racerPosition, Vector3 colliderPosition ){
        vec2.set(racerPosition.x, racerPosition.z);         // only consider 2d position in horizontal plane
        for(Rock rock : rocks){
            if(rock.position.dst2(vec2) < rock.radius2 ) {
                colliderPosition.set(rock.position.x, 0, rock.position.y);
                return true;
            }
        }
        // some clever data structure would be more efficient
        return false;
    }

    public void debugRender( Vector3 playerPos ) {
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(Color.RED);
        for(Rock rock : rocks){
            vec2.set(rock.position);
            vec2.sub(playerPos.x, playerPos.z);
            float radius = (float)Math.sqrt(rock.radius2)/SCALE;
            convert(vec2);
            sr.circle(vec2.x, vec2.y, radius);
        }


        // racer
        vec2.set(0,0);
        convert(vec2);
        sr.setColor(Color.YELLOW);
        sr.circle(vec2.x, vec2.y, 3);
        sr.end();
    }

    // convert x,z from chunk units to screen pixels x,y
    private void convert(Vector2 pos){
        pos.x  = 600 - pos.x/SCALE;
        pos.y  = 400 + pos.y/SCALE;
    }


}
