package com.monstrous.canyonracer.collision;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;


// Collision detection is done with dedicated methods.

public class Colliders {
    private static final float EPSILON = 0.001f;

    private final Vector3 pos = new Vector3();
    private final Array<Vector3> intersections;
    final Array<Polygon> collisionPolygons;         // used only for CollidersView (debug)
    private final Vector2 vec2 = new Vector2();
    final SpatialHash spatialHash;

    public Colliders() {
        intersections = new Array<>();
        collisionPolygons = new Array<>();
        spatialHash = new SpatialHash();
    }

    public boolean inCollision(Vector3 racerPosition, Vector3 outColliderPosition ){
        Array<Polygon> polygons = spatialHash.findPolygons(racerPosition.x, racerPosition.z);
        if(polygons == null)
            return false;
        vec2.set(racerPosition.x, racerPosition.z);         // only consider 2d position in horizontal plane
        for(Polygon poly : polygons) {
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


    public void addCollider(ModelInstance rock, float h){
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

//        for(GridPoint3 e : edges ){
//            Gdx.app.log("edges", "v"+e.x + " - v"+e.y);
//        }

        Array<Vector3> polyNodes = new Array<>();
        int start = 0;
        int curr = start;
        //Gdx.app.log("start", String.valueOf(curr));
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
                //Gdx.app.log("end (error)", "");
                break;
            }
            //else
               // Gdx.app.log("next", String.valueOf(curr));

            if(curr == start) {
                //Gdx.app.log("loop closed", String.valueOf(curr));

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
                    spatialHash.addPolygon(poly);
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
    }


    private int addToIntersectPoints( Vector3 v ){
        for(int i = 0; i < intersections.size; i++){
            Vector3 p = intersections.get(i);
            if(v.dst2(p) < EPSILON)
                return i;
        }
        intersections.add( new Vector3(v) );
        return intersections.size-1;
    }
}
