package com.monstrous.canyonracer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;


// post-processing effect to render an FBO to screen applying shader effects
// requires that you've rendered the seen to an FBO

public class PostFilter implements Disposable {

    private SpriteBatch batch;
    private ShaderProgram program;
    private float[] resolution = { 640, 480 };

    public PostFilter() {
        // full screen post processing shader
        program = new ShaderProgram(
            Gdx.files.internal("shaders\\vignette.vertex.glsl"),
            Gdx.files.internal("shaders\\vignette.fragment.glsl"));
        if (!program.isCompiled())
            throw new GdxRuntimeException(program.getLog());
        ShaderProgram.pedantic = false;

        batch = new SpriteBatch();
    }

    public void resize (int width, int height) {
        resolution[0] = width;
        resolution[1] = height;
        batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);  // to ensure the fbo is rendered to the full window after a resize

    }

    public void render( FrameBuffer fbo ) {
        Sprite s = new Sprite(fbo.getColorBufferTexture());
        s.flip(false,  true); // coordinate system in buffer differs from screen

        batch.begin();
        batch.setShader(program);                        // post-processing shader
        batch.draw(s, 0, 0, resolution[0], resolution[1]);    // draw frame buffer as screen filling texture
        batch.end();
        batch.setShader(null);
    }


    @Override
    public void dispose() {
        batch.dispose();
        program.dispose();
    }
}
