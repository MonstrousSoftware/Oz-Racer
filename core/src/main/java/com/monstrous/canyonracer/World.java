package com.monstrous.canyonracer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.monstrous.canyonracer.collision.Colliders;
import com.monstrous.canyonracer.collision.CollidersView;
import com.monstrous.canyonracer.input.PlayerController;
import com.monstrous.canyonracer.screens.Main;
import com.monstrous.canyonracer.terrain.Terrain;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class World implements Disposable {
    private final Array<GameObject> gameObjects;
    public Colliders colliders;
    public CollidersView collidersView;
    private SceneAsset sceneAsset;
    public GameObject racer;
    public final GameObject intactRacer;
    public final GameObject brokenRacer;
    //public final GameObject enemy1;
    private GameObject finish;
    private Vector3 finishPosition = new Vector3();
    private GameObject start;
    private Vector3 startPosition = new Vector3();
    public final PlayerController playerController;
    public Vector3 playerPosition;
    //public final Vector3 enemyPosition;
    public final Terrain terrain;
    //private final EnemyController enemyController;
    public Turbines turbines;
    public Rocks rocks;
    public boolean collided;
    private StringBuffer sb = new StringBuffer();
    public static float raceTime = 0;
    public static String raceTimeString;
    public static boolean racing = false;
    public static boolean finished = false;
    public static float nitroLevel = 75f;
    public static float healthPercentage = 100;
    public LeaderBoard leaderBoard;
    public String playerName = "Bob";
    public int attempt = -1;


    public World() {
        gameObjects = new Array<>();
        colliders = new Colliders();
        collidersView = new CollidersView();

        sceneAsset = Main.assets.sceneAssetGame;
        for (Node node : sceneAsset.scene.model.nodes) {  // print some debug info
            Gdx.app.log("Node ", node.id);
        }

        playerController = new PlayerController();

        playerPosition = new Vector3(-3350, 68, 30);

        intactRacer = spawnObject("Feisar_Ship", true, playerPosition);
        brokenRacer = spawnObject("BrokenRacer", true, new Vector3(0,-100,0)); // out of sight
        racer = intactRacer;

//        enemyPosition = new Vector3(4, 8, 6);
//        enemy1 = spawnObject("Feisar_Ship", true, enemyPosition);
//        enemyController = new EnemyController();

        terrain = new Terrain(playerPosition);
        //path = new Path(terrain);

        turbines = new Turbines(this);

        placeCheckPoints();

        sceneAsset = Main.assets.sceneAssetRocks;
        rocks = new Rocks(this);

        restart();

        leaderBoard = new LeaderBoard(10);
    }

    // restart the race
    public void restart(){
        racer = intactRacer;
        float y = terrain.getHeight(-3350, 30);
        playerPosition.set(-3350, y+5f, 30);
        playerController.restart(90f);
        // get player position
        racer.getScene().modelInstance.transform.setTranslation(playerPosition);

        racing = false;
        finished = false;
        raceTime = 0;
        healthPercentage = 100f;
        nitroLevel = 100;

        attempt++;
    }

    private void placeCheckPoints() {
        start = placeCheckPoint(-3300, 30, 80);
        finish = placeCheckPoint(3300, 0, 80);

        start.getScene().modelInstance.transform.getTranslation(startPosition);
        finish.getScene().modelInstance.transform.getTranslation(finishPosition);
    }

    private GameObject placeCheckPoint(float x, float z, float angle) {
        float y = terrain.getHeight(x, z);
        GameObject go = spawnObject("CheckPoint", true, new Vector3(x, y, z));
        go.getScene().modelInstance.transform.rotate(Vector3.Y, angle);
        colliders.addCollider(go.getScene().modelInstance, y+5f);

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
        if(healthPercentage <= 0)
            racing = false;
        if (racing)
            raceTime += deltaTime;
        formatRaceTimeString();

        playerController.update(racer, this, terrain, deltaTime);
        //enemyController.update(enemy1, terrain, deltaTime);
        turbines.update(deltaTime);

        // update player position variable
        racer.getScene().modelInstance.transform.getTranslation(playerPosition);

        // gates are 66 units wide
        if (!racing && startPosition.dst2(playerPosition) < 1000 ) {
            racing = true;
            finished = false;
            raceTime = 0;
            Gdx.app.log("started", "");
        }
        if (racing && finishPosition.dst2(playerPosition) < 1000 ) {
            racing = false;
            finished = true;
            Gdx.app.log("finished", "");
            leaderBoard.add(playerName, attempt, true, raceTimeString, (int)(100*raceTime));
        }


        boolean wasCollided = collided;
        collided = colliders.inCollision(playerPosition, colliderPosition);
        if (collided && !wasCollided) {
            Main.assets.COLLISION.play();
            healthPercentage -= Settings.collisionDamage * playerController.getSpeed();
            if(healthPercentage > 0) {
                // throw player away from the collider
                Vector3 normal = colliderPosition.sub(playerPosition);
                normal.y = 0;  // horizontal impulse only
                normal.nor();
                playerController.collisionImpact(normal);
            } else {
                // swap racer mode for model of broken racer
                brokenRacer.getScene().modelInstance.transform.set(racer.getScene().modelInstance.transform);
                intactRacer.getScene().modelInstance.transform.translate(0, -100, 0);
                racer = brokenRacer;
                leaderBoard.add(playerName, attempt, false, raceTimeString, (int)(100*raceTime));
            }

        }
    }


    public GameObject spawnObject(String name, boolean resetPosition, Vector3 position) {
        Scene scene = loadNode(name, resetPosition, position);

        GameObject go = new GameObject(scene);
        gameObjects.add(go);

        return go;
    }

    public Scene loadNode(String nodeName, boolean resetPosition, Vector3 position) {
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
            sb.append(':');
            sb.append(fractions);
            raceTimeString = sb.toString();
        }
         else
            raceTimeString ="";
    }

    @Override
    public void dispose() {

        sceneAsset.dispose();
        collidersView.dispose();
    }
}
