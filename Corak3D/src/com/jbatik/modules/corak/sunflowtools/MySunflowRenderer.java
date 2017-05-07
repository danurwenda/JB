/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.corak.sunflowtools;

import com.jbatik.modules.corak.CorakLSystem;
import com.jbatik.modules.corak.canting.Canting;
import com.jbatik.modules.corak.canting.CorakStructure;
import com.jbatik.modules.corak.canting.ShapeInfo;
import com.jbatik.modules.interfaces.Appearance3DChangerCookie;
import com.jbatik.modules.interfaces.Scene3DObserverCookie;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import javax.media.j3d.Geometry;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TriangleFanArray;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import org.sunflow.PluginRegistry;
import org.sunflow.SunflowAPI;
import org.sunflow.core.display.FileDisplay;
import org.sunflow.core.renderer.BucketRenderer;
import org.sunflow.math.Matrix4;
import org.sunflow.math.Point3;
import org.sunflow.math.Vector3;

/**
 *
 * @author RAPID01
 */
public class MySunflowRenderer {

    public static boolean isSunflowRenderingThread(Thread thread) {
        Class enclosingClass = thread.getClass().getEnclosingClass();
        if (enclosingClass != null && enclosingClass.equals(BucketRenderer.class)) {
            return true;
        } else return false;
    }

    private SunflowAPI sunflow;
    private int width;
    private int height;
    private boolean shiny;
    private float distanceToObject;

    public MySunflowRenderer(
            Canting canting,
            Appearance3DChangerCookie.Appearance appearance,
            Scene3DObserverCookie.Projection projection,
            Point3d cameraPos,
            Point3d cameraFocus,
            Vector3d up,
            double fieldOfView,
            double screenScale,
            double far,
            double near,
            boolean useLighting,
            int width,
            int height,
            boolean withBackground,
            Color backgroundColor
    ) {
        this.width = width;
        this.height = height;

        initSetup();

        setupCamera(cameraPos, cameraFocus, up, fieldOfView, far, near);

        setupLighting(useLighting, cameraPos, cameraFocus);

        setupObject(canting, appearance, cameraPos, cameraFocus, projection, screenScale);
        
        if (withBackground) {
            setupBackground(backgroundColor);
        }
    }

    public void initSetup() {
        sunflow = new SunflowAPI();
        PluginRegistry.shaderPlugins.registerPlugin("my_textured_constant", MyTexturedConstantShader.class);
    }

