package com.monstrous.canyonracer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.monstrous.canyonracer.Settings;
import com.monstrous.canyonracer.input.MyControllerMenuStage;
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


public class LogoScreen implements Screen {
    private static final String FILE_NAME = "models/libGDX-logo.gltf";
    private static final int SHADOW_MAP_SIZE = 2048;

    private final Main game;
    private SceneManager sceneManager;
    private SceneAsset sceneAsset;
    private PerspectiveCamera camera;
    private Cubemap diffuseCubemap;
    private Cubemap environmentCubemap;
    private Cubemap specularCubemap;
    private Texture brdfLUT;
    private float time;
    private SceneSkybox skybox;
    private CameraInputController camController;
    private MyControllerMenuStage stage;
    private Controller currentController;

    public LogoScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        // create scene
        sceneAsset = new GLTFLoader().load(Gdx.files.internal(FILE_NAME ));
        Scene scene = new Scene(sceneAsset.scene);
        sceneManager = new SceneManager();
        sceneManager.addScene(scene);

        camera = new PerspectiveCamera(60f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = 0.01f;
        camera.far = 1000f;
        sceneManager.setCamera(camera);

        // input multiplexer to input to GUI and to cam controller
        InputMultiplexer im = new InputMultiplexer();
        Gdx.input.setInputProcessor(im);
        camController = new CameraInputController(camera);
        im.addProcessor(camController);

        sceneManager.environment.set(new PBRFloatAttribute(PBRFloatAttribute.ShadowBias, 0.001f));

        // setup light
        //light = new DirectionalLightEx();
        DirectionalShadowLight light = new DirectionalShadowLight(SHADOW_MAP_SIZE, SHADOW_MAP_SIZE).setViewport(5,5,5,40);

        light.direction.set(1, -3, -1).nor();
        light.color.set(Color.WHITE);
        light.intensity = 5f;
        sceneManager.environment.add(light);


        // setup quick IBL (image based lighting)
        IBLBuilder iblBuilder = IBLBuilder.createOutdoor(light);
        environmentCubemap = iblBuilder.buildEnvMap(1024);
        diffuseCubemap = iblBuilder.buildIrradianceMap(256);
        specularCubemap = iblBuilder.buildRadianceMap(10);
        iblBuilder.dispose();

        // This texture is provided by the library, no need to have it in your assets.
        brdfLUT = new Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"));

        sceneManager.setAmbientLight(0.2f);
        sceneManager.environment.set(new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdfLUT));
        sceneManager.environment.set(PBRCubemapAttribute.createSpecularEnv(specularCubemap));
        sceneManager.environment.set(PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap));

        // setup skybox
        skybox = new SceneSkybox(environmentCubemap);
        sceneManager.setSkyBox(skybox);

        stage = new MyControllerMenuStage(new ScreenViewport());
        if(Settings.supportControllers) {
            currentController = Controllers.getCurrent();
            game.controllerToInputAdapter.setInputProcessor(stage); // forward controller input to stage
        }

        Table screenTable = new Table();
        screenTable.setFillParent(true);

        Image logo = new Image(new Texture(Gdx.files.internal("textures/monstrous.png")));

        Table table = new Table();
        table.add(logo).row();
        table.add(new Label("Monstrous Software", Main.assets.skin)).pad(0,0,100,0).row();

        screenTable.add(table).bottom().expand();

        // fade in
        screenTable.setColor(1,1,1,0);                   // set alpha to zero
        screenTable.addAction(Actions.fadeIn(3f));           // fade in
        stage.addActor(screenTable);

        Table promptTable = new Table();
        promptTable.setFillParent(true);
        Label prompt = new Label("Press Any Key...", Main.assets.skin);
        promptTable.add(prompt).bottom().expand();

        // fade in
        promptTable.setColor(1,1,1,0);                   // set alpha to zero
        promptTable.addAction(Actions.sequence(Actions.delay(3), Actions.fadeIn(3f)));           // fade in
        stage.addActor(promptTable);
    }

    @Override
    public void render(float deltaTime) {
        time += deltaTime;

        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY) ||
            (currentController != null && currentController.getButton(currentController.getMapping().buttonA))) {
            game.setScreen( new MainMenuScreen(game ));
            return;
        }


        float t = MathUtils.PI * Math.abs(MathUtils.sin(time/5f));

        // animate camera
        camera.position.setFromSpherical(t*MathUtils.PI/4, .3f).scl(7f);
        camera.up.set(Vector3.Y);
        camera.lookAt(Vector3.Zero);
        camera.update();
        camController.update();

        // render
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        sceneManager.update(deltaTime);
        sceneManager.render();

        stage.act(deltaTime);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        // Resize your screen here. The parameters represent the new window size.
        sceneManager.updateViewport(width, height);
        stage.getViewport().update(width, height, true);
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
        stage.dispose();
    }
}
