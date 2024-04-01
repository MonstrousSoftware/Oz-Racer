package com.monstrous.canyonracer.screens;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.monstrous.canyonracer.Settings;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;
import net.mgsx.gltf.scene3d.utils.IBLBuilder;


// main menu

public class MainMenuScreen extends MenuScreen {

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
    private DirectionalLightEx light;
    private CameraInputController camController;

    public MainMenuScreen(Main game) {
        super(game);
    }




    @Override
    public void show() {
        super.show();

        // create scene
        sceneAsset = Main.assets.sceneAssetGame;
        sceneManager = new SceneManager();
        scene = new Scene(sceneAsset.scene,"Feisar_Ship");
        sceneManager.addScene(scene);
        scene = new Scene(sceneAsset.scene,"BackDrop");
        sceneManager.addScene(scene);

        camera = new PerspectiveCamera(60f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = 0.01f;
        camera.far = 1000f;
        sceneManager.setCamera(camera);

        sceneManager.environment.set(new PBRFloatAttribute(PBRFloatAttribute.ShadowBias, 0.001f));

        // setup light
        light = new DirectionalLightEx();

        light.direction.set(1, -3, -1).nor();
        light.color.set(Color.WHITE);
        light.intensity = 8f;
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
    }

    @Override
    public void render(float deltaTime) {
        time += deltaTime;

        float t = MathUtils.PI * Math.abs(MathUtils.sin(time/5f));
        float t2 = .3f; //MathUtils.PI * Math.abs(MathUtils.cos(time/12f));

        // animate camera
        camera.position.setFromSpherical(t*MathUtils.PI/4, t2).scl(10f);
        camera.up.set(Vector3.Y);
        camera.lookAt(Vector3.Zero);
        camera.update();

        // render
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        sceneManager.update(deltaTime);
        sceneManager.render();

        super.render(deltaTime);    // menu overlay
    }

    // don't override resize(): rely on parent class resize


    @Override
    protected void rebuild() {
        stage.clear();


        Table screenTable = new Table();
        screenTable.setFillParent(true);

        Image title = new Image( Main.assets.title );

        TextButton play = new TextButton("Play Game", skin);
        TextButton region = new TextButton("Monitor Orientation", skin);
        TextButton options = new TextButton("Options", skin);
        TextButton quit = new TextButton("Quit", skin);

        float pad = 17f;
        //screenTable.debug();
        screenTable.top();
        screenTable.add(title).pad(50,10,150,10).row();

        Table menu = new Table();
        menu.add(play).pad(pad).left().bottom().row();
        menu.add(region).pad(pad).left().row();
        menu.add(options).pad(pad).left().row();

        // hide quit on web unless we have an outro screen
        if(!(Gdx.app.getType() == Application.ApplicationType.WebGL) )
            menu.add(quit).pad(pad).left().row();

        screenTable.add(menu).left().bottom().expand();
        screenTable.pack();

        screenTable.setColor(1,1,1,0);                   // set alpha to zero
        screenTable.addAction(Actions.fadeIn(2f));           // fade in
        stage.addActor(screenTable);


        Table versionTable = new Table();
        versionTable.setFillParent(true);
        versionTable.add(new Label(Settings.version, Main.assets.debugSkin)).right().bottom().expand();
        versionTable.pack();
        stage.addActor(versionTable);

        play.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                playSelectNoise();
                game.setScreen(new PreGameScreen( game ));
            }
        });

        options.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                playSelectNoise();
                game.setScreen(new OptionsScreen( game ));
            }
        });

        region.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                playSelectNoise();
                game.setScreen(new RegionScreen( game ));
            }
        });

        quit.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                playSelectNoise();
                //game.musicManager.stopMusic();
                Gdx.app.exit();
            }
        });

        // set up for keyboard/controller navigation
        if(Settings.supportControllers) {
            stage.clearFocusableActors();
            stage.addFocusableActor(play);
            stage.addFocusableActor(region);
            stage.addFocusableActor(options);
            stage.addFocusableActor(quit);
            stage.setFocusedActor(play);
            super.focusActor(play);    // highlight focused actor

        }
    }

    @Override
    public void dispose() {
        // Destroy screen's assets here.
        sceneManager.dispose();
        environmentCubemap.dispose();
        diffuseCubemap.dispose();
        specularCubemap.dispose();
        brdfLUT.dispose();
        skybox.dispose();
    }


}
