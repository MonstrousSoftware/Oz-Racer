package com.monstrous.canyonracer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.monstrous.canyonracer.input.CameraController;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalShadowLight;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;
import net.mgsx.gltf.scene3d.utils.IBLBuilder;

public class GameView {
    private static final int SHADOW_MAP_SIZE = 2048;

    private World world;
    private SceneManager sceneManager;
    private PerspectiveCamera camera;
    private Cubemap diffuseCubemap;
    private Cubemap environmentCubemap;
    private Cubemap specularCubemap;
    private Texture brdfLUT;
    private SceneSkybox skybox;
    private DirectionalShadowLight light;
    private Model frustumModel;
    private Vector3 lightPosition = new Vector3();
    private Vector3 lightCentre = new Vector3();
    private ModelBatch modelBatch;
    private Array<ModelInstance> instances;
    private Model arrowModel;
    public CameraController cameraController;
    private Vector3 playerPos = new Vector3();
    private ParticleEffects particleEffects;
    private ParticleEffect exhaust;

    public GameView(World world) {
        this.world = world;

        sceneManager = new SceneManager();

        camera = new PerspectiveCamera(40f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        float d = 7.5f;
        camera.near = 1f;
        camera.far = 5000f;
        camera.position.set(0, d / 3f, -d);
        camera.update();
        sceneManager.setCamera(camera);

        modelBatch = new ModelBatch();
        instances = new Array<>();

        buildEnvironment();

        debugLight();

        cameraController = new CameraController(camera);

        particleEffects = new ParticleEffects(camera);
        float x = 0;
        float z = 100;
        float y = world.terrain.getHeight(x, z);
        particleEffects.addFire(new Vector3(x, y, z));
        exhaust = particleEffects.addExhaustFumes(world.racer.getScene().modelInstance.transform);
    }

    public void resize(int width, int height) {
        // Resize your screen here. The parameters represent the new window size.
        sceneManager.updateViewport(width, height);
    }

    public Camera getCamera() {
        return sceneManager.camera;
    }

    public void refresh() {
        sceneManager.getRenderableProviders().clear();        // remove all scenes

        // terrain chunks are taken directly from the Terrain class, these are not game objects
        for(Scene scene : world.terrain.scenes)
            sceneManager.addScene(scene, false);

        // add scene for each game object
        int num = world.getNumGameObjects();
        for(int i = 0; i < num; i++){
            Scene scene = world.getGameObject(i).getScene();
            sceneManager.addScene(scene, false);
        }


    }

    private Vector3 playerForward = new Vector3();
    private Matrix4 exhaustTransform = new Matrix4();

    public void render(float deltaTime) {

        // animate camera
        Matrix4 transform = world.racer.getScene().modelInstance.transform;
        transform.getTranslation(playerPos);
        playerForward.set(Vector3.Z);
        playerForward.rot(transform);

        exhaustTransform.set(transform);
        exhaustTransform.translate(0.0f, -0.5f, -5f);           // offset for tail pipe
        exhaust.setTransform(exhaustTransform);

        cameraController.update(playerPos, playerForward, deltaTime);



        light.setCenter(playerPos); // keep shadow light on player so that we have shadows


        refresh();

        // render
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        sceneManager.update(deltaTime);
        sceneManager.render();

        particleEffects.update(deltaTime);
        particleEffects.render(camera);

        if(Settings.showLightBox) {
            modelBatch.begin(sceneManager.camera);
            modelBatch.render(instances);
            modelBatch.end();
        }

    }

    public void dispose() {
        // Destroy assets here.
        sceneManager.dispose();
        environmentCubemap.dispose();
        diffuseCubemap.dispose();
        specularCubemap.dispose();
        brdfLUT.dispose();
        skybox.dispose();
        modelBatch.dispose();
        particleEffects.dispose();
    }

    public void buildEnvironment() {

        sceneManager.environment.set(new PBRFloatAttribute(PBRFloatAttribute.ShadowBias, Settings.shadowBias));

        // setup light


        if(light != null)
            sceneManager.environment.remove(light);
        // set the light parameters so that your area of interest is in the shadow light frustum
        // but keep it reasonably tight to keep sharper shadows
        lightPosition = new Vector3(0,135,0);    // even though this is a directional light and is "infinitely far away", use this to set the near plane
        float farPlane = 30;
        float nearPlane = 0;
        float VP_SIZE = 30f;
        light = new DirectionalShadowLight(SHADOW_MAP_SIZE, SHADOW_MAP_SIZE).setViewport(VP_SIZE,VP_SIZE,nearPlane,farPlane);


        light.direction.set(1, -3, -1).nor();

        // for the directional shadow light we can set the light centre which is the center of the frustum of the orthogonal camera
        // that is used to create the depth buffer.
        // calculate the centre from the light position, near and far planes and light direction
        float halfDepth = (nearPlane + farPlane)/2f;
        lightCentre.set(light.direction).scl(halfDepth).add(lightPosition);

        light.setCenter(lightCentre);           // set the centre of the frustum box

        light.color.set(Color.WHITE);
        light.intensity = Settings.shadowLightLevel;
        sceneManager.environment.add(light);


        // setup quick IBL (image based lighting)
        IBLBuilder iblBuilder = IBLBuilder.createOutdoor(light);
        environmentCubemap = iblBuilder.buildEnvMap(1024);
        diffuseCubemap = iblBuilder.buildIrradianceMap(256);
        specularCubemap = iblBuilder.buildRadianceMap(10);
        iblBuilder.dispose();

        // This texture is provided by the library, no need to have it in your assets.
        brdfLUT = new Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"));

        sceneManager.setAmbientLight(Settings.ambientLightLevel);
        sceneManager.environment.set(new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdfLUT));
        sceneManager.environment.set(PBRCubemapAttribute.createSpecularEnv(specularCubemap));
        sceneManager.environment.set(PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap));

