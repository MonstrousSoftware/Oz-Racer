package com.monstrous.canyonracer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

// Based on ThinMatrix OpenGL tutorial 53 on Youtube

public class LensFlare implements Disposable {

    public static boolean showLensFlare = true;

    private Texture textures[];
    private SpriteBatch batch;
    private float lightX, lightY;
    private float centreX, centreY;
    private float width, height;
    private float stepX, stepY;
    private float brightness;
    private ShaderProgram shaderProgram;
    private int u_brightness;
    private float screenX, screenY;
    private boolean sunVisible;

    public LensFlare() {
        batch = new SpriteBatch();
        textures = new Texture[9];
        textures[0] = new Texture(Gdx.files.internal("textures/lensFlare/tex1.png"));
        textures[1] = new Texture(Gdx.files.internal("textures/lensFlare/tex2.png"));
        textures[2] = new Texture(Gdx.files.internal("textures/lensFlare/tex3.png"));
        textures[3] = new Texture(Gdx.files.internal("textures/lensFlare/tex4.png"));
        textures[4] = new Texture(Gdx.files.internal("textures/lensFlare/tex5.png"));
        textures[5] = new Texture(Gdx.files.internal("textures/lensFlare/tex6.png"));
        textures[6] = new Texture(Gdx.files.internal("textures/lensFlare/tex7.png"));
        textures[7] = new Texture(Gdx.files.internal("textures/lensFlare/tex8.png"));
        textures[8] = new Texture(Gdx.files.internal("textures/lensFlare/tex9.png"));

        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();
        centreX = width/2;
        centreY = height/2;
        stepX = 0;
        stepY = 0;

        // simple shader which allows to control the brightness
        ShaderProgram.pedantic = true;
        shaderProgram = new ShaderProgram(
                Gdx.files.internal("shaders\\flare.vertex.glsl"),
                Gdx.files.internal("shaders\\flare.fragment.glsl"));
        if (!shaderProgram.isCompiled())
            throw new GdxRuntimeException(shaderProgram.getLog());

        u_brightness = shaderProgram.getUniformLocation("u_brightness");
    }

    private boolean setLightSourcePosition(float x, float y) {
        if( x < 0 || x > width || y < 0 || y > height) {
            brightness = 0; // disable flare effect when light source is off screen
            return false;
        }

        lightX = x;
        lightY = y;
        stepX = -0.3f * (lightX - centreX);
        stepY = -0.3f * (lightY - centreY);

        float dx = 2f*(lightX - centreX)/width;
        float dy = 2f*(lightY - centreY)/height;
        float len = dx*dx + dy*dy;      // length of vector

        // brightness falls off as light position distance from centre screen increases
        //
        brightness = 1.0f - len * 0.6f;
        brightness *= 0.2f;      // keep it subtle

        shaderProgram.bind();
        shaderProgram.setUniformf(u_brightness, brightness);
        return true;
    }

    private Vector3 relativeLightPos = new Vector3();

    public void render( Camera cam, Vector3 lightPosition ) {
        if(!showLensFlare)      // settings
            return;

        sunVisible = false;
        relativeLightPos.set(lightPosition);
        relativeLightPos.add(cam.position);
        // is light source visible from the camera?
        if(!cam.frustum.pointInFrustum(relativeLightPos))
            return;

        // convert world position to screen position
        cam.project(relativeLightPos);
        sunVisible = true;
        screenX = relativeLightPos.x;
        screenY = relativeLightPos.y;

       // Gdx.app.log("flare screen coords", ""+relativeLightPos.x+" , "+relativeLightPos.y);

        boolean visible = setLightSourcePosition(relativeLightPos.x, relativeLightPos.y);
        if(!visible)
            return;

        float x,y;
        float scale = 3f;       // scale for the textures

        x = lightX;
        y = lightY;

        batch.begin();
        batch.setShader(shaderProgram);

        batch.enableBlending();
        batch.setBlendFunction(Gdx.gl20.GL_SRC_ALPHA, Gdx.gl20.GL_ONE);
        for( Texture tex : textures ) {
            x += stepX;
            y += stepY;
            batch.draw(tex, x-scale*tex.getWidth()/2, y-scale*tex.getHeight()/2, scale*tex.getWidth(), scale*tex.getHeight());
        }
        batch.end();
    }

    private ShapeRenderer sr = new ShapeRenderer();

    // debug: show screen position of light source as a red dot
    public void showLightPosition() {
        if(!sunVisible)
            return;

        sr.begin( ShapeRenderer.ShapeType.Filled );
        sr.setColor(Color.RED);
        sr.circle(screenX, screenY, 20);
        sr.end();
    }

    @Override
    public void dispose() {
        for( Texture tex : textures )
            tex.dispose();
        textures = null;
        batch.dispose();
        shaderProgram.dispose();
    }
}
