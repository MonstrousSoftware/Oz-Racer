package com.monstrous.canyonracer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.ModelCache;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import net.mgsx.gltf.scene3d.scene.Scene;


// Rocks are not categorized as GameObjects
// They are rendered directly from the model cache
// Collision detection is done with dedicated methods.

public class Rocks implements Disposable {
    private static float DEBUG_VIEW_SCALE = 10;
    public static int AREA_LENGTH = 5000;
    public static int AREA_LENGTH2 = 8000;
    private static int SEPARATION_DISTANCE = 200;  // 200
    private static int SEPARATION_DISTANCE2 = 1000;  // 200

    private static String names[] = { "Rock", "Rock.001", "Rock.002", "Rock.003", "Rock.004" };
    private static float radius[] = { 20f,      21f,        10f,        20f,        14f };


    private ShapeRenderer sr = new ShapeRenderer();
    private Array<Rock> rocks;
    private Vector3 pos = new Vector3();
    public ModelCache cache;
    private Array<Vector3> intersections;
    private Array<Polygon> collisionPolygons;


    // obsolete
    static class Rock{
        Vector2 position;   // in horizontal (XZ) plane
        float radius2;

        public Rock(Vector2 position, float radius) {
            this.position = position;
            this.radius2 = radius*radius;
        }
    }

    // note: perhaps we should generate along with chunks to have an infinite amount

    public Rocks(World world ) {
        rocks = new Array<>();
        intersections = new Array<>();
        collisionPolygons = new Array<>();

        MathUtils.random.setSeed(1234);

        // generate a random poisson distribution of instances over a rectangular area, meaning instances are never too close together
        PoissonDistribution poisson = new PoissonDistribution();
        Rectangle area = new Rectangle(1, 1, AREA_LENGTH, AREA_LENGTH);
        Array<Vector2> points = poisson.generatePoissonDistribution(SEPARATION_DISTANCE, area);
        for(Vector2 point : points ) {
            point.x -= AREA_LENGTH/2;
            point.y -= AREA_LENGTH/2;
        }
        Rectangle area2 = new Rectangle(1, 1, AREA_LENGTH2, AREA_LENGTH2);
        Array<Vector2> points2 = poisson.generatePoissonDistribution(SEPARATION_DISTANCE2, area);
        for(Vector2 point : points2 ) {
            point.x -= AREA_LENGTH2/2;
            point.y -= AREA_LENGTH2/2;
        }
        points.addAll( points2 );
        points2.clear();

        cache = new ModelCache();
        cache.begin();

        for(Vector2 point : points ) {
            cache.add( addRock(world, point.x, point.y));
        }
        cache.end();
        Gdx.app.log("Rocks:", ""+points.size);


    }


    private ModelInstance addRock( World world, float x, float z ){
        int index = MathUtils.random( names.length-1 );
        float scale = MathUtils.random(0.5f, 5.5f);
        float rotation = MathUtils.random(0f, 360f);
        float y = world.terrain.getHeight(x,z);
        pos.set(x,y,z);

        Scene scene = world.loadNode(names[index], true, pos);
        scene.modelInstance.transform.scale(scale, scale, scale);
        scene.modelInstance.transform.rotate(Vector3.Y, rotation);

        rockSlicer(scene.modelInstance, y+5f);

//        GameObject gameObject = world.spawnObject(names[index], true, pos);
//        gameObject.isRock = true;
//        gameObject.getScene().modelInstance.transform.scale(scale, scale, scale);
//        gameObject.getScene().modelInstance.transform.rotate(Vector3.Y, rotation);
//        gameObject.calculateBoundingBox();  // for frustum culling, update after scaling and rotating

        if(index == 0) { // rock
            // this rock is rectangular, so we use 3 collision circles
            float offset = 30f*scale;
            rocks.add( new Rock( new Vector2(x,z), 15 * scale) );
            rocks.add( new Rock( new Vector2(0, offset).rotateDeg(-rotation).add(x,z), radius[index] * scale) );
            rocks.add( new Rock( new Vector2(0, -offset).rotateDeg(-rotation).add(x,z), radius[index] * scale) );
        }
        else if(index == 3) { // rock.003
            // this rock has a hole, so we use 2 collision circles
            float offset = 45*scale;
            rocks.add( new Rock( new Vector2(0, offset).rotateDeg(-rotation).add(x,z), radius[index] * scale) );
            rocks.add( new Rock( new Vector2(0, -offset).rotateDeg(-rotation).add(x,z), radius[index] * scale) );
        }
        else
            rocks.add( new Rock( new Vector2(x, z), radius[index] * scale) );

        return scene.modelInstance;
    }

    private Vector2 vec2 = new Vector2();

