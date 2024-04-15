package com.monstrous.canyonracer.terrain;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

// Perlin noise functions


public class Noise {

    // parameters for fBM processing
    public static float persistence = 0.7f;
    public static float lacunarity = 2.0f;
    public static int octaves = 5;

    Vector2 a = new Vector2();
    Vector2 d1 = new Vector2();

    public Noise() {
    }


    /* Create pseudorandom direction vector
     */
    void randomGradient(int ix, int iy, Vector2 gradient) {
        final float M = 2147483648f;
        final int shift = 16;

        int a = ix;
        int b = iy;
        a *= 348234342;
        b = b ^ ((a >> shift)|(a << shift));
        b *= 933742374;
        a = a^((b >> shift)|(b << shift));
        double rnd = ((float)a/M) * Math.PI;
        gradient.set((float)Math.sin(rnd), (float)Math.cos(rnd));
    }

    float smoothstep(float a, float b, float w)
    {
        if(w < 0)
            w = 0;
        else if (w > 1.0f)
            w = 1.0f;
        float f = w*w*(3.0f-2.0f*w);
        return a + f*(b-a);
    }


    private float dotDistanceGradient(int ix, int iy, float x, float y){
        randomGradient(ix, iy, a);
        float dx = x - ix;	// distance to corner
        float dy = y - iy;
        d1.set(dx,dy);
        return a.dot(d1);
    }


    public float PerlinNoise(float x, float y) {
        int ix = MathUtils.floor(x);
        int iy = MathUtils.floor(y);


        float f1 = dotDistanceGradient(ix, iy, x, y);
        float f2 = dotDistanceGradient(ix+1, iy, x, y);
        float f3 = dotDistanceGradient(ix, iy+1, x, y);
        float f4 = dotDistanceGradient(ix+1, iy+1, x, y);

        float u1 = smoothstep(f1, f2, x-ix);	// interpolate between top corners
        float u2 = smoothstep(f3, f4, x-ix);	// between bottom corners
        float res = smoothstep(u1, u2, y-iy); // between previous two points
        return res;
    }




    public float[][] generatePerlinMap (int xoffset, int yoffset, int width, int height,  float gridscale, float amplitude) {
        float[][] noise = new float[height+1][width+1]; // add one extra to make seamless meshes

        for (int y = 0; y <= height; y++) {
            for (int x = 0; x <= width; x++) {

                float xf = (xoffset+x)/gridscale;
                float yf = (yoffset+y)/gridscale;
                float value = PerlinNoise(xf, yf);
                noise[y][x] = value * amplitude;
            }
        }
        return noise;

    }

    public float interpolate (float x0, float x1, float alpha) {
        return x0 * (1 - alpha) + alpha * x1;
    }

    public float[][] generateSmoothNoise (int width, int height, float[][] baseNoise, int samplePeriod) {

        float[][] smoothNoise = new float[height][width];

        float sampleFrequency = 1.0f/(float)samplePeriod;
        for (int i = 0; i < width; i++) {
            int sample_i0 = (i / samplePeriod) * samplePeriod;
            int sample_i1 = (sample_i0 + samplePeriod) % width; // wrap around
            float horizontal_blend = (i - sample_i0) * sampleFrequency;

            for (int j = 0; j < height; j++) {
                int sample_j0 = (j / samplePeriod) * samplePeriod;
                int sample_j1 = (sample_j0 + samplePeriod) % height; // wrap around
                float vertical_blend = (j - sample_j0) * sampleFrequency;
                float top = interpolate(baseNoise[sample_j0][sample_i0], baseNoise[sample_j1][sample_i0], horizontal_blend);
                float bottom = interpolate(baseNoise[sample_j0][sample_i1], baseNoise[sample_j1][sample_i1], horizontal_blend);
                smoothNoise[j][i] = interpolate(top, bottom, vertical_blend);
            }
        }
        return smoothNoise;
    }

    // fBM - fractal Brownian motion processing of noise map
    //
    public float[][] smoothNoise (int width, int height, float[][] baseNoise) {

        float[][] smoothedNoise = new float[height][width]; // an array of floats initialised to 0

        int period = 1;
        float amplitude = 1.0f;
        float totalAmplitude = 0.0f;

        for (int octave = 0; octave < octaves; octave++) {
            float[][] smoothNoise = generateSmoothNoise(width, height, baseNoise, period);
            amplitude *= persistence;
            period *= 2; // ignore lacunarity
            totalAmplitude += amplitude;

            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    smoothedNoise[j][i] += smoothNoise[j][i] * amplitude;
                }
            }
        }

        // normalize
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                smoothedNoise[j][i] /= totalAmplitude;
            }
        }
        return smoothedNoise;
    }

}
