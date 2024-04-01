package com.monstrous.canyonracer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Disposable;
import net.mgsx.gltf.loaders.gltf.GLTFAssetLoader;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

import static com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;

public class Assets implements Disposable {

    public Music    gameMusic;
    public Skin     skin;
    public Skin     debugSkin;
    public Sound    MENU_CLICK;
    public Sound    COLLISION;
    public Sound    START_BEEP;
    public Texture  title;
    public Texture  character;
    public Texture  terrainTexture;
    public SceneAsset   sceneAssetGame;
    public SceneAsset   sceneAssetRocks;
    public SceneAsset   sceneAssetLogo;


    private AssetManager assets;




    public Assets() {
        Gdx.app.log("Assets constructor", "");
        assets = new AssetManager();
        assets.load("skin/uiskin.json", Skin.class);
        assets.load("gameSkin/canyon.json", Skin.class);

        assets.load("textures/title.png", Texture.class);
        assets.load("textures/crazy-cat.png", Texture.class);
        TextureLoader.TextureParameter param = new TextureLoader.TextureParameter();
        param.minFilter = Linear;
        param.genMipMaps = true;
        assets.load("textures/ground/smooth+sand+dunes-512x512.jpg", Texture.class, param);

        assets.load("sound/click_002.ogg", Sound.class);
        assets.load("sound/explosionCrunch_000.ogg", Sound.class);
        assets.load("sound/startbeeps.ogg", Sound.class);

        assets.load("music/fight.ogg", Music.class);

        assets.setLoader(SceneAsset.class, ".gltf", new GLTFAssetLoader());
        assets.load(  "models/OzRacer.gltf", SceneAsset.class);
        assets.load(  "models/rocks.gltf", SceneAsset.class);
        assets.load(  "models/libGDX-logo.gltf", SceneAsset.class);

    }


    public boolean update() {
        return assets.update();
    }


    public void finishLoading() {
        assets.finishLoading();

        skin = assets.get("gameSkin/canyon.json");
        debugSkin = assets.get("skin/uiskin.json");
        title = assets.get("textures/title.png");
        character = assets.get("textures/crazy-cat.png");
        terrainTexture = assets.get("textures/ground/smooth+sand+dunes-512x512.jpg");
        gameMusic = assets.get("music/fight.ogg");
        MENU_CLICK = assets.get("sound/click_002.ogg");
        COLLISION = assets.get("sound/explosionCrunch_000.ogg");
        START_BEEP = assets.get("sound/startbeeps.ogg");
        sceneAssetGame = assets.get("models/OzRacer.gltf");
        sceneAssetRocks = assets.get("models/rocks.gltf");
        sceneAssetLogo = assets.get("models/libGDX-logo.gltf");
    }

    public float getProgress() {
        return assets.getProgress();
    }


    public <T> T get(String name ) {
        return assets.get(name);
    }

    @Override
    public void dispose() {
        Gdx.app.log("Assets dispose()", "");
        assets.dispose();
        assets = null;
    }
}
