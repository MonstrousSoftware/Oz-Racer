package com.monstrous.canyonracer.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntIntMap;
import com.monstrous.canyonracer.GameObject;
import com.monstrous.canyonracer.Settings;
import com.monstrous.canyonracer.World;
import com.monstrous.canyonracer.terrain.Terrain;


// Controller for the racer using keyboard input and/or gamecontroller input

public class PlayerController extends InputAdapter {

    public static int forwardKey = Input.Keys.W;
    public static int turnLeftKey = Input.Keys.A;
    public static int turnRightKey = Input.Keys.D;
    public static int boostKey = Input.Keys.SPACE;

    private float speed;
    private float turnAngle;
    public float rotation;
    private float hoverHeight = 5f;

    private final IntIntMap keys = new IntIntMap();
    public Vector3 forwardDirection;       // unit vector in forward direction
    private Vector3 velocity;               // velocity vector

    private Vector3 tmpV = new Vector3();
    private Vector3 playerPos = new Vector3();
    private Vector3 drag = new Vector3();
    private float targetHeight;

    private float stickHorizontal;
    private float stickVertical;
    private float stickBoost;
    private boolean stickSteering = false;

    public float boostFactor;
    private boolean enoughNitro;


    public PlayerController() {
        forwardDirection = new Vector3();
        velocity = new Vector3(0,0,0);
        restart(0);
    }

    public void restart( float angle ){
        speed = 0;
        rotation = angle;
        turnAngle = 0;
        hoverHeight = 5f;
        velocity.set(Vector3.Zero);
        boostFactor = 0;
        enoughNitro = true;
    }

    public float getSpeed(){
        return speed;
    }

    @Override
    public boolean keyDown (int keycode) {
        keys.put(keycode, keycode);
        return true;
    }

    @Override
    public boolean keyUp (int keycode) {
        keys.remove(keycode, 0);
        return true;
    }

    public void update (GameObject racer, Terrain terrain, float deltaTime ) {

        // sink to the ground when dead
        if(World.healthPercentage <= 0) {
            hoverHeight = 0;
        }

        float acceleration = 0f;
        if (keys.containsKey(forwardKey) && World.healthPercentage > 0)
            acceleration = Settings.acceleration;
        else if(stickVertical > 0 && World.healthPercentage > 0)
            acceleration = Settings.acceleration * stickVertical;

        speed = velocity.len();

        if (keys.containsKey(boostKey) && boostFactor < 1f && enoughNitro && speed > 0 && World.healthPercentage > 0)
            boostFactor += deltaTime;
        else if (stickBoost > 0 && enoughNitro && speed > 0 && World.healthPercentage > 0)
            boostFactor = MathUtils.lerp(boostFactor, stickBoost,  deltaTime);
        else if(boostFactor > 0)
            boostFactor -= deltaTime;

        // consume nitro if boosting, or if not: slowly replenish the nitro
        if(boostFactor > 0) {
            World.nitroLevel -= boostFactor * deltaTime * Settings.nitroConsumption;
            if(World.nitroLevel <= 0)
                enoughNitro = false;
        } else if (World.nitroLevel < 100 && World.healthPercentage > 0) {
            World.nitroLevel += deltaTime * Settings.nitroReplenishment;
            if(World.nitroLevel > 50)
                enoughNitro = true;
        }

        acceleration += boostFactor*acceleration;

        if (keys.containsKey(turnLeftKey)&& World.healthPercentage > 0) {
            if (turnAngle < Settings.maxTurn)
                turnAngle += deltaTime * Settings.turnRate;
        }
        else if (keys.containsKey(turnRightKey)&& World.healthPercentage > 0) {
            if( turnAngle > -Settings.maxTurn)
                turnAngle -= deltaTime *  Settings.turnRate;
        }
        else if (stickSteering && World.healthPercentage > 0) { // do we have controller stick input?
            float targetAngle = stickHorizontal * Settings.maxTurn;
            turnAngle = MathUtils.lerp(turnAngle, targetAngle, 5.0f*deltaTime);
        }
        else
            turnAngle -= turnAngle * 2f * deltaTime;      // auto-level

        Matrix4 transform = racer.getScene().modelInstance.transform;
        transform.getTranslation(playerPos);

        // place racer fixed distance above the terrain, i.e. it follows the terrain
        targetHeight = hoverHeight+terrain.getHeight(playerPos.x, playerPos.z);

        // move towards target height with some lag
        playerPos.y = moveTowards(playerPos.y, targetHeight, Settings.heightLag*deltaTime);

        // apply rotation to player transform (yaw)
        rotation += turnAngle*deltaTime;
        transform.setToRotation(Vector3.Y, rotation);
        // roll
        transform.rotate(Vector3.Z, -turnAngle * Settings.bankFactor);                // bank for visual effect

        // determine forward vector of the racer from its transform
        forwardDirection.set(Vector3.Z).rot(transform);

        // apply acceleration to velocity: v' = v + dt.a
        tmpV.set(forwardDirection).scl(deltaTime * acceleration);
        velocity.add(tmpV);

        // drag scales with speed and slows down the racer (deceleration). This limits the top speed.
        drag.set(velocity).scl(deltaTime*Settings.dragFactor);
        velocity.sub(drag);

        // apply velocity to position
        tmpV.set(velocity).scl(deltaTime);
        playerPos.add(tmpV);
        transform.setTranslation(playerPos);
    }

    // act on collision impact
    // normal is the surface normal we collided with
    public void collisionImpact( Vector3 normal, GameObject racer ) {
        boostFactor = 0;                    // remove any boost

        // reflect the velocity vector across the normal vector of the collider surface: r = d - 2(d.n)n
        float dot = velocity.dot(normal);
        velocity.sub(normal.scl(2f*dot));
        turnAngle = dot;

        // instantly throw racer some distance away from the collision
        tmpV.set(velocity).scl(.1f);
        racer.getScene().modelInstance.transform.translate(tmpV);
    }


    // Game controller interface
    //
    //

    // rotate view left/right
    // we only get events when the stick angle changes so once it is fully left or fully right we don't get events anymore until the stick is released.
    public void horizontalAxisMoved(float value) {       // -1 to 1

        stickHorizontal = value;
        stickSteering = true;
    }

    public void verticalAxisMoved(float value) {       // -1 to 1
        stickVertical = value;
    }

    public void boostAxisMoved(float value) {       // -1 to 1
        stickBoost = value;
    }

    float moveTowards(float current, float target, float maxSpeed){
        float delta = target - current;
        if(Math.abs(delta) < maxSpeed)
            return target;
        return current + maxSpeed * Math.signum(delta);
    }

}
