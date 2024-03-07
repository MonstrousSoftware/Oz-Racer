package com.monstrous.canyonracer.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntIntMap;
import com.monstrous.canyonracer.GameObject;
import com.monstrous.canyonracer.terrain.Terrain;

public class PlayerController extends InputAdapter {

    public static float MAX_SPEED = 600f;
    public static float ACCELERATION = 260f;
    public static float DECELERATION = 120f;
    public static float TURN_RATE = 90f;
    public static float MAX_TURN = 35f;
    public static float DRAG_FACTOR = 0.01f;


    public static int forwardKey = Input.Keys.W;
    public static int backwardKey = Input.Keys.S;
    public static int turnLeftKey = Input.Keys.A;
    public static int turnRightKey = Input.Keys.D;

    private float speed = 0f;
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
            acceleration = ACCELERATION;
        if (keys.containsKey(backwardKey) ) {
            acceleration = -DECELERATION;
        }
        speed = velocity.len();
        if (keys.containsKey(turnLeftKey) && turnAngle < MAX_TURN)
            turnAngle += deltaTime * TURN_RATE;                            // turn rate depends on speed
        else if (keys.containsKey(turnRightKey) && turnAngle > -MAX_TURN)
            turnAngle -= deltaTime * TURN_RATE; //speed / 6f;

        Matrix4 transform = racer.getScene().modelInstance.transform;
        transform.getTranslation(playerPos);
        targetHeight = 5f+terrain.getHeight(playerPos.x, playerPos.z);
        playerPos.y = MathUtils.lerp(playerPos.y, targetHeight, 20f*deltaTime);
        forwardDirection.set(Vector3.Z);
        forwardDirection.rot(transform);

        // apply acceleration to velocity
        tmpV.set(forwardDirection).scl(deltaTime * acceleration);
        velocity.add(tmpV);

        drag.set(velocity).scl(DRAG_FACTOR);
        velocity.sub(drag);

        // never go backwards
//        if(velocity.dot(forwardDirection)< 0)
//            velocity.set(0,0,0);

        rotation += turnAngle*deltaTime;
        transform.setToRotation(Vector3.Y, rotation);
        transform.rotate(Vector3.Z, -turnAngle);                // bank


        // apply velocity to position
        tmpV.set(velocity).scl(deltaTime);
        playerPos.add(tmpV);
        transform.setTranslation(playerPos);
    }


}
