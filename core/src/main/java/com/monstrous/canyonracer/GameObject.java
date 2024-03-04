package com.monstrous.canyonracer;

import net.mgsx.gltf.scene3d.scene.Scene;

public class GameObject {
    private Scene scene;

    public GameObject(Scene scene) {
        this.scene = scene;
    }

    public Scene getScene() {
        return scene;
    }
}
