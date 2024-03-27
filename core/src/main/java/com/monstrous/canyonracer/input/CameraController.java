package com.monstrous.canyonracer.input;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.monstrous.canyonracer.Settings;

public class CameraController extends InputAdapter {

    private final PerspectiveCamera camera;
    private final Vector3 playerPosition = new Vector3();
    private final Vector3 viewDirection = new Vector3();
    private final Vector3 focalOffset = new Vector3();
    private float distance;
    private final Vector3 cameraTargetPosition = new Vector3();
    public boolean skyCamMode = false;
    private Vector3 up;

    public CameraController(PerspectiveCamera camera ) {
        this.camera = camera;
        focalOffset.set(0,0,5);
        if(skyCamMode)
            distance = 200;
        up = new Vector3(Vector3.Y);
        setCameraUpSideDown( Settings.cameraInverted );
        distance = 500f;
    }

    public void setCameraUpSideDown( boolean mode ){
        if(mode)
            up.y = -1;
        else
            up.y = 1;
    }

    // can be used to temporarily move camera further away
    public void setDistance( float d ){
        distance = d;
    }

    // viewDirection is unit forward vector pointing for the racer
    public void update ( Matrix4 targetTransform, float deltaTime ) {

        // get position and forward direction from racer's transform
        targetTransform.getTranslation(playerPosition);
        viewDirection.set(Vector3.Z);
        viewDirection.rot(targetTransform);

        // smooth from actual distance to desired distance
        // not frame rate independent
        distance = MathUtils.lerp(distance, Settings.cameraDistance, 0.05f );

        // camera is at some position behind and above the player
        cameraTargetPosition.set(viewDirection).scl(-distance);       // distance behind player
        cameraTargetPosition.y = distance/3;                          // and above
        cameraTargetPosition.add(playerPosition);


        // smoothly slerp the camera towards the desired position
//        float alpha = MathUtils.clamp(Settings.cameraSlerpFactor*deltaTime, 0.0f, 1.0f);    // make sure alpha <= 1 even at low frame rates
//        if(alpha > 0.99f)
//            camera.position.set(cameraTargetPosition);
//        else
//            camera.position.slerp(cameraTargetPosition, alpha);
        // Slerping makes the camera jitter forward and back
        camera.position.set(cameraTargetPosition);

        // camera is looking at a point in front of the racer so that racer appears in the bottom half of the screen, not centre screen
        focalOffset.set(viewDirection).scl(65).add(playerPosition);
        camera.lookAt(focalOffset);
        camera.up.set(up);

        updateCameraShake(camera, deltaTime);

        // top view
        if(skyCamMode) {
            camera.position.set(playerPosition.x, distance, playerPosition.z);
            camera.up.set(Vector3.Z);
            camera.lookAt(playerPosition);
        }

        camera.update(true);
    }

    private float shakeScale = 0;

    public void startCameraShake(){
        shakeScale = 3f;
    }

    public void updateCameraShake( PerspectiveCamera camera, float deltaTime ) {
        if(shakeScale <= 0)
            return;
        camera.position.y += (Math.random() - 0.5f)* shakeScale;
        shakeScale -= deltaTime * 10f;
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

    float moveTowards(float current, float target, float maxSpeed){
        float delta = target - current;
        if(Math.abs(delta) < maxSpeed)
            return target;
        return current + maxSpeed * Math.signum(delta);
    }
}
