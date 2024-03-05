package com.monstrous.canyonracer.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntIntMap;
import com.monstrous.canyonracer.GameObject;

public class PlayerController extends InputAdapter {

    public static float MAX_SPEED = 600f;
    public static float ACCELERATION = 60f;
    public static float DECELERATION = 120f;
    public static float TURN_RATE = 90f;
    public static float MAX_TURN = 35f;

    public static int forwardKey = Input.Keys.W;
    public static int backwardKey = Input.Keys.S;
    public static int turnLeftKey = Input.Keys.A;
    public static int turnRightKey = Input.Keys.D;

    private float speed = 0f;
    private float turnAngle = 0f;
    private float rotation = 0f;

    private final IntIntMap keys = new IntIntMap();
    private Vector3 forwardDirection;

    public PlayerController() {
        forwardDirection = new Vector3(0,0,1);
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

    public void update (GameObject racer, float deltaTime ) {


        if (keys.containsKey(forwardKey) && speed < MAX_SPEED)
            speed += deltaTime*ACCELERATION;
        if (keys.containsKey(backwardKey) && speed > 0) {
            speed -= deltaTime * DECELERATION;
            if(speed < 0)
                speed = 0;
        }



        if (keys.containsKey(turnLeftKey) && turnAngle < MAX_TURN)
            turnAngle += deltaTime * speed / 6f;                            // turn rate depends on speed
        if (keys.containsKey(turnRightKey) && turnAngle > -MAX_TURN)
            turnAngle -= deltaTime * speed / 6f;

        turn(racer, turnAngle*deltaTime);

        moveForward(racer, deltaTime * speed);

    }

    private Vector3 tmpV = new Vector3();

    private void moveForward(GameObject racer, float distance){
        tmpV.set(forwardDirection).scl(distance);
        racer.getScene().modelInstance.transform.translate(tmpV);
    }

    private void turn(GameObject racer, float degrees){
        rotation += degrees;
        forwardDirection.set(0,0,1);
        forwardDirection.rotate(Vector3.Y, rotation);
        Matrix4 transform = racer.getScene().modelInstance.transform;
        transform.getTranslation(tmpV);
        tmpV.y = 20f;
        transform.setToRotation(Vector3.Y, rotation);
        transform.rotate(Vector3.Z, -turnAngle);                // bank
        transform.setTranslation(tmpV);
        Gdx.app.log("turn", "rot:"+rotation+" ta:"+turnAngle+" fwd:"+forwardDirection);
    }


}