    public void setupObject(Canting canting, Appearance3DChangerCookie.Appearance appearance, Point3d cameraPos, Point3d cameraFocus, Scene3DObserverCookie.Projection projection, double screenScale) {
        //here comes the object
        
        HashMap<CorakLSystem, CorakStructure> structures = canting.getStructures();
        
        for (Map.Entry<CorakLSystem, CorakStructure> entry : structures.entrySet()) {
                        
            CorakStructure currStruct = entry.getValue();
            ArrayList<Color3f> currCList = currStruct.getColorList();
            int currCListSize = currCList.size();
            ArrayList<String> currTList = currStruct.getTextureList();
            for (int idx=1; idx<currCListSize; idx++) {
                Color currentColor = currCList.get(idx).get();
                ShapeInfo shapeInfo = currStruct.getShapeInfoByColor(idx);
                String currentTexture = canting.getTextureFilePath( currTList.get(idx));
                LinkedList<Shape3D> shapeList = shapeInfo.getShapes();

                //let's reconstruct shapes of this color
                if (!shapeList.isEmpty()) {

                    //for each shape of this color
                    for (Shape3D shape : shapeList) {

                        Geometry geom = shape.getGeometry();

                        //if it's a tube (F)
                        if (geom instanceof QuadArray) {

                            QuadArray q = (QuadArray) geom;
                            String name = System.identityHashCode(q) + "_" + currentColor.toString() + "_F";

                            int vCount = q.getVertexCount();
                            double[] JCoords = new double[vCount * 3];
                            
                            q.getCoordinates(0, JCoords);
                            
                            if (projection == Scene3DObserverCookie.Projection.PARALLEL) {
                                JCoords = convertToParallel(JCoords, cameraPos, cameraFocus, screenScale);
                            }
                            
                            
                            if (appearance == Appearance3DChangerCookie.Appearance.WIREFRAME) {

                                //prepare vertices for hair primitive

                                float[] verticesW = new float[JCoords.length * 2];

                                for (int cIdx=0, vIdx=0; vIdx<verticesW.length; cIdx+=12, vIdx+=24) {
                                    verticesW[vIdx] = (float) JCoords[cIdx];
                                    verticesW[vIdx+1] = (float) JCoords[cIdx+1];
                                    verticesW[vIdx+2] = (float) JCoords[cIdx+2];

                                    verticesW[vIdx+3] = (float) JCoords[cIdx+3];
                                    verticesW[vIdx+4] = (float) JCoords[cIdx+4];
                                    verticesW[vIdx+5] = (float) JCoords[cIdx+5];

                                    verticesW[vIdx+6] = (float) JCoords[cIdx+3];
                                    verticesW[vIdx+7] = (float) JCoords[cIdx+4];
                                    verticesW[vIdx+8] = (float) JCoords[cIdx+5];

                                    verticesW[vIdx+9] = (float) JCoords[cIdx+6];
                                    verticesW[vIdx+10] = (float) JCoords[cIdx+7];
                                    verticesW[vIdx+11] = (float) JCoords[cIdx+8];

                                    verticesW[vIdx+12] = (float) JCoords[cIdx+6];
                                    verticesW[vIdx+13] = (float) JCoords[cIdx+7];
                                    verticesW[vIdx+14] = (float) JCoords[cIdx+8];

                                    verticesW[vIdx+15] = (float) JCoords[cIdx+9];
                                    verticesW[vIdx+16] = (float) JCoords[cIdx+10];
                                    verticesW[vIdx+17] = (float) JCoords[cIdx+11];

                                    verticesW[vIdx+18] = (float) JCoords[cIdx+9];
                                    verticesW[vIdx+19] = (float) JCoords[cIdx+10];
                                    verticesW[vIdx+20] = (float) JCoords[cIdx+11];

                                    verticesW[vIdx+21] = (float) JCoords[cIdx];
                                    verticesW[vIdx+22] = (float) JCoords[cIdx+1];
                                    verticesW[vIdx+23] = (float) JCoords[cIdx+2];
                                }

                                int segments = 1;

                                createWireframeMesh(name, currentColor, segments, verticesW);

                            } else {

                                float[] JNormals = new float[vCount * 3];
                                q.getNormals(0, JNormals);

                                float[] JTexcoords = new float[vCount * 2];
                                q.getTextureCoordinates(0, 0, JTexcoords);

                                //construct a triangle mesh from this infos
                                //the number of triangles is 2 * number of rectangles,
                                //which are number of all vertexes / 4
                                //then multiply by 3, because 3 points per 1 triangle

                                float[] vertices = new float[vCount * 3];

                                int[] triangles = new int[2 * (vCount / 4) * 3];

                                for (int cIdx = 0, tIdx = 0; cIdx < vCount * 3; cIdx++) {

                                    vertices[cIdx] = (float) JCoords[cIdx];

                                    if ((cIdx + 1) % 4 == 0 && tIdx < triangles.length) {
                                        //  3  ------------  2
                                        //    | \          |
                                        //    |      \     |
                                        //    |          \ |
                                        //  0  ------------  1
                                        //
                                        // counter clock wise

                                        //top triangle
                                        triangles[tIdx] = cIdx - 2;       //1
                                        triangles[tIdx + 1] = cIdx - 1;   //2
                                        triangles[tIdx + 2] = cIdx;       //3

                                        //bottom triangle
                                        triangles[tIdx + 3] = cIdx - 2;   //1
                                        triangles[tIdx + 4] = cIdx;       //3
                                        triangles[tIdx + 5] = cIdx - 3;   //0
                                        tIdx += 6;
                                    }
                                }

                                createMesh(name, currentColor, currentTexture, vertices, triangles, JNormals, JTexcoords, appearance);
                            }

                        } else //if it's a polygon
                        if (geom instanceof TriangleFanArray) {

                            TriangleFanArray t = (TriangleFanArray) geom;

                            String name = System.identityHashCode(t)+ "_" + currentColor.toString() + "_polygon";
                            int vCount = t.getVertexCount();

                            double[] JCoords = new double[vCount * 3];
                            
                            t.getCoordinates(0, JCoords);
                                                        
                            if (projection == Scene3DObserverCookie.Projection.PARALLEL) {
                                JCoords = convertToParallel(JCoords, cameraPos, cameraFocus, screenScale);
                            }

                            float[] JNormals = new float[vCount * 3];
                            t.getNormals(0, JNormals);

                            float[] JTexcoords = new float[vCount * 2];
                            t.getTextureCoordinates(0, 0, JTexcoords);

                            int[] stripVertexCounts = new int[vCount];
                            t.getStripVertexCounts(stripVertexCounts);

                            //construct a triangle mesh from this infos
                            float[] vertices = new float[vCount * 3];

                            //the number of triangles = number of strips
                            int[] triangles = new int[stripVertexCounts.length];

                            int vIdx = 0;
                            int tIdx = 0;
                            for (int stripIdx = 0; stripIdx < stripVertexCounts.length; stripIdx++) {

                                for (int svIdx = 0; svIdx < stripVertexCounts[stripIdx]; svIdx++) {

                                    //assign vertex
                                    vertices[vIdx] = (float) JCoords[vIdx];
                                    vertices[vIdx+1] = (float) JCoords[vIdx+1];
                                    vertices[vIdx+2] = (float) JCoords[vIdx+2];
                                    vIdx += 3;

                                    //assign triangles
                                    triangles[tIdx] = tIdx;
                                    tIdx++;
                                }

                            }
                            if (appearance == Appearance3DChangerCookie.Appearance.WIREFRAME) {

                                float[] verticesW = new float [vertices.length * 4];

                                for (int tIx=0, vIx=0; tIx<triangles.length; tIx+=3, vIx+=18) {
                                    int ix0 = triangles[tIx] * 3;
                                    int ix1 = triangles[tIx+1] * 3;
                                    int ix2 = triangles[tIx+2] * 3;

                                    float x0 = vertices[ix0];
                                    float y0 = vertices[ix0+1];
                                    float z0 = vertices[ix0+2];

                                    float x1 = vertices[ix1];
                                    float y1 = vertices[ix1+1];
                                    float z1 = vertices[ix1+2];

                                    float x2 = vertices[ix2];
                                    float y2 = vertices[ix2+1];
                                    float z2 = vertices[ix2+2];

                                    verticesW[vIx] = x0;
                                    verticesW[vIx+1] = y0;
                                    verticesW[vIx+2] = z0;

                                    verticesW[vIx+3] = x1;
                                    verticesW[vIx+4] = y1;
                                    verticesW[vIx+5] = z1;

                                    verticesW[vIx+6] = x1;
                                    verticesW[vIx+7] = y1;
                                    verticesW[vIx+8] = z1;

                                    verticesW[vIx+9] = x2;
                                    verticesW[vIx+10] = y2;
                                    verticesW[vIx+11] = z2;

                                    verticesW[vIx+12] = x2;
                                    verticesW[vIx+13] = y2;
                                    verticesW[vIx+14] = z2;

                                    verticesW[vIx+15] = x0;
                                    verticesW[vIx+16] = y0;
                                    verticesW[vIx+17] = z0;
                                }

                                int segments = 1;

                                createWireframeMesh(name, currentColor, segments, verticesW);

                            } else {
                                createMesh(name, currentColor, currentTexture, vertices, triangles, JNormals, JTexcoords, appearance);
                            }

                        }
                    }

                }
            }

        }
    }
    
