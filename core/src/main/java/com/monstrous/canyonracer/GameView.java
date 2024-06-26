package com.monstrous.canyonracer;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer.FrameBufferBuilder;
import com.badlogic.gdx.graphics.profiling.GLErrorListener;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.monstrous.canyonracer.input.CameraController;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalShadowLight;
import net.mgsx.gltf.scene3d.scene.*;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider;
import net.mgsx.gltf.scene3d.utils.EnvironmentUtil;
import net.mgsx.gltf.scene3d.utils.IBLBuilder;

import static com.badlogic.gdx.Gdx.gl;


// This does the game rendering.
// It owns the SceneManager and retrieves game objects and scenes from World.

public class GameView {
    private static final int SHADOW_MAP_SIZE = 8192;

    private World world;
    public SceneManager sceneManager;
    private PerspectiveCamera camera;
    private Cubemap diffuseCubemap;
    private Cubemap environmentCubemap;
    private Cubemap specularCubemap;
    private Texture brdfLUT;
    private SceneSkybox skybox;
    private DirectionalShadowLight light;
    private Model frustumModel;
    private  ModelInstance frustumInstance;
    private Vector3 lightPosition = new Vector3();
    private Vector3 sunPosition;
    private Vector3 lightCentre = new Vector3();
    private ModelBatch modelBatch;
    private Array<ModelInstance> instances;
    private Model arrowModel;
    public CameraController cameraController;
    public ParticleEffects particleEffects;
    private ParticleEffect exhaust;
    private PostFilter postFilter;
    private FrameBuffer fbo = null;
    private FrameBuffer fboMS = null;
    private LensFlare lensFlare;
    private CascadeShadowMap csm;
    private boolean doProfiling = false;
    private GLProfiler glProfiler;
    private Matrix4 exhaustTransform = new Matrix4();

    public GameView(World world) {
        this.world = world;

        if(doProfiling){
            glProfiler = new GLProfiler(Gdx.graphics);
            glProfiler.enable();
            //glProfiler.setListener(GLErrorListener.THROWING_LISTENER);
        }

        // the default renderable sorter starts overflowing with distances > 1500
        // You may get the exception: Comparison method violates its general contract!
        // Especially on teavm version.
        // So use our dedicated renderable sorter instead
        sceneManager = new SceneManager(PBRShaderProvider.createDefault(0), PBRShaderProvider.createDefaultDepth(0), new MyRenderableSorter());
        ModelBatch depthBatch = new ModelBatch( PBRShaderProvider.createDefaultDepth(0), new MyRenderableSorter());
        sceneManager.setDepthBatch(depthBatch);


        camera = new PerspectiveCamera(Settings.cameraFieldOfView, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = 1f;
        camera.far = 6500f;
        sceneManager.setCamera(camera);

        modelBatch = new ModelBatch();
        instances = new Array<>();

        sunPosition = new Vector3(10,15, -15); // manually aligned with skybox texture
        lensFlare = new LensFlare();

        buildEnvironment();

        cameraController = new CameraController(camera);
        cameraController.update(world.racer.getScene().modelInstance.transform, 0.1f);  // force camera position init

        buildDebugInstances();

        postFilter = new PostFilter();

        particleEffects = new ParticleEffects(camera);
        exhaust = particleEffects.addExhaustFumes(world.racer.getScene().modelInstance.transform);

        // multi-sampling frame buffer only for Desktop on GL ES 3.1+
        if( Gdx.app.getType() != Application.ApplicationType.Desktop || !Gdx.graphics.isGL31Available()) {
            Settings.multiSamplingFrameBufferAvailable = false;
            Settings.useMultiSamplingFrameBuffer = false;       // multi-sampling buffer only supported on desktop
        }
    }

    public void resize(int width, int height) {
        // Resize your screen here. The parameters represent the new window size.
        sceneManager.updateViewport(width, height);
        postFilter.resize(width, height);


        if(fbo != null)
            fbo.dispose();
        if(fboMS != null)
            fboMS.dispose();


        if(Settings.multiSamplingFrameBufferAvailable) {
            int nbSamples = 4;
            fboMS = new FrameBufferBuilder(width, height, nbSamples).addColorRenderBuffer(GL30.GL_RGBA8).addColorRenderBuffer(GL30.GL_RGBA8)
                .addDepthRenderBuffer(GL30.GL_DEPTH_COMPONENT24).build();

            fbo = new FrameBufferBuilder(width, height).addColorTextureAttachment(GL30.GL_RGBA8, GL20.GL_RGBA, GL30.GL_UNSIGNED_BYTE)
                .addColorTextureAttachment(GL30.GL_RGBA8, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE)
                .addDepthTextureAttachment(GL30.GL_DEPTH_COMPONENT24, GL30.GL_UNSIGNED_INT).build();
        }
        else
            fbo = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, true);
    }

    public PerspectiveCamera getCamera() {
        return camera;
    }

