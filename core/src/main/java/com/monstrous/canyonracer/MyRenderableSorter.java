package com.monstrous.canyonracer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.utils.DefaultRenderableSorter;
import com.badlogic.gdx.graphics.g3d.utils.RenderableSorter;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import java.util.Comparator;

public class MyRenderableSorter implements RenderableSorter, Comparator<Renderable> {
    private Camera camera;
    private final Vector3 tmpV1 = new Vector3();
    private final Vector3 tmpV2 = new Vector3();

//    private DefaultRenderableSorter sorter = new DefaultRenderableSorter();

    @Override
    public void sort (final Camera camera, final Array<Renderable> renderables) {
        this.camera = camera;
        //sorter.sort(camera, renderables);

        //testCompares(renderables);

        renderables.sort(this);
        //testSort(renderables);
    }

//    private void testSort( Array<Renderable> renderables ){
//        int n = renderables.size;
//
//        for(int i = 0; i < n-1; i++){
//            int c1 = sorter.compare(renderables.get(i), renderables.get(i+1));
//            int c2 = sorter.compare(renderables.get(i+1), renderables.get(i));
//
//            Renderable o1 = renderables.get(i);
//            getTranslation(o1.worldTransform, o1.meshPart.center, tmpV1);
//            float dst =  camera.position.dst(tmpV1);
//            float dst2 = camera.position.dst2(tmpV1);
//            int di = (int)(1000f * dst2);
//
//            Gdx.app.log("compares:", ""+i+" : "+c1+" vs "+c2+" dst:"+dst+
//                " d2:"+dst2+" integer:"+di);
//
//        }
//        Gdx.app.exit();
//    }
//
//
//    private void testCompares( Array<Renderable> renderables ){
//        int n = renderables.size;
//
//        for(int i = 0; i < n; i++){
//            for(int j = 0; j < n; j++ ) {
//                int c1 = sorter.compare(renderables.get(i), renderables.get(j));
//                int c2 = sorter.compare(renderables.get(j), renderables.get(i));
//                if(c1 == 0 && c2 == 0)
//                    continue;
//                if(c1 == -1 && c2 == 1)
//                    continue;
//                if(c1 == 1 && c2 == -1)
//                    continue;
//                Gdx.app.log("compare error:", "" + i + " : " + c1 + " vs " + c2);
//                Gdx.app.exit();
//            }
//        }
//        //Gdx.app.exit();
//    }

    private Vector3 getTranslation (Matrix4 worldTransform, Vector3 center, Vector3 output) {
        if (center.isZero())
            worldTransform.getTranslation(output);
        else if (!worldTransform.hasRotationOrScaling())
            worldTransform.getTranslation(output).add(center);
        else
            output.set(center).mul(worldTransform);
        return output;
    }

    @Override
    public int compare (final Renderable o1, final Renderable o2) {
        final boolean b1 = o1.material.has(BlendingAttribute.Type)
            && ((BlendingAttribute)o1.material.get(BlendingAttribute.Type)).blended;
        final boolean b2 = o2.material.has(BlendingAttribute.Type)
            && ((BlendingAttribute)o2.material.get(BlendingAttribute.Type)).blended;
        if (b1 != b2) return b1 ? 1 : -1;


        // FIXME implement better sorting algorithm
        // final boolean same = o1.shader == o2.shader && o1.mesh == o2.mesh && (o1.lights == null) == (o2.lights == null) &&
        // o1.material.equals(o2.material);
        getTranslation(o1.worldTransform, o1.meshPart.center, tmpV1);
        getTranslation(o2.worldTransform, o2.meshPart.center, tmpV2);
        float d1 =  camera.position.dst2(tmpV1);
        float d2 =  camera.position.dst2(tmpV2);
//        int di1 = (int)(1000f*d1);
//        int di2 = (int)(1000f*d2);
        final float dst = d1 - d2;

        if (dst < 0.00001f && dst > -0.00001f)
            return 0;

        // fixed overflow issue!

        //final float dst = (int)(1000f * camera.position.dst2(tmpV1)) - (int)(1000f * camera.position.dst2(tmpV2));
        final int result = dst < 0 ? -1 : 1;
        //return result;
        return b1 ? -result : result;
    }
}
