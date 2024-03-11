package com.monstrous.canyonracer.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntIntMap;
import com.monstrous.canyonracer.GameObject;
import com.monstrous.canyonracer.Settings;
import com.monstrous.canyonracer.terrain.Terrain;

public class PlayerController extends InputAdapter {

//    public static float ACCELERATION = 260f;
//    public static float TURN_RATE = 390f;
//    public static float MAX_TURN = 95f;
//    public static float DRAG_FACTOR = 0.6f;


    public static int forwardKey = Input.Keys.W;
    public static int turnLeftKey = Input.Keys.A;
    public static int turnRightKey = Input.Keys.D;

    public float speed = 0f;
    private float turnAngle = 0f;
    private float rotation = 0f;

    private final IntIntMap keys = new IntIntMap();
    private Vector3 forwardDirection;       // unit vector in forward direction
    private Vector3 velocity;               // velocity vector

    private Vector3 tmpV = new Vector3();
    private Vector3 playerPos = new Vector3();
    private Vector3 drag = new Vector3();
    private float targetHeight;



    public PlayerController() {

        forwardDirection = new Vector3(0,0,1);
        velocity = new Vector3(0,0,0);
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

        float acceleration = 0f;
        if (keys.containsKey(forwardKey) )
            acceleration = Settings.acceleration;

        speed = velocity.len();
        // potential idea: turn rate depends on speed
        if (keys.containsKey(turnLeftKey)) {
            if (turnAngle < Settings.maxTurn)
                turnAngle += deltaTime * Settings.turnRate;
        }
        else if (keys.containsKey(turnRightKey)) {
            if( turnAngle > -Settings.maxTurn)
                turnAngle -= deltaTime *  Settings.turnRate;
        }
        else
            turnAngle -= turnAngle * 2f * deltaTime;      // auto-level

        Matrix4 transform = racer.getScene().modelInstance.transform;
        transform.getTranslation(playerPos);

        // place racer fixed distance above the terrain, i.e. it follows the terrain
        targetHeight = 5f+terrain.getHeight(playerPos.x, playerPos.z);
        playerPos.y = MathUtils.lerp(playerPos.y, targetHeight, Settings.heightLag*deltaTime);     // with a bit of lag

        // apply rotation to player transform (yaw)
        rotation += turnAngle*deltaTime;
        transform.setToRotation(Vector3.Y, rotation);
        transform.rotate(Vector3.Z, -turnAngle * Settings.bankFactor);                // bank for visual effect

        // determine forward vector of the racer from its transform
        forwardDirection.set(Vector3.Z).rot(transform);


        // apply acceleration to velocity: v' = v + dt.a
        tmpV.set(forwardDirection).scl(deltaTime * acceleration);
        velocity.add(tmpV);

        // drag scales with speed and slows down the racer (deceleration). This limits the top speed.
        drag.set(velocity).scl(deltaTime*Settings.dragFactor);
        velocity.sub(drag);

        // never go backwards
//        if(velocity.dot(forwardDirection)< 0)
//            velocity.set(0,0,0);




        // apply velocity to position
        tmpV.set(velocity).scl(deltaTime);
        playerPos.add(tmpV);
        transform.setTranslation(playerPos);
    }


}