    public void refresh() {
        // Here we add all scenes to the scene manager.  This is called every frame, which sounds expensive
        // but actually isn't.
        //
        sceneManager.getRenderableProviders().clear();        // remove all scenes

        // terrain chunks are taken directly from the Terrain class, these are not game objects
        for(Scene scene : world.terrain.scenes)
            sceneManager.addScene(scene, false);

        // rocks and turbines are taken from their model cache
        sceneManager.getRenderableProviders().add(world.rocks.cache);
        sceneManager.getRenderableProviders().add(world.turbines.cache);

        // add scene for each (visible) game object
        int num = world.getNumGameObjects();
        for(int i = 0; i < num; i++){
            GameObject go =  world.getGameObject(i);
            if(go.isVisible(camera)) {
                sceneManager.addScene(go.getScene(), false);
            }
        }

    }


    public void render(float deltaTime) {
        if(doProfiling)
            glProfiler.reset();

        Matrix4 racerTransform = world.racer.getScene().modelInstance.transform;

        // reposition particle effect
        exhaustTransform.set(racerTransform);
        exhaustTransform.translate(0.0f, 0.5f, -6f);           // offset for engine
        exhaust.setTransform(exhaustTransform);

        // animate camera
        cameraController.update(racerTransform, deltaTime);

        refresh();  // fill scene array

        if(Settings.cascadedShadows) {
            csm.setCascades(sceneManager.camera, light, 0, 10f);
        }
        else
            light.setCenter(world.playerPosition); // keep shadow light on player so that we have shadows

        sceneManager.update(deltaTime);
        particleEffects.update(deltaTime);

        if(!Settings.usePostShader)
            renderWorldBasic();
        else if(!Settings.multiSamplingFrameBufferAvailable || !Settings.useMultiSamplingFrameBuffer)
            renderWorldFBO();
        else
            renderWorldFBOAA();

        lensFlare.render(sceneManager.camera, sunPosition);

        //lensFlare.showLightPosition();    // debug

        // debug option: show shadow light frustum (only visible without the post shader)
        if(Settings.showLightBox) {
            modelBatch.begin(sceneManager.camera);
            modelBatch.render(instances);
            modelBatch.end();
        }
        if(doProfiling) {
            Gdx.app.log("draw calls", "drawcalls:" + glProfiler.getDrawCalls()
                + "tex binds:" + glProfiler.getTextureBindings() + " sdr swtch:" + glProfiler.getShaderSwitches() );
        }

    }

    private void renderWorldBasic(){
        gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        sceneManager.render();
        particleEffects.render(camera);
    }

    private void renderWorldFBO(){
        sceneManager.renderShadows();
        fbo.begin();
        gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        sceneManager.renderColors();
        particleEffects.render(camera);
        fbo.end();
        postFilter.render(fbo);
    }

    private void renderWorldFBOAA(){
        sceneManager.renderShadows();
        fboMS.begin();
        gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        sceneManager.renderColors();
        particleEffects.render(camera);
        fboMS.end();
        fboMS.transfer(fbo);        // transfer multi-sampling fbo to output fbo
        postFilter.render(fbo);
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
        postFilter.dispose();
        if(Settings.multiSamplingFrameBufferAvailable)
            fboMS.dispose();
        fbo.dispose();
    }

    // can be called from gui
    public void adjustLighting() {
        light.intensity = Settings.shadowLightLevel;
        sceneManager.setAmbientLight(Settings.ambientLightLevel);
        sceneManager.environment.set(new PBRFloatAttribute(PBRFloatAttribute.ShadowBias, Settings.shadowBias));
    }

    private void buildEnvironment() {

        sceneManager.environment.set(new PBRFloatAttribute(PBRFloatAttribute.ShadowBias, Settings.shadowBias));

        if(Settings.cascadedShadows) {
            csm = new CascadeShadowMap(2);
            sceneManager.setCascadeShadowMap(csm);
        }
        // setup light
        if(light != null)
            sceneManager.environment.remove(light);

        // set the light parameters so that your area of interest is in the shadow light frustum
        // but keep it reasonably tight to keep sharper shadows
        lightPosition = new Vector3(0,135,0);    // even though this is a directional light and is "infinitely far away", use this to set the near plane
        lightPosition.add(world.playerPosition);
        float farPlane = 300;
        float nearPlane = 0;
        float VP_SIZE = 300f;
        light = new DirectionalShadowLight(SHADOW_MAP_SIZE, SHADOW_MAP_SIZE).setViewport(VP_SIZE,VP_SIZE,nearPlane,farPlane);

        light.direction.set(sunPosition).scl(-1).nor();

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
        environmentCubemap = EnvironmentUtil.createCubemap(new InternalFileHandleResolver(),
            "skybox/side-", ".png", EnvironmentUtil.FACE_NAMES_NEG_POS);
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



    private void buildDebugInstances() {

        // force the light.camera and hence its frustum to be set to the correct position and direction
        light.begin();
        light.end();

        // create a frustum model (box shape, since the camera is orthogonal) for the directional shadow light
        frustumModel = createFrustumModel(light.getCamera().frustum.planePoints);
        frustumInstance = new ModelInstance(frustumModel, lightPosition);

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