    public void setupCamera(Point3d cameraPos, Point3d cameraFocus, Vector3d up, double fieldOfView, double far, double near) {
        Point3 sunflowEye = new Point3((float) cameraPos.getX(), (float) cameraPos.getY(), (float) cameraPos.getZ());
        Point3 sunflowTarget = new Point3((float) cameraFocus.getX(), (float) cameraFocus.getY(), (float) cameraFocus.getZ());
        Vector3 sunflowUp = new Vector3((float) up.getX(), (float) up.getY(), (float) up.getZ());

        sunflow.parameter("transform", Matrix4.lookAt(sunflowEye, sunflowTarget, sunflowUp));
        sunflow.parameter("fov", (float) Math.toDegrees(fieldOfView));

        sunflow.parameter("aspect", (float) this.width / (float) this.height);

        sunflow.camera("thinlens", "thinlens");
        
        distanceToObject = (float) cameraPos.distance(cameraFocus);
    }

    public void setupLighting(boolean useLighting, Point3d cameraPos, Point3d cameraFocus) {

        this.shiny = useLighting;

        if (useLighting) {

            //directional light
            Vector3 dir = new Vector3();
            dir.set((float) (cameraFocus.getX() - cameraPos.getX()),
                    (float) (cameraFocus.getY() - cameraPos.getY()),
                    (float) (cameraFocus.getZ() - cameraPos.getZ()));

            sunflow.parameter("source", new Point3((float) cameraPos.getX(), (float) cameraPos.getY(), (float) cameraPos.getZ()));
            sunflow.parameter("dir", dir);
            sunflow.parameter("radius", ((float) cameraPos.distance(cameraFocus)));
            sunflow.parameter("radiance", "sRGB nonlinear", 1.0f, 1.0f, 1.0f);
            sunflow.light("lighting", "directional");

            //ambient light
        }

    }
    
