package com.monstrous.canyonracer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import net.mgsx.gltf.scene3d.attributes.FogAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.lights.DirectionalShadowLight;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;
import net.mgsx.gltf.scene3d.utils.EnvironmentUtil;
import net.mgsx.gltf.scene3d.utils.IBLBuilder;

import java.util.zip.Deflater;

// Help class: convert skybox texture into 6 textures in format used by gdx-gltf
//


public class SkyBoxConverter extends ScreenAdapter {

    private static String fileName = "skybox/kisspng-skybox.png";
    private SpriteBatch batch;
    private Texture texture;
    private TextureRegion[] regions;
    private SceneManager sceneManager;
    private PerspectiveCamera camera;
    private DirectionalLight light;
    private Cubemap diffuseCubemap;
    private Cubemap environmentCubemap;
    private Cubemap specularCubemap;
    private Texture brdfLUT;
    private SceneSkybox skybox;
    private CameraInputController camController;


    @Override
    public void show() {
        batch = new SpriteBatch();

        if(true)
            splitSkybox();

        sceneManager = new SceneManager();
        // setup camera
        camera = new PerspectiveCamera(50f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = 1f;
        camera.far = 300f;
        camera.position.set(10,0,10);
        camera.lookAt(0, 100, 0);
        camera.up.set(Vector3.Y);
        sceneManager.setCamera(camera);

        camController = new CameraInputController(camera);
        Gdx.input.setInputProcessor(camController);


        // setup quick IBL (image based lighting)
        light = new DirectionalLightEx();
        light.direction.set(1, -3, 1).nor();
        light.color.set(Color.WHITE);
        sceneManager.environment.add(light);

        environmentCubemap = EnvironmentUtil.createCubemap(new InternalFileHandleResolver(),
            "skybox/side-", ".png", EnvironmentUtil.FACE_NAMES_NEG_POS);
        IBLBuilder iblBuilder = IBLBuilder.createOutdoor(light);
        diffuseCubemap = iblBuilder.buildIrradianceMap(256);
        specularCubemap = iblBuilder.buildRadianceMap(10);
        iblBuilder.dispose();

        // This texture is provided by the library, no need to have it in your assets.
        brdfLUT = new Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"));

        sceneManager.setAmbientLight(1f);
        sceneManager.environment.set(new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdfLUT));
        sceneManager.environment.set(PBRCubemapAttribute.createSpecularEnv(specularCubemap));
        sceneManager.environment.set(PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap));

        // setup skybox
        skybox = new SceneSkybox(environmentCubemap);
        sceneManager.setSkyBox(skybox);


    }

    private void splitSkybox() {

        texture = new Texture(Gdx.files.internal(fileName));
        int w = texture.getWidth();
        int h = texture.getHeight();
        Gdx.app.log("Texture", "w=" + w + " h=" + h);
        regions = new TextureRegion[6];


        // Split into 6 texture regions assuming the following layout
        // The order is +x -x +y -y +z -z in line with the standard face name order
        //
        //     [2]
        //  [0][4][1][5]
        //     [3]
        //

        int wr = w / 4;
        int hr = h / 3;
        Gdx.app.log("TextureRegions", "w=" + wr + " h=" + hr);

        regions[0] = new TextureRegion(texture, 0, hr, wr, hr);
        regions[1] = new TextureRegion(texture, wr * 2, hr, wr, hr);
        regions[2] = new TextureRegion(texture, wr, 0, wr, hr);
        regions[3] = new TextureRegion(texture, wr, hr * 2, wr, hr);
        regions[4] = new TextureRegion(texture, wr, hr, wr, hr);
        regions[5] = new TextureRegion(texture, wr * 3, hr, wr, hr);

        Pixmap pixmap = new Pixmap(wr, hr, Pixmap.Format.RGBA8888);

        for (int i = 0; i < 6; i++) {
            TextureRegion region = regions[i];

            TextureData textureData = region.getTexture().getTextureData();
            if (!textureData.isPrepared()) {
                textureData.prepare();
            }

            pixmap.drawPixmap(
                textureData.consumePixmap(), // The other Pixmap
                0, // The target x-coordinate (top left corner)
                0, // The target y-coordinate (top left corner)
                region.getRegionX(), // The source x-coordinate (top left corner)
                region.getRegionY(), // The source y-coordinate (top left corner)
                region.getRegionWidth(), // The width of the area from the other Pixmap in pixels
                region.getRegionHeight() // The height of the area from the other Pixmap in pixels
            );

            Pixmap flippedPixmap = flipPixmap(pixmap);

            FileHandle fh = new FileHandle("skybox/side-" + EnvironmentUtil.FACE_NAMES_NEG_POS[i] + ".png");
            PixmapIO.writePNG(fh, flippedPixmap, Deflater.DEFAULT_COMPRESSION, false);
        }
    }

    // mirror horizontally
    public static Pixmap flipPixmap (Pixmap src){
        final int width = src.getWidth();
        final int height = src.getHeight();
        Pixmap flipped = new Pixmap(width, height, src.getFormat());

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                    flipped.drawPixel(x, y, src.getPixel((width-1)-x, y));
            }
        }
        return flipped;
    }

    @Override
    public void render(float delta) {
        camController.update();
        camera.up.set(Vector3.Y);
        camera.update();

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        sceneManager.update(0.1f);
        sceneManager.render();

//        batch.begin();
//        batch.draw(texture,0,0);
//        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        batch.getProjectionMatrix().setToOrtho2D(0,0,width, height);
        sceneManager.updateViewport(width, height);
    }

    @Override
    public void hide() {
        dispose();

    }

    @Override
    public void dispose() {
        texture.dispose();
        sceneManager.dispose();
    }
}
