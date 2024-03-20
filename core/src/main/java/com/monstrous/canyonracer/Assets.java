package com.monstrous.canyonracer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Disposable;
import net.mgsx.gltf.loaders.gltf.GLTFAssetLoader;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class Assets implements Disposable {

    public Music    gameMusic;
    public Skin     skin;
    public Skin     debugSkin;
    public Sound    MENU_CLICK;
    public Texture  title;

    private AssetManager assets;




    public Assets() {
        Gdx.app.log("Assets constructor", "");
        assets = new AssetManager();
        assets.load("skin/uiskin.json", Skin.class);
        assets.load("gameSkin/canyon.json", Skin.class);

        assets.load("textures/title.png", Texture.class);

        assets.load("sound/click_002.ogg", Sound.class);

        assets.load("music/fight.ogg", Music.class);
    }


    public boolean update() {
        return assets.update();
    }


    public void finishLoading() {
        assets.finishLoading();

        skin = assets.get("gameSkin/canyon.json");
        debugSkin = assets.get("skin/uiskin.json");
        title = assets.get("textures/title.png");
        gameMusic = assets.get("music/fight.ogg");
        MENU_CLICK = assets.get("sound/click_002.ogg");
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
