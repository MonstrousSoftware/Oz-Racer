package com.monstrous.canyonracer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.monstrous.canyonracer.input.EnemyController;
import com.monstrous.canyonracer.input.PlayerController;
import com.monstrous.canyonracer.screens.Main;
import com.monstrous.canyonracer.terrain.Terrain;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class World implements Disposable {
    private static final String GLTF_FILE = "models/OzRacer.gltf";
    private static final String GLTF_FILE2 = "models/rocks.gltf";

    private final Array<GameObject> gameObjects;
    private SceneAsset sceneAsset;
    public final GameObject racer;
    public final GameObject enemy1;
    private GameObject finish;
    private Vector3 finishPosition = new Vector3();
    private GameObject start;
    private Vector3 startPosition = new Vector3();
    public final PlayerController playerController;
    public Vector3 playerPosition;
    public final Vector3 enemyPosition;
    public final Terrain terrain;
    private final EnemyController enemyController;
    private final Turbines turbines;
    public final Rocks rocks;
    public boolean collided;
    private StringBuffer sb = new StringBuffer();
    public static float raceTime = 0;
    public static String raceTimeString;
    public static boolean racing = false;
    public static boolean finished = false;


    public World() {
        gameObjects = new Array<>();

        sceneAsset = new GLTFLoader().load(Gdx.files.internal(GLTF_FILE));
        for (Node node : sceneAsset.scene.model.nodes) {  // print some debug info
            Gdx.app.log("Node ", node.id);
        }

        playerController = new PlayerController();

        playerPosition = new Vector3(-3350, 68, 30);
//        playerController.rotation = 90f;

//        playerPosition = new Vector3(0, 8,0);
        racer = spawnObject("Feisar_Ship", true, playerPosition);

        enemyPosition = new Vector3(4, 8, 6);
        enemy1 = spawnObject("Feisar_Ship", true, enemyPosition);
        enemyController = new EnemyController();

        spawnObject("TestCube", true, new Vector3(0, 0, 0));
        spawnObject("Marker", true, new Vector3(10, 5, 80));

        spawnObject("Arrow", true, new Vector3(-3350, 90, 30));

        terrain = new Terrain(playerPosition);
        //path = new Path(terrain);

        turbines = new Turbines(this);

        placeCheckPoints();

        //importRocks();
        sceneAsset = new GLTFLoader().load(Gdx.files.internal(GLTF_FILE2));
        rocks = new Rocks(this);

        restart();
    }

    // restart the race
    public void restart(){
        playerPosition.set(-3350, 68, 30);
        playerController.restart(90f);
        //playerController.rotation = 90f;
        racer.getScene().modelInstance.transform.setTranslation(playerPosition);

        racing = false;
        finished = false;
        raceTime = 0;
    }

    private void placeCheckPoints() {
        start = placeCheckPoint(-3300, 30, 80);
        finish = placeCheckPoint(3300, 0, 80);

        start.getScene().modelInstance.transform.getTranslation(startPosition);
        finish.getScene().modelInstance.transform.getTranslation(finishPosition);

//        placeCheckPoint(-2531, -278, 120);
//        placeCheckPoint(-2092, -978, 80);
//        placeCheckPoint(-1518, -641, 80);
//        placeCheckPoint(-835, -16, 116);
//        placeCheckPoint(-336, -523, 109);
    }

    private GameObject placeCheckPoint(float x, float z, float angle) {
        float y = terrain.getHeight(x, z);
        GameObject go = spawnObject("CheckPoint", true, new Vector3(x, y, z));
        go.getScene().modelInstance.transform.rotate(Vector3.Y, angle);
        return go;
    }


    public int getNumGameObjects() {
        return gameObjects.size;
    }

    public GameObject getGameObject(int index) {
        return gameObjects.get(index);
    }

    private Vector3 colliderPosition = new Vector3();

    public void update(float deltaTime) {
        if (racing)
            raceTime += deltaTime;
        formatRaceTimeString();

        playerController.update(racer, this, terrain, deltaTime);
        enemyController.update(enemy1, terrain, deltaTime);
        turbines.update(deltaTime);

        // update player position variable
        racer.getScene().modelInstance.transform.getTranslation(playerPosition);

        if (!racing && startPosition.dst2(playerPosition) < 250) {
            racing = true;
            finished = false;
            raceTime = 0;
            Gdx.app.log("started", "");
        }
        if (racing && finishPosition.dst2(playerPosition) < 250) {
            racing = false;
            finished = true;
            Gdx.app.log("finished", "");
        }


        boolean wasCollided = collided;
        collided = rocks.inCollision(playerPosition, colliderPosition);
        if (collided && !wasCollided) {
            Main.assets.COLLISION.play();
            // throw player away from the collider
            Vector3 normal = colliderPosition.sub(playerPosition);
            normal.y = 0;  // horizontal impulse only
            normal.nor();
            //racer.getScene().modelInstance.transform.translate(impulse);
            playerController.collisionImpact(normal);
            // perhaps add some camera shake?
        }
    }


    public GameObject spawnObject(String name, boolean resetPosition, Vector3 position) {
        Scene scene = loadNode(name, resetPosition, position);

        GameObject go = new GameObject(scene);
        gameObjects.add(go);

        return go;
    }

    private Scene loadNode(String nodeName, boolean resetPosition, Vector3 position) {
        Scene scene = new Scene(sceneAsset.scene, nodeName);
        if (scene.modelInstance.nodes.size == 0)
            throw new RuntimeException("Cannot find node in GLTF file: " + nodeName);
        applyNodeTransform(resetPosition, scene.modelInstance, scene.modelInstance.nodes.first());         // incorporate nodes' transform into model instance transform
        scene.modelInstance.transform.translate(position);
        return scene;
    }

    private void applyNodeTransform(boolean resetPosition, ModelInstance modelInstance, Node node) {
        if (!resetPosition)
            modelInstance.transform.mul(node.globalTransform);
        node.translation.set(0, 0, 0);
        node.scale.set(1, 1, 1);
        node.rotation.idt();
        modelInstance.calculateTransforms();
    }

    private void formatRaceTimeString() {
        if(World.racing ||World.finished )
        {
            int seconds = (int) World.raceTime;
            int fractions = (int) (10 * (World.raceTime - seconds));
            sb.setLength(0);
            sb.append(seconds);
            sb.append('.');
            sb.append(fractions);
            raceTimeString = sb.toString();
        }
         else
            raceTimeString ="";
    }

    @Override
    public void dispose() {

        sceneAsset.dispose();

    }
}
