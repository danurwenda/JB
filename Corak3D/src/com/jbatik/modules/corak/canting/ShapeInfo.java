/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.corak.canting;

import java.util.LinkedList;
import javax.media.j3d.Shape3D;
import javax.vecmath.Point3d;
import javax.vecmath.TexCoord2f;
import javax.vecmath.Vector3f;

/**
 * Sebuah instance ShapeInfo terasosiasi dengan sebuah color index. Awalnya
 * ShapeInfo merupakan kelas yang berperan sebagai dump dari daftar koordinat
 * point (sebelum terbentuk Shape).
 *
 * Setelah semua koordinat dihitung, Shape dibentuk, untuk sementara primitifnya
 * ada dua, yaitu QuadArray untuk F dan TriangleFanArray untuk poligon. Sekali
 * diassign, kedua Shape ini final.
 * 
 * Edit 9/22/14 Ditambahkan primitif satu lagi pakai GeometryInfo.POLYGON_ARRAY
 * Hanya dipakai saat export to OBJ dengan opsi close F ends diaktifkan.
 *
 * @author RAPID01
 */
public class ShapeInfo {

    private Shape3D quadArrayShape;
    private Shape3D poligonShape;
    private Shape3D enclosingShape;

    private LinkedList<Point3d>[] shapePoints;  //[0] F, [1] polygon, [2] enclose
    private LinkedList<Vector3f> fNormals;
    private LinkedList<Integer> polygonStripVertexCounts;
    private LinkedList<Integer> encloseStripVertexCounts;
    private LinkedList<TexCoord2f>[] shapeTexCoords; //[0] F, [1] polygon, [2] enclose

    public ShapeInfo() {
        shapePoints = new LinkedList[3];
        shapePoints[0] = new LinkedList<>();
        shapePoints[1] = new LinkedList<>();
        shapePoints[2] = new LinkedList<>();
        fNormals = new LinkedList<>();
        polygonStripVertexCounts = new LinkedList<>();
        encloseStripVertexCounts = new LinkedList<>();
        shapeTexCoords = new LinkedList[2];
        shapeTexCoords[0] = new LinkedList<>();
        shapeTexCoords[1] = new LinkedList<>();
//        shapeTexCoords[2] = new LinkedList<>();
    }

    public Shape3D getEnclosingShape() {
        return enclosingShape;
    }

    public boolean setEnclosingShape(Shape3D e) {
        boolean ret = false;
        if (enclosingShape == null) {
            enclosingShape = e;
            ret = true;
        }
        return ret;
    }

    public Shape3D getFShape() {
        return quadArrayShape;
    }

    public boolean setFShape(Shape3D f) {
        boolean ret = false;
        if (quadArrayShape == null) {
            quadArrayShape = f;
            ret = true;
        }
        return ret;
    }

    public Shape3D getPolygonShape() {
        return poligonShape;
    }

    public boolean setPolygonShape(Shape3D f) {
        boolean ret = false;
        if (poligonShape == null) {
            poligonShape = f;
            ret = true;
        }
        return ret;
    }

    public LinkedList<Point3d> getFPoints() {
        return shapePoints[0];
    }

    public Point3d[] getFPointsInArray() {
        return shapePoints[0].toArray(new Point3d[shapePoints[0].size()]);
    }

    public LinkedList<Point3d> getPolygonPoints() {
        return shapePoints[1];
    }

    public Point3d[] getPolygonPointsInArray() {
        return shapePoints[1].toArray(new Point3d[shapePoints[1].size()]);
    }

    public LinkedList<Point3d> getEnclosePoints() {
        return shapePoints[2];
    }

    public Point3d[] getEnclosePointsInArray() {
        return shapePoints[2].toArray(new Point3d[shapePoints[2].size()]);
    }

    public LinkedList<Vector3f> getFNormals() {
        return fNormals;
    }

    public Vector3f[] getFNormalsInArray() {
        return fNormals.toArray(new Vector3f[fNormals.size()]);
    }

    public int[] getPolygonStripVertexCountsInArray() {
        int vertexNum = polygonStripVertexCounts.size();
        int[] arr = new int[vertexNum];
        for (int i = 0; i < vertexNum; i++) {
            arr[i] = polygonStripVertexCounts.get(i);
        }
        return arr;
    }

    public int[] getEncloseStripVertexCountsInArray() {
        int vertexNum = encloseStripVertexCounts.size();
        int[] arr = new int[vertexNum];
        for (int i = 0; i < vertexNum; i++) {
            arr[i] = encloseStripVertexCounts.get(i);
        }
        return arr;
    }

    public void addPolygonStripVertexCount(int count) {
        polygonStripVertexCounts.add(count);
    }

    public void addEncloseStripVertexCount(int count) {
        encloseStripVertexCounts.add(count);
    }

    public LinkedList<TexCoord2f> getFTexCoords() {
        return shapeTexCoords[0];
    }

    public TexCoord2f[] getFTexCoordsInArray() {
        return shapeTexCoords[0].toArray(new TexCoord2f[shapeTexCoords[0].size()]);
    }

    public LinkedList<TexCoord2f> getPolygonTexCoords() {
        return shapeTexCoords[1];
    }

    public TexCoord2f[] getPolygonTexCoordsInArray() {
        return shapeTexCoords[1].toArray(new TexCoord2f[shapeTexCoords[1].size()]);
    }

//    public LinkedList<TexCoord2f> getEncloseTexCoords() {
//        return shapeTexCoords[2];
//    }
//
//    public TexCoord2f[] getEncloseTexCoordsInArray() {
//        return shapeTexCoords[2].toArray(new TexCoord2f[shapeTexCoords[2].size()]);
//    }

    public LinkedList<Shape3D> getShapes() {
        LinkedList<Shape3D> ret = new LinkedList<>();
        if (getFShape() != null) {
            ret.add(getFShape());
        }
        if (getPolygonShape() != null) {
            ret.add(getPolygonShape());
        }
        if (getEnclosingShape() != null) {
            ret.add(getEnclosingShape());
        }
        return ret;
    }

}
