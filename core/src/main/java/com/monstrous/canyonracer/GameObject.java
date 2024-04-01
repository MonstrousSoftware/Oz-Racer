package com.monstrous.canyonracer;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import net.mgsx.gltf.scene3d.scene.Scene;

public class GameObject {
    private final Scene scene;
    public final Vector3 center = new Vector3();
    public final Vector3 dimensions = new Vector3();
    public float radius;
    private static final BoundingBox bbox = new BoundingBox();
    private final Vector3 pos = new Vector3();

    public GameObject(Scene scene) {
        this.scene = scene;
        calculateBoundingBox();
    }

    // call this after rotation or scaling (expensive; don't do this per frame)
    public void calculateBoundingBox(){
        scene.modelInstance.calculateBoundingBox(bbox);
        bbox.getCenter(center);
        bbox.getDimensions(dimensions);
        Matrix4 transform = scene.modelInstance.transform;
        dimensions.x *= transform.getScaleX();
        dimensions.y *= transform.getScaleY();
        dimensions.z *= transform.getScaleZ();
        radius = dimensions.len() / 2f;
    }

    public Scene getScene() {
        return scene;
    }

    public boolean isVisible(final Camera cam ) {
        scene.modelInstance.transform.getTranslation(pos);
        pos.add(center);
        return cam.frustum.sphereInFrustum(pos, radius);
    }
}
