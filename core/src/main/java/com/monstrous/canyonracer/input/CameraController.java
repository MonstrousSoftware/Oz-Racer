package com.monstrous.canyonracer.input;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;

public class CameraController extends InputAdapter {

    private final Camera camera;
    private final Vector3 offset = new Vector3();
    private final Vector3 focalOffset = new Vector3();
    private float distance = 75f;

    public CameraController(Camera camera ) {
        this.camera = camera;
        offset.set(0, distance/3f, -distance);
        focalOffset.set(0,0,50);
    }

    public void update ( Vector3 playerPosition, Vector3 viewDirection ) {

        // camera is at some position behind and above the player

        camera.position.set(playerPosition);
        offset.nor().scl(distance);                   // scale for camera distance
        camera.position.add(offset);

        // camera is looking at a point in front of the racer so that racer appears in the bottom half of the screen, not centre screen
        focalOffset.set(playerPosition);
        focalOffset.z += 250;
        camera.lookAt(focalOffset);
        camera.up.set(Vector3.Y);

        camera.update(true);
    }

    @Override
    public boolean scrolled (float amountX, float amountY) {
        return zoom(amountY );
    }

    private boolean zoom (float amount) {
        if(amount < 0 && distance < 75f)
            return false;
        if(amount > 0 && distance > 500f)
            return false;
        distance += 10f*amount;
        return true;
    }
}