        // setup skybox
        skybox = new SceneSkybox(environmentCubemap);
        sceneManager.setSkyBox(skybox);
    }



    private void debugLight() {

        // force the light.camera to be set to the correct position and direction
        light.begin();
        light.end();

        // create a frustum model (box shape, since the camera is orthogonal) for the directional shadow light
        frustumModel = createFrustumModel(light.getCamera().frustum.planePoints);
        ModelInstance frustumInstance = new ModelInstance(frustumModel, lightPosition);

        // move frustum to world position of light camera
        Vector3 offset = new Vector3(light.getCamera().position).scl(-1);
        frustumInstance.transform.translate(offset);
        instances.add(frustumInstance);

        ModelBuilder modelBuilder = new ModelBuilder();

        // add sphere as light source
        float sz = 1.0f;
        Model ball = modelBuilder.createSphere(sz, sz, sz, 4, 4, new Material(), VertexAttributes.Usage.Position | VertexAttributes.Usage.ColorPacked);
        instances.add(new ModelInstance(ball, light.getCamera().position));

        // light direction as arrow
        Model arrow = modelBuilder.createArrow(Vector3.Zero, light.direction, new Material(), VertexAttributes.Usage.Position | VertexAttributes.Usage.ColorPacked);
        instances.add(new ModelInstance(arrow, lightPosition));

        // XYZ axis reference
        arrowModel = modelBuilder.createXYZCoordinates(15f, new Material(), VertexAttributes.Usage.Position | VertexAttributes.Usage.ColorPacked);
        instances.add(new ModelInstance(arrowModel, Vector3.Zero));

    }

    // from libgdx tests
    public static Model createFrustumModel (final Vector3... p) {
        ModelBuilder builder = new ModelBuilder();
        builder.begin();
        MeshPartBuilder mpb = builder.part("", GL20.GL_LINES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
            new Material(new ColorAttribute(ColorAttribute.Diffuse, Color.WHITE)));
        mpb.vertex(p[0].x, p[0].y, p[0].z, 0, 0, 1, p[1].x, p[1].y, p[1].z, 0, 0, 1, p[2].x, p[2].y, p[2].z, 0, 0, 1, p[3].x,
            p[3].y, p[3].z, 0, 0, 1, // near
            p[4].x, p[4].y, p[4].z, 0, 0, -1, p[5].x, p[5].y, p[5].z, 0, 0, -1, p[6].x, p[6].y, p[6].z, 0, 0, -1, p[7].x, p[7].y,
            p[7].z, 0, 0, -1);
        mpb.index((short)0, (short)1, (short)1, (short)2, (short)2, (short)3, (short)3, (short)0);
        mpb.index((short)4, (short)5, (short)5, (short)6, (short)6, (short)7, (short)7, (short)4);
        mpb.index((short)0, (short)4, (short)1, (short)5, (short)2, (short)6, (short)3, (short)7);
        return builder.end();
    }

}