    /**
     * create wireframe mesh for F and polygon. Vertices order should be prepared before
     */
    private void createWireframeMesh(String name, Color currentColor, int segments, float[] vertices) {
        float r = currentColor.getRed() / (float) 255;
        float g = currentColor.getGreen() / (float) 255;
        float b = currentColor.getBlue() / (float) 255;
        
        float wireThickness = .0005f;
        wireThickness *= distanceToObject;
        
        //shaders
        sunflow.parameter("color", "sRGB nonlinear", r, g, b);
        sunflow.shader(name + "_shader", "constant");
        
        //geom
        sunflow.parameter("segments", segments);
        sunflow.parameter("widths", "float", "none", new float[] { wireThickness });
        sunflow.parameter("points", "point", "vertex", vertices);

        sunflow.geometry( name, "hair" );
        sunflow.parameter("shaders", name + "_shader");
        sunflow.instance( name + ".instance", name );
    }

    
    /**
     * create triangle mesh
     */
    private void createMesh(String name, Color currentColor, String currentTexture, float[] vertices, int[] triangles, float[] JNormals,float[] JTexcoords, Appearance3DChangerCookie.Appearance appearance) {
        
        float r = currentColor.getRed() / (float) 255;
        float g = currentColor.getGreen() / (float) 255;
        float b = currentColor.getBlue() / (float) 255;
        
        //shaders
        if (shiny) {
            //phong
            if (appearance == Appearance3DChangerCookie.Appearance.SOLID) {
                sunflow.parameter("diffuse", "sRGB nonlinear", r, g, b);
                sunflow.parameter("specular", "sRGB nonlinear", 1.0f, 1.0f, 1.0f);
                sunflow.parameter("power", 5.0f);
                sunflow.parameter("samples", 16);
                sunflow.shader(name + "_shader", "phong");
            } else if (appearance == Appearance3DChangerCookie.Appearance.TEXTURE) {
                sunflow.parameter("texture", currentTexture);
                sunflow.parameter("specular", "sRGB nonlinear", 1.0f, 1.0f, 1.0f);
                sunflow.parameter("power", 5.0f);
                sunflow.parameter("samples", 16);
                sunflow.shader(name + "_shader", "textured_phong");
            }

        } else {
            if (appearance == Appearance3DChangerCookie.Appearance.SOLID) {
                //constant
                sunflow.parameter("color", "sRGB nonlinear", r, g, b);
                sunflow.shader(name + "_shader", "constant");
            } else if (appearance == Appearance3DChangerCookie.Appearance.TEXTURE) {
                sunflow.parameter("texture", currentTexture);
                sunflow.shader(name + "_shader", "my_textured_constant");
            }
        }

        //geom
        sunflow.parameter("points", "point", "vertex", vertices);
        sunflow.parameter("triangles", triangles);
        sunflow.parameter("normals", "vector", "vertex", JNormals);
        sunflow.parameter("uvs", "texcoord", "vertex", JTexcoords);
        sunflow.geometry(name, "triangle_mesh");
        sunflow.parameter("shaders", name + "_shader");
        sunflow.instance(name + ".instance", name);
    }
    