    public boolean inCollisionOld(Vector3 racerPosition, Vector3 colliderPosition ){
        vec2.set(racerPosition.x, racerPosition.z);         // only consider 2d position in horizontal plane
        for(Rock rock : rocks){
            if(rock.position.dst2(vec2) < rock.radius2 ) {
                colliderPosition.set(rock.position.x, 0, rock.position.y);
                return true;
            }
        }
        // some clever data structure would be more efficient
        return false;
    }

    public boolean inCollision(Vector3 racerPosition, Vector3 outColliderPosition ){
        vec2.set(racerPosition.x, racerPosition.z);         // only consider 2d position in horizontal plane
        for(Polygon poly : collisionPolygons) {
            // first do a quick test against the bounding rectangle, the polygon instance caches this
            if(!poly.getBoundingRectangle().contains(vec2))
                continue;

            if(poly.contains(vec2)){

                outColliderPosition.set(poly.getX(), 0, poly.getY());
                return true;
            }
        }
        return false;
    }

    public void debugRender( Vector3 playerPos ) {
        sr.begin(ShapeRenderer.ShapeType.Line);
//        sr.setColor(Color.RED);
//        for(Rock rock : rocks){
//            vec2.set(rock.position);
//            vec2.sub(playerPos.x, playerPos.z);
//            float radius = (float)Math.sqrt(rock.radius2)/SCALE;
//            convert(vec2);
//            sr.circle(vec2.y, vec2.x, radius);
//        }

        float[] test = { -2500, 2500, 2500, 2500, 2500, -2500, -2500, -2500};
        Polygon poly = new Polygon(test);

        sr.setColor(Color.GREEN);
        poly.setScale(1f/ DEBUG_VIEW_SCALE, 1f/ DEBUG_VIEW_SCALE);
        poly.translate(600-playerPos.z/ DEBUG_VIEW_SCALE, 400-playerPos.x/ DEBUG_VIEW_SCALE);
        sr.polygon(poly.getTransformedVertices());

        //float[] test2 = { -25, 25, 25, 25, 25, -25, -25, -25};
        Polygon poly2 = new Polygon();

        sr.setColor(Color.BLUE);
        //Polygon p = collisionPolygons.get(0);
        //poly2.setVertices(p.getVertices());
        for(Polygon p : collisionPolygons){

           poly2.setVertices(p.getVertices());
           poly2.setScale(1f/ DEBUG_VIEW_SCALE, 1f/ DEBUG_VIEW_SCALE);
           poly2.setPosition(600-playerPos.z/ DEBUG_VIEW_SCALE, 400-playerPos.x/ DEBUG_VIEW_SCALE);

//           float[] verts = poly2.getTransformedVertices();
//           for(int j = 0; j < verts.length; j += 2){
//               sr.circle(verts[j], verts[j+1], 2);
//            }

            sr.polygon(poly2.getTransformedVertices());
        }


        // racer
        vec2.set(0,0);
        convert(vec2);
        sr.setColor(Color.YELLOW);
        sr.circle(vec2.y, vec2.x, 5);
        sr.end();
    }

    // convert x,z from chunk units to screen pixels x,y
    private void convert(Vector2 pos){
        pos.x  = 400 + pos.x/ DEBUG_VIEW_SCALE;
        pos.y  = 600 + pos.y/ DEBUG_VIEW_SCALE;
    }

