package com.monstrous.canyonracer.input;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.monstrous.canyonracer.GameObject;
import com.monstrous.canyonracer.Settings;
import com.monstrous.canyonracer.terrain.Terrain;

public class EnemyController {

    public float speed = 0f;
    private float turnAngle = 0f;
    private float rotation = 0f;

    private Vector3 forwardDirection;       // unit vector in forward direction
    private Vector3 velocity;               // velocity vector

    private Vector3 tmpV = new Vector3();
    private Vector3 racerPos = new Vector3();
    private Vector3 drag = new Vector3();
    private float targetHeight;


    public EnemyController() {
        forwardDirection = new Vector3(0,0,1);
        velocity = new Vector3(0,0,0);
    }


    public void update (GameObject racer, Terrain terrain, float deltaTime ) {

        float acceleration = 0f;
//        if (keys.containsKey(forwardKey) )
            acceleration = Settings.acceleration;

        speed = velocity.len();

        // potential idea: turn rate depends on speed
//        if (keys.containsKey(turnLeftKey)) {
//            if (turnAngle < Settings.maxTurn)
//                turnAngle += deltaTime * Settings.turnRate;
//        }
//        else if (keys.containsKey(turnRightKey)) {
//            if( turnAngle > -Settings.maxTurn)
//                turnAngle -= deltaTime *  Settings.turnRate;
//        }
//        else
//            turnAngle -= turnAngle * 2f * deltaTime;      // auto-level
        turnAngle = 10f;


        Matrix4 transform = racer.getScene().modelInstance.transform;
        transform.getTranslation(racerPos);

        // place racer fixed distance above the terrain, i.e. it follows the terrain
        targetHeight = 5f+terrain.getHeight(racerPos.x, racerPos.z);
        racerPos.y = MathUtils.lerp(racerPos.y, targetHeight, Settings.heightLag*deltaTime);     // with a bit of lag

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
        racerPos.add(tmpV);
        transform.setTranslation(racerPos);
    }


}