    private void setupBackground(Color bgColor) {
        float r = bgColor.getRed() / (float) 255;
        float g = bgColor.getGreen() / (float) 255;
        float b = bgColor.getBlue() / (float) 255;
        
        String name = "background";
        String shaderName = "background_shader";
        
        sunflow.parameter("color", "sRGB nonlinear", r, g, b);
        sunflow.shader(shaderName, "constant");
        
        sunflow.geometry(name, "background");
        sunflow.parameter("shaders", shaderName);
        sunflow.instance(name + ".instance", name);
    }


    public void toPNG(String filepath){
        
        sunflow.parameter("gi.engine", "ambocc"); 
        sunflow.parameter("gi.ambocc.bright", "sRGB nonlinear", 1.0f, 1.0f, 1.0f);
        sunflow.parameter("gi.ambocc.dark", "sRGB nonlinear", 0.0f, 0.0f, 0.0f);
        sunflow.parameter("gi.ambocc.samples", 16);
        sunflow.parameter("gi.ambocc.maxdist", 2.0f);
        
        sunflow.parameter("camera", "thinlens");
        sunflow.parameter("resolutionX", width);
        sunflow.parameter("resolutionY", height);
        sunflow.parameter("aa.min", 1);
        sunflow.parameter("aa.max", 3);
        sunflow.parameter("bucket.order", "column");
        sunflow.parameter("filter", "mitchell");

        sunflow.options(SunflowAPI.DEFAULT_OPTIONS);
        
        FileDisplay fileDisplay = new FileDisplay(filepath);
        sunflow.render(SunflowAPI.DEFAULT_OPTIONS, fileDisplay);
    }
    
    private double[] convertToParallel(double[] points, Point3d cameraPos, Point3d cameraFocus, double screenScale) {
        //project all the points to a plane with normal: cameraFocus to cameraPos
        Vector3d normal = new Vector3d(); //normal of target plane
        normal.sub(cameraPos, cameraFocus);
        normal.normalize();
        
        int n = points.length;
        double[] parallelPoints = new double[n];
        
        for (int i=0; i<n; i+=3) {
            Point3d p = new Point3d(points[i], points[i+1], points[i+2]);
            Point3d p1 = new Point3d(); //projected to plane
            
            //p1 = p - dot(p - focus, normal) * normal
            Vector3d pointMinusFocus = new Vector3d();
            pointMinusFocus.sub(p, cameraFocus);
            
            double dot = pointMinusFocus.dot(normal);
            p1.x = p.x - dot * normal.x;
            p1.y = p.y - dot * normal.y;
            p1.z = p.z - dot * normal.z;
            
            //p1 is now in the projected position, but still not scaled correctly by zoom level
            //lets scale it
            double cameraToObj = cameraPos.distance(cameraFocus);
            double factor = 1.5 * cameraToObj; //trial & error
            p1.x = (screenScale * factor * (p1.x - cameraFocus.x)) + cameraFocus.x;
            p1.y = (screenScale * factor * (p1.y - cameraFocus.y)) + cameraFocus.y;
            p1.z = (screenScale * factor * (p1.z - cameraFocus.z)) + cameraFocus.z;
            
            parallelPoints[i] = p1.x;
            parallelPoints[i+1] = p1.y;
            parallelPoints[i+2] = p1.z;
        }

        
        return parallelPoints;
    }
    
    public void cleanup() {
        sunflow.reset();
    }

}
