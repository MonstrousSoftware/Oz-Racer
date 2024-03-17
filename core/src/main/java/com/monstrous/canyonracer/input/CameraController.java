package com.monstrous.canyonracer.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.monstrous.canyonracer.Settings;

public class CameraController extends InputAdapter {

    private final PerspectiveCamera camera;
    private final Vector3 focalOffset = new Vector3();
    private float distance = Settings.cameraDistance;
    private final Vector3 cameraTargetPosition = new Vector3();
    public boolean skyCamMode = false;
    private float angle = 0;
    private Vector3 up;

    public CameraController(PerspectiveCamera camera ) {
        this.camera = camera;
        focalOffset.set(0,0,5);
        if(skyCamMode)
            distance = 200;
        up = new Vector3(Vector3.Y);
        setCameraUpSideDown( Settings.cameraInverted );
    }

    public void setCameraUpSideDown( boolean mode ){
        if(mode)
            up.y = -1;
        else
            up.y = 1;
    }

    public void setDistance( float d ){
        distance = d;
    }

    // viewDirection is unit forward vector pointing for the racer
    public void update ( Vector3 playerPosition, Vector3 viewDirection, float deltaTime ) {

        // camera is at some position behind and above the player
        cameraTargetPosition.set(viewDirection).scl(-distance);       // distance behind player
        cameraTargetPosition.y = distance/3;                          // and above
        cameraTargetPosition.add(playerPosition);


        // smoothly slerp the camera towards the desired position
        float alpha = MathUtils.clamp(Settings.cameraSlerpFactor*deltaTime, 0.0f, 1.0f);    // make sure alpha <= 1 even at low frame rates
        camera.position.slerp(cameraTargetPosition, alpha);

       camera.position.set(cameraTargetPosition);

        angle += deltaTime*50f;
       // camera.position.y += 0.01f*Math.sin(angle);    // camera vibration

        // camera is looking at a point in front of the racer so that racer appears in the bottom half of the screen, not centre screen
        focalOffset.set(viewDirection).scl(65).add(playerPosition);
        camera.lookAt(focalOffset);
        camera.up.set(up);
        //Gdx.app.log("lookat", focalOffset.toString());

        // top view
        if(skyCamMode) {
            camera.position.set(playerPosition.x, distance, playerPosition.z);
            camera.up.set(Vector3.Z);
            camera.lookAt(playerPosition);
        }

        camera.update(true);
    }


    public void setFOV(float fov){
        camera.fieldOfView = fov;
    }

    @Override
    public boolean scrolled (float amountX, float amountY) {
        return zoom(amountY );
    }

    private boolean zoom (float amount) {
        if(amount < 0 && distance < 7.5f)
            return false;
        if(amount > 0 && distance > 50f && !skyCamMode )
            return false;
        distance += amount;
        return true;
    }
}
