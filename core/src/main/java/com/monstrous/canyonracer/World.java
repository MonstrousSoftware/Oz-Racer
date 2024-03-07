package com.monstrous.canyonracer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.monstrous.canyonracer.input.PlayerController;
import com.monstrous.canyonracer.terrain.Terrain;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class World implements Disposable {
    private static final String GLTF_FILE = "models/Feisar.gltf";

    private final Array<GameObject> gameObjects;
    private SceneAsset sceneAsset;
    public final GameObject racer;
    public final PlayerController playerController;
    public final Vector3 playerPosition;
    public final Terrain terrain;

    public World() {
        gameObjects = new Array<>();

        sceneAsset = new GLTFLoader().load(Gdx.files.internal(GLTF_FILE));
        for(Node node : sceneAsset.scene.model.nodes){  // print some debug info
            Gdx.app.log("Node ", node.id);
        }

        playerPosition = new Vector3(0,8,0);
        racer = spawnObject("Feisar_Ship", true, playerPosition);
        playerController = new PlayerController();

        //spawnObject("Ground", true, new Vector3(0,0,0));
        spawnObject("TestCube", true, new Vector3(0,0,0));
        spawnObject("Rock", true, new Vector3(0,0,0));

        terrain = new Terrain();
    }

    public int getNumGameObjects() {
        return gameObjects.size;
    }

    public GameObject getGameObject(int index) {
        return gameObjects.get(index);
    }

    public void update( float deltaTime ){
        playerController.update(racer, terrain, deltaTime);

        // update player position variable
        racer.getScene().modelInstance.transform.getTranslation(playerPosition);
    }


    public GameObject spawnObject(String name, boolean resetPosition,Vector3 position){
        Scene scene = loadNode( name, resetPosition, position );

        GameObject go = new GameObject(scene);
        gameObjects.add(go);

        return go;
    }

    private Scene loadNode( String nodeName, boolean resetPosition, Vector3 position ) {
        Scene scene = new Scene(sceneAsset.scene, nodeName);
        if(scene.modelInstance.nodes.size == 0)
            throw new RuntimeException("Cannot find node in GLTF file: " + nodeName);
        applyNodeTransform(resetPosition, scene.modelInstance, scene.modelInstance.nodes.first());         // incorporate nodes' transform into model instance transform
        scene.modelInstance.transform.translate(position);
        return scene;
    }

    private void applyNodeTransform(boolean resetPosition, ModelInstance modelInstance, Node node ){
        if(!resetPosition)
            modelInstance.transform.mul(node.globalTransform);
        node.translation.set(0,0,0);
        node.scale.set(1,1,1);
        node.rotation.idt();
        modelInstance.calculateTransforms();
    }

//    private GameObject spawnTerrain() {
//        for(ModelInstance instance : terrain.instances) {
//
//            Scene scene = new Scene(instance);
//            GameObject go = new GameObject(scene);
//            gameObjects.add(go);
//        }
//        return null;
//    }


    @Override
    public void dispose() {

        sceneAsset.dispose();

    }
}
