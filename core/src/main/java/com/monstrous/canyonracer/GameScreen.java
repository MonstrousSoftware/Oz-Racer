package com.monstrous.canyonracer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.monstrous.canyonracer.gui.GUI;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.lights.DirectionalShadowLight;

import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;
import net.mgsx.gltf.scene3d.utils.IBLBuilder;


public class GameScreen implements Screen {
    private static final String FILE_NAME = "models/Feisar.gltf";
    private static final int SHADOW_MAP_SIZE = 2048;

    private SceneManager sceneManager;
    private SceneAsset sceneAsset;
    private Scene scene;
    private PerspectiveCamera camera;
    private Cubemap diffuseCubemap;
    private Cubemap environmentCubemap;
    private Cubemap specularCubemap;
    private Texture brdfLUT;
    private float time;
    private SceneSkybox skybox;
    private DirectionalShadowLight light;
    private CameraInputController camController;
    private GUI gui;
    private Model frustumModel;
    private Vector3 lightPosition = new Vector3();
    private Vector3 lightCentre = new Vector3();
    private ModelBatch modelBatch;
    private Array<ModelInstance> instances;
    private Model arrowModel;

    @Override
    public void show() {

        gui = new GUI(this);

        // create scene
        sceneAsset = new GLTFLoader().load(Gdx.files.internal(FILE_NAME));
        scene = new Scene(sceneAsset.scene);
        sceneManager = new SceneManager();
        sceneManager.addScene(scene);

        // setup camera (The BoomBox model is very small so you may need to adapt camera settings for your scene)
        camera = new PerspectiveCamera(60f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        float d = 75f;
        camera.near = 0.1f;
        camera.far = 1000f;
        camera.position.set(0, d / 3f, -d);
        camera.update();
        sceneManager.setCamera(camera);

        // input multiplexer to input to GUI and to cam controller
        InputMultiplexer im = new InputMultiplexer();
        Gdx.input.setInputProcessor(im);
        camController = new CameraInputController(camera);
        im.addProcessor(gui.stage);
        im.addProcessor(camController);

        modelBatch = new ModelBatch();
        instances = new Array<>();

        buildEnvironment();

        debugLight();
    }

    public void buildEnvironment() {

        //sceneManager.environment.clear();

        sceneManager.environment.set(new PBRFloatAttribute(PBRFloatAttribute.ShadowBias, Settings.shadowBias));

        // setup light


        if(light != null)
            sceneManager.environment.remove(light);
        // set the light parameters so that your area of interest is in the shadow light frustum
        // but keep it reasonably tight to keep sharper shadows
        lightPosition = new Vector3(0,135,0);    // even though this is a directional light and is "infinitely far away", use this to set the near plane
        float farPlane = 300;
        float nearPlane = 0;
        float VP_SIZE = 300f;
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

        //sceneManager.setAmbientLight(0.2f);
        sceneManager.setAmbientLight(Settings.ambientLightLevel);
        sceneManager.environment.set(new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdfLUT));
        sceneManager.environment.set(PBRCubemapAttribute.createSpecularEnv(specularCubemap));
        sceneManager.environment.set(PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap));

        // setup skybox
        skybox = new SceneSkybox(environmentCubemap);
        sceneManager.setSkyBox(skybox);
    }

    @Override
    public void render(float deltaTime) {
        time += deltaTime;

        // animate camera
        //Gdx.app.log("cam pos", ""+camera.position);
        camera.up.set(Vector3.Y);
        camera.lookAt(Vector3.Zero);
        camera.update();
        camController.update();

        // render
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        sceneManager.update(deltaTime);
        sceneManager.render();

        if(Settings.showLightBox) {
            modelBatch.begin(sceneManager.camera);
            modelBatch.render(instances);
            modelBatch.end();
        }

        gui.render(deltaTime);
    }

    @Override
    public void resize(int width, int height) {
        // Resize your screen here. The parameters represent the new window size.
        sceneManager.updateViewport(width, height);

        gui.resize(width, height);
    }

    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void hide() {
        // This method is called when another screen replaces this one.
    }

    @Override
    public void dispose() {
        // Destroy screen's assets here.
        sceneManager.dispose();
        sceneAsset.dispose();
        environmentCubemap.dispose();
        diffuseCubemap.dispose();
        specularCubemap.dispose();
        brdfLUT.dispose();
        skybox.dispose();
        gui.dispose();
        modelBatch.dispose();
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
