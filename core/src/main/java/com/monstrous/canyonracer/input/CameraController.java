package com.monstrous.canyonracer.input;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.monstrous.canyonracer.Settings;

public class CameraController extends InputAdapter {

    private final Camera camera;
    private final Vector3 offset = new Vector3();
    private final Vector3 focalOffset = new Vector3();
    private float distance = 75f;
    private final Vector3 cameraTargetPosition = new Vector3();
    public boolean skyCamMode = false;

    public CameraController(Camera camera ) {
        this.camera = camera;
        focalOffset.set(0,0,50);
    }

    // viewDirection is unit forward vector pointing for the racer
    public void update ( Vector3 playerPosition, Vector3 viewDirection, float deltaTime ) {

        // camera is at some position behind and above the player
        cameraTargetPosition.set(viewDirection).scl(-distance);       // distance behind player
        cameraTargetPosition.y = distance/3;                          // and above
        cameraTargetPosition.add(playerPosition);

        // smoothly slerp the camera towards the desired position
        camera.position.slerp(cameraTargetPosition, Settings.cameraSlerpFactor*deltaTime);

        // camera is looking at a point in front of the racer so that racer appears in the bottom half of the screen, not centre screen
        focalOffset.set(viewDirection).scl(250).add(playerPosition);
        camera.lookAt(focalOffset);
        camera.up.set(Vector3.Y);

        // top view
        if(skyCamMode) {
            camera.position.set(playerPosition.x, 1000, playerPosition.z);
            camera.up.set(Vector3.Z);
            camera.lookAt(playerPosition);
        }

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
