package com.monstrous.canyonracer.collision;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;


// This class stores the colliders as 2d polygons.
// These are the outlines of obstacles at fly height

public class Colliders {
    private static final float EPSILON = 0.001f;    // distance where vertices are considered same

    final Array<Polygon> collisionPolygons;         // used only for CollidersView (debug)
    final SpatialHash spatialHash;                  // spatial hash of Polygons
    private final Vector3 pos = new Vector3();
    private final Array<Vector3> intersections;
    private final Vector2 vec2 = new Vector2();

    public Colliders() {
        intersections = new Array<>();
        collisionPolygons = new Array<>();
        spatialHash = new SpatialHash();
    }

    // is racer in collision?  returns collider position if true
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

    // derive polygon outline of model instance intersected with horizonal plane at given height
    // and add it to colliders list
    //
    public void addCollider(ModelInstance obstacle, float height){
        Mesh mesh = obstacle.nodes.first().parts.first().meshPart.mesh;

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
                v0.mul(obstacle.transform);
                v1.mul(obstacle.transform);

                float d0 = v0.y - height;
                float d1 = v1.y - height;
                if (d0 * d1 < 0) {  // edge crosses plane
                    // calculate fraction of the edge from d0 to intersection point
                    float frac = Math.abs(d0)/(Math.abs(d0) + Math.abs(d1));
                    edge.set(v1).sub(v0);
                    midPoint.set(edge).scl(frac).add(v0); // derive point of intersection
                    // store in vertex array for this triangle
                    vindex[crossings++] = addToIntersectPoints( midPoint );
                }
            }
            if(crossings > 0){
                // assume if the triangle crosses once , then it crossed twice
                // the two intersection points form an edge of the polygon
                edges.add( new GridPoint3( vindex[0], vindex[1], 0));
            }

        }

        // now order the polygon edges by connecting vertices to form a loop
        // (or multiple loops) e.g. edge 1-2 connects to 2-3 connects to 3-4 etc.

        Array<Vector3> polyNodes = new Array<>();
        int start = 0;
        int curr = start;
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
                break;
            }

            if(curr == start) { // loop complete

                if(polyNodes.size >= 3) {
                    float[] vertexData = new float[2 * polyNodes.size];
                    int i = 0;
                    for (Vector3 p : polyNodes) {
                        vertexData[i++] = p.x;
                        vertexData[i++] = p.z;
                    }
                    Polygon poly = new Polygon(vertexData);
                    obstacle.transform.getTranslation(pos);
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