    public void rockSlicer(ModelInstance rock, float h){
        Mesh mesh = rock.nodes.first().parts.first().meshPart.mesh;

        int numVertices = mesh.getNumVertices();
        int numIndices = mesh.getNumIndices();
        int stride = mesh.getVertexSize()/4;        // floats per vertex in mesh, e.g. for position, normal, textureCoordinate, etc.

        float[] vertices = new float[numVertices*stride];
        short[] indices = new short[numIndices];
        // find offset of position floats per vertex, they are not necessarily the first 3 floats
        int posOffset = mesh.getVertexAttributes().findByUsage(VertexAttributes.Usage.Position).offset / 4;

        mesh.getVertices(vertices);
        mesh.getIndices(indices);

        Vector3 v0 = new Vector3();
        Vector3 v1 = new Vector3();

        intersections.clear();

//        for(int i = 0; i < numVertices; i++){
//            v0.set(vertices[stride*i+posOffset], vertices[stride*i+posOffset+1],vertices[stride*i+posOffset+2]);
//            Gdx.app.log("vertex:", ""+i+" : xyz="+v0.toString());
//        }

        Vector3 edge = new Vector3();
        Vector3 midPoint = new Vector3();
        int numTris = numIndices/3;
        int[] vindex = new int[2];
        Array<GridPoint3> edges = new Array<>();

        short i0, i1;
        for(int t = 0; t < numTris; t++){   // for each triangle
            int crossings = 0;
            for(int j = 0; j < 3; j++) {    // loop through 3 edges
                i0 = indices[3*t+j];
                if(j == 2)
                    i1 = indices[3*t];
                else
                    i1 = indices[3*t+j+1];

                v0.set(vertices[stride * i0 + posOffset], vertices[stride * i0 + posOffset + 1], vertices[stride * i0 + posOffset + 2]);
                v1.set(vertices[stride * i1 + posOffset], vertices[stride * i1 + posOffset + 1], vertices[stride * i1 + posOffset + 2]);
                // apply the transform of the model instance
                v0.mul(rock.transform);
                v1.mul(rock.transform);

                //Gdx.app.log("vertex:", "v0="+v0.toString()+" v1="+v0.toString());

                float d0 = v0.y - h;
                float d1 = v1.y - h;
                if (d0 * d1 < 0) {

                    float frac = Math.abs(d0)/(Math.abs(d0) + Math.abs(d1));
                    edge.set(v1).sub(v0);
                    midPoint.set(edge).scl(frac).add(v0);

                    //Gdx.app.log("line crossing plane", "d0:" + d0 + " d1:" + d1+" frac:"+frac+" midpoint:"+midPoint.toString());

                    vindex[crossings++] = addToIntersectPoints( midPoint );
                }
            }
            //Gdx.app.log("triangle", "t:" + t + " crossings:" + crossings);
            if(crossings > 0){
                //Gdx.app.log("triangle", "t:" + t + " crossings:" + crossings + "v"+vindex[0] + " v"+vindex[1]);
                edges.add( new GridPoint3( vindex[0], vindex[1], 0));
            }

        }

        for(GridPoint3 e : edges ){
            Gdx.app.log("edges", "v"+e.x + " - v"+e.y);
        }

        Array<Vector3> polyNodes = new Array<>();
        int start = 0;
        int curr = start;
        Gdx.app.log("start", ""+curr);
        polyNodes.add( intersections.get(curr) );
        while(true) {
            boolean found = false;
            for(GridPoint3 e : edges){
                if(e.z == 0 && e.x == curr) {
                    curr = e.y;
                    found = true;
                    e.z = 1;
                    polyNodes.add( intersections.get(curr) );
                    break;
                }
                if(e.z == 0 && e.y == curr) {
                    curr = e.x;
                    found = true;
                    e.z = 1;
                    polyNodes.add( intersections.get(curr) );
                    break;
                }
            }
            if(!found) {
                Gdx.app.log("end (error)", "");
                break;
            }
            else
                Gdx.app.log("next", ""+curr);

            if(curr == start) {
                Gdx.app.log("loop closed", "" + curr);

                if(polyNodes.size >= 3) {
                    float[] vertexData = new float[2 * polyNodes.size];
                    int i = 0;
                    for (Vector3 p : polyNodes) {
                        vertexData[i++] = p.x;
                        vertexData[i++] = p.z;
                    }
                    Polygon poly = new Polygon(vertexData);
                    rock.transform.getTranslation(pos);
                    poly.setOrigin(pos.x, pos.z);
                    collisionPolygons.add(poly);
                }
                polyNodes.clear();

                start = -1;
                for(GridPoint3 e : edges){
                    if(e.z == 0){
                        start = e.x;
                        break;
                    }
                }
                if(start < 0) {
//                    Gdx.app.log("all done", "");
                    break;
                }
                curr = start;
                polyNodes.add( intersections.get(curr) );
            }
        }



//        for( Vector3 p : intersections){
//            Gdx.app.log("intersect points", " xyz:"+p.toString());
//        }

//        if(intersections.size == 0) {
//            Gdx.app.log("no slice", "");
//            return null;
//        }
//
//        float[] vertexData = new float[2*intersections.size+2];
//        int i = 0;
//        for( Vector3 p : intersections){
//            vertexData[i++] = p.x;
//            vertexData[i++] = p.z;
//        }
//        // close the loop
//        Vector3 p = intersections.get(0);
//        vertexData[i++] = p.x;
//        vertexData[i] = p.z;
//
//        Polygon poly = new Polygon( vertexData );
//        rock.transform.getTranslation(pos);
//        poly.setOrigin(pos.x, pos.z);
//        collisionPolygons.add( poly );
    }

    private static final float EPSILON = 0.001f;

    private int addToIntersectPoints( Vector3 v ){
        for(int i = 0; i < intersections.size; i++){
            Vector3 p = intersections.get(i);
            if(v.dst2(p) < EPSILON)
                return i;
        }
        intersections.add( new Vector3(v) );
        return intersections.size-1;
    }



    @Override
    public void dispose() {
        cache.dispose();
    }
}
