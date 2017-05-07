/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.cor.navigator.funiteditor;

import com.sun.j3d.utils.picking.PickCanvas;
import com.sun.j3d.utils.picking.PickIntersection;
import com.sun.j3d.utils.picking.PickResult;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.GraphicsConfigTemplate3D;
import javax.media.j3d.PhysicalBody;
import javax.media.j3d.PhysicalEnvironment;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;
import javax.media.j3d.ViewPlatform;
import javax.media.j3d.VirtualUniverse;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.SingularMatrixException;
import javax.vecmath.Vector3d;

/**
 *
 * @author risman
 */
public class FUnitViewerPanel extends JPanel {
    
    public static final int XY = 1;
    public static final int YZ = 2;
    public static final int ZX = 3;
    public static final int PERSPECTIVE = 0;
    
    private FlowLayout layout;
    private JPanel toolsPanel;
    private JLabel toolsLabel;
    private JButton toolsRotateButton;
    private JButton toolsTranslateButton;
    
    private FUnitEditorPanel editor;
    private int viewMode;
    
    private Canvas3D canvas;
    private VirtualUniverse u;
    private View v;
    
    private Point3d cameraPos = new Point3d();
    private Point3d cameraFocus = new Point3d();
    private Vector3d up = new Vector3d();
    private Point3d mousePos3D = new Point3d();
    private double camDist;

    private double fieldOfView;
    private TransformGroup cameraTG;
    private Transform3D cameraT3D;
    
    //mouse for camera
    private Point2d lastMousePos1 = new Point2d();
    private Point2d lastMousePos2 = new Point2d();
    private int lastClicked;
    private boolean pressed = false;
    
    //mouse for point drag
    private Point2d lastMousePos = new Point2d();
    private boolean dragging = false;

    //hover highlight
    PickCanvas pickCanvas;
    int n;
    public static final int NONE = 0;
    public static final int POINT = 1;
    public static final int MID = 2;
    private int hoverType = 0;
    private Integer hoverIdx = null;
    private boolean hovered = false;
    private boolean selected = false;
    private Integer selectIdx = null;
    
    public FUnitViewerPanel(FUnitEditorPanel editor, int viewMode) {
        
        this.editor = editor;
        this.viewMode = viewMode;
        
        this.setLayout(new BorderLayout());

        layout = new FlowLayout();
        layout.setAlignment(FlowLayout.LEFT);

        //the toolbar
        toolsPanel = new JPanel(layout);

        toolsLabel = new JLabel();
        toolsLabel.setFont(Font.getFont(Font.MONOSPACED));
        if (viewMode == XY) {
            toolsLabel.setText("XY");
        } else if (viewMode == YZ) {
            toolsLabel.setText("YZ");
        } else if (viewMode == ZX) {
            toolsLabel.setText("ZX");
        } else if (viewMode == PERSPECTIVE) {
            toolsLabel.setText("XYZ");
        }
        toolsPanel.add(toolsLabel);
        
        toolsRotateButton = new JButton("Rotate");
        toolsRotateButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                editor.popupRotate(viewMode);
            }
        });
        
        toolsPanel.add(toolsRotateButton);
        
        toolsTranslateButton = new JButton("Translate");
        toolsTranslateButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                editor.popupTranslate(viewMode);
            }
        });
        
        toolsPanel.add(toolsTranslateButton);

        this.add(BorderLayout.NORTH, toolsPanel);
        
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        canvas = new Canvas3D(gd.getBestConfiguration(new GraphicsConfigTemplate3D()));
        canvas.setMinimumSize(new Dimension());
        this.add(BorderLayout.CENTER, canvas);
    }
    
    public Canvas3D getCanvas() {
        return canvas;
    }
    
    public void setupViewBranch() {
        BranchGroup vpBG = new BranchGroup();
        cameraTG = new TransformGroup(); //vpTG
        cameraTG.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        cameraTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        cameraT3D = new Transform3D();
        cameraT3D.set(new Vector3d(0.0d, 0.0d, 1/Math.tan(Math.PI/4.0)));
        cameraTG.setTransform(cameraT3D);
        ViewPlatform vp = new ViewPlatform();
        cameraTG.addChild(vp);
        vpBG.addChild(cameraTG);
        PhysicalBody bp = new PhysicalBody();
        PhysicalEnvironment pe = new PhysicalEnvironment();
        v = new View();
        v.setScreenScalePolicy(View.SCALE_EXPLICIT);
        v.setBackClipPolicy(View.VIRTUAL_EYE);
        v.setFrontClipPolicy(View.VIRTUAL_EYE);
        v.setBackClipDistance(10000);
        v.setFrontClipDistance(0.5);
        v.setDepthBufferFreezeTransparent(false);
        
        v.addCanvas3D(canvas);
        
        v.setPhysicalBody(bp);
        v.setPhysicalEnvironment(pe);
        
        fieldOfView = v.getFieldOfView();
        
        v.attachViewPlatform(vp);

        if (viewMode == PERSPECTIVE) {
            v.setProjectionPolicy(View.PERSPECTIVE_PROJECTION);
        } else {
            v.setProjectionPolicy(View.PARALLEL_PROJECTION);
        }
        
        editor.getULocale().addBranchGraph(vpBG);
    }
        
    public void setupCameraAndMouse() {
        cameraDefault();
        setupMouseInteraction();
    }
    
    public void cameraDefault() {

        double d = getObjRadius() / Math.sin(Math.toDegrees(fieldOfView));
        d *= 4;

        cameraFocus = getObjCenter();

        if (viewMode == XY) {
            cameraPos.set(cameraFocus.getX(), cameraFocus.getY(), d);
            up.set(0, 1, 0);
        } else if (viewMode == YZ) {
            cameraPos.set(d, cameraFocus.getY(), cameraFocus.getZ());
            up.set(0, 0, 1);
        } else if (viewMode == ZX) {
            cameraPos.set(cameraFocus.getX(), d, cameraFocus.getZ());
            up.set(1, 0, 0);
        } else if (viewMode == PERSPECTIVE) {
            double diagonal = d / Math.sqrt(3);
            cameraPos.set(diagonal, diagonal, diagonal);
            up.set(0, 1, 0);
            v.setScreenScale(1.0);
        }

        if (viewMode != PERSPECTIVE) {
            v.setScreenScale(0.7 / d);
        }

        moveCamera();
    }

    private void moveCamera() {
        try {
            cameraT3D.lookAt(cameraPos, cameraFocus, up);
            cameraT3D.invert();
            cameraTG.setTransform(cameraT3D);

            //camera distance to object
            camDist = cameraPos.distance(getObjCenter());

        } catch (SingularMatrixException ex) {}
    }
    
    private double getObjRadius() {
        return getBS().getRadius();
    }

    private BoundingSphere getBS() {
        return (BoundingSphere) editor.getShapeBG().getBounds();
    }

    private Point3d getObjCenter() {
        Point3d objCenter = new Point3d();
        getBS().getCenter(objCenter);
        return objCenter;
    }
    
    public void setupMouseInteraction() {

        pickCanvas = new PickCanvas(canvas, editor.getRootBG());

        //for XY, YZ, and ZX view, we can modify the points
        if (viewMode != PERSPECTIVE) {

            canvas.addMouseMotionListener(new MouseMotionListener() {

                @Override
                public void mouseDragged(MouseEvent e) {
                    //if point selected or point highlighed, move
                    if ((selected) || (hovered && hoverType == POINT)) {
                        dragging = true;
                        Integer toBeDraggedIdx;
                        if (selected) {
                            toBeDraggedIdx = selectIdx;
                        } else {
                            toBeDraggedIdx = hoverIdx;
                        }
                        
                        double distH = distanceConvert((double) e.getX() - lastMousePos.x);
                        double distV = -distanceConvert((double) e.getY() - lastMousePos.y);
                        
                        if (viewMode == XY) {
                            editor.updatePoint(toBeDraggedIdx, new Point3d(distH, distV, 0.f));
                        } else if (viewMode == YZ) {
                            editor.updatePoint(toBeDraggedIdx, new Point3d(0.0f, distH, distV));
                        } else if (viewMode == ZX) {
                            editor.updatePoint(toBeDraggedIdx, new Point3d(distV, 0.0f, distH));
                        }
                        
                        lastMousePos.set(e.getX(), e.getY());
                    }
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    //try to snap, point highlight if found, line highlight too
                    if (e.getX() > 0 && e.getX() < canvas.getWidth()
                            && e.getY() > 0 && e.getY() < canvas.getHeight()) {
                        hoverIdx = getMouseIntersection(e);
                        if (hoverType != NONE) {
                            editor.setHoverHighlightOn(hoverType, hoverIdx);
                            hovered = true;
                        } else {
                            editor.setHoverHighlightOff();
                            if (selected)
                                editor.setSelectHighlightOn(selectIdx);
                            hoverType = NONE;
                            hoverIdx = null;
                            hovered = false;
                        }
                    }
                }
            });

            canvas.addMouseListener(new MouseListener() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    //if highlighted, select. 
                    //If selected and not in snap area, unselect.
                    //if double click and point highlighted, 
                    //popup edit
                    //if double click and line highlighted, add point
                    
                    //select highlighted
                    if (e.getClickCount() == 1 && hovered && hoverType == POINT) {
                        selectIdx = hoverIdx;
                        editor.setSelectHighlightOff();
                        editor.setSelectHighlightOn(selectIdx);
                        selected = true;
                    } 
                    //unselect
                    else if (selected && !hovered) { 
                        editor.setSelectHighlightOff();
                        selected = false;
                        selectIdx = null;
                    }
                    //popup edit
                    else if (e.getClickCount() == 2 && hovered && hoverType == POINT) {
                        editor.popupEdit(hoverIdx);
                        editor.setHoverHighlightOff();
                        hovered = false;
                        hoverType = NONE;
                        hoverIdx = null;
                        editor.setSelectHighlightOff();
                        selected = false;
                        selectIdx = null;
                    }
                    //add point
                    else if (e.getClickCount() == 2 && hovered & hoverType == MID) {
                        editor.insertPoint(hoverIdx, viewMode);
                        editor.setHoverHighlightOff();
                        hovered = false;
                        hoverType = NONE;
                        hoverIdx = null;
                        editor.setSelectHighlightOff();
                        selected = false;
                        selectIdx = null;
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    lastMousePos.set(e.getX(), e.getY());
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (dragging) {
                        editor.updateFUnit();
                    }
                    dragging = false;
                }

                @Override
                public void mouseEntered(MouseEvent e) {}

                @Override
                public void mouseExited(MouseEvent e) {}

            });

            canvas.addKeyListener(new KeyListener() {

                @Override
                public void keyTyped(KeyEvent e) {}

                @Override
                public void keyPressed(KeyEvent e) {
                    //if selected then "delete" pressed
                    if (selected && e.getKeyCode() == KeyEvent.VK_DELETE) {
                        editor.removePoint(selectIdx);
                        editor.setHoverHighlightOff();
                        hovered = false;
                        hoverType = NONE;
                        hoverIdx = null;
                        if (selectIdx != null)
                        editor.setSelectHighlightOff();
                        selected = false;
                        selectIdx = null;
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {}
            });

        } else { //for perspective view, we can move around the object

            canvas.addMouseListener(new MouseListener() {

                @Override
                public void mouseClicked(MouseEvent e) {}

                @Override
                public void mousePressed(MouseEvent e) {

                    lastClicked = e.getButton();
                    if (lastClicked == MouseEvent.BUTTON1 || lastClicked == MouseEvent.BUTTON3) {
                        lastMousePos1.set(e.getX(), e.getY());
                    } else if (lastClicked == MouseEvent.BUTTON2) {
                        pressed = true;
                        lastMousePos2.set(e.getX(), e.getY());
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON2) {
                        pressed = false;
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {}

                @Override
                public void mouseExited(MouseEvent e) {}
            });

            //drag
            canvas.addMouseMotionListener(new MouseMotionListener() {

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (e.getX() > 0 && e.getX() < canvas.getWidth()
                            && e.getY() > 0 && e.getY() < canvas.getHeight()) {

                        if (lastClicked == 1 || lastClicked == 3) {

                            Transform3D shift = new Transform3D();

                            //distance of mouse movement
                            double distH = -distanceConvert((double) e.getX() - lastMousePos1.x);
                            double distV = distanceConvert((double) e.getY() - lastMousePos1.y);

                            Vector3d shiftHVec = getCameraLeftDir();
                            Vector3d shiftVVec = new Vector3d(up);

                            shiftHVec.normalize();
                            shiftHVec.scale(distH);
                            shiftVVec.normalize();
                            shiftVVec.scale(distV);

                            shift.setTranslation(shiftHVec);
                            shift.transform(cameraPos);
                            shift.transform(cameraFocus);
                            shift.setTranslation(shiftVVec);
                            shift.transform(cameraPos);
                            shift.transform(cameraFocus);

                            lastMousePos1.set(e.getX(), e.getY());
                        } else //middle click drag: rotate
                        if (lastClicked == 2) {

                            //distance of mouse movement
                            double distH = -((double) e.getX() - lastMousePos2.x);
                            double distV = (double) e.getY() - lastMousePos2.y;

                            //distance to angle
                            distH = distH / canvas.getWidth() * 180.0;
                            distV = distV / canvas.getHeight() * 180.0;

                            if (!e.isControlDown()) {
                                //rotate
                                pitchCamera(Math.toRadians(-distV));
                                yawCamera(Math.toRadians(distH));
                            } else {
                                //roll
                                if (e.getX() > canvas.getWidth() / 2) {
                                    distV = -distV;
                                }
                                if (e.getY() > canvas.getHeight() / 2) {
                                    distH = -distH;
                                }
                                rollCamera(Math.toRadians(distH * 2));
                                rollCamera(Math.toRadians(distV * 2));

                            }

                            lastMousePos2.set(e.getX(), e.getY());
                        }
                        moveCamera();
                    }
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                }
            });

            canvas.addMouseWheelListener(new MouseWheelListener() {

                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    if (!pressed) {
                        int amount = e.getWheelRotation();
                        //zoom
                        //get mouse pos in 3d
                        canvas.getPixelLocationInImagePlate(e.getX(), e.getY(), mousePos3D);
                        Transform3D mouseTransform = new Transform3D();
                        canvas.getImagePlateToVworld(mouseTransform);
                        mouseTransform.transform(mousePos3D);

                        //movement vector from camera to mouse, zoom in
                        Vector3d moveV = new Vector3d();
                        moveV.sub(cameraPos, mousePos3D);
                        moveV.normalize();
                        double factor = camDist / 10;

                        //if scroll down, movement from mouse to camera, zoom out
                        moveV.scale(factor * amount);

                        Transform3D zoom = new Transform3D();
                        zoom.setTranslation(moveV);
                        zoom.transform(cameraPos);
                        zoom.transform(cameraFocus);

                        moveCamera();
                    }
                }
            });
        }
    }

    
    //Convert coordinate distances from scales in JPanel to scales in Java3D.
    //Just approximation.
    private double distanceConvert(double inPanel) {
        double factor;
        factor = (double) canvas.getWidth() / (camDist * 0.77); //trial & error
        double inJ3d = inPanel / factor;
        return inJ3d;
    }

    //Right hand rule for camera:
    //index finger: cameraPos to cameraFocus
    //thumb: up
    //middle finger:c cameraPos x up
    private Vector3d getCameraDir() {
        Vector3d camDir = new Vector3d();
        camDir.x = cameraFocus.x - cameraPos.x;
        camDir.y = cameraFocus.y - cameraPos.y;
        camDir.z = cameraFocus.z - cameraPos.z;
        return camDir;
    }

    private Vector3d getCameraLeftDir() {
        Vector3d camHShift = new Vector3d();
        camHShift.cross(getCameraDir(), up);
        return camHShift;
    }

    private void pitchCamera(double angle) {
        Vector3d backToZero;
        Point3d center;
        center = getObjCenter();
        backToZero = new Vector3d(center);
        backToZero.negate();
        Transform3D back = new Transform3D();
        back.set(backToZero);

        Transform3D rotate = new Transform3D();
        rotate.set(new AxisAngle4d(getCameraLeftDir(), angle));

        Transform3D toOri = new Transform3D();
        toOri.set(new Vector3d(center));

        //creating composite transform, reversing the order
        Transform3D fullRotate = new Transform3D(toOri);
        fullRotate.mul(rotate);
        fullRotate.mul(back);

        fullRotate.transform(cameraPos);
        fullRotate.transform(cameraFocus);
        fullRotate.transform(up);
    }

    private void yawCamera(double angle) {
        Vector3d backToZero;
        Point3d center;
        center = getObjCenter();
        backToZero = new Vector3d(center);
        backToZero.negate();
        Transform3D back = new Transform3D();
        back.set(backToZero);

        Transform3D rotate = new Transform3D();
        rotate.set(new AxisAngle4d(up, angle));

        Transform3D toOri = new Transform3D();
        toOri.set(new Vector3d(center));

        //creating composite transform, reversing the order
        Transform3D fullRotate = new Transform3D(toOri);
        fullRotate.mul(rotate);
        fullRotate.mul(back);

        fullRotate.transform(cameraPos);
        fullRotate.transform(cameraFocus);
    }

    private void rollCamera(double angle) {
        Vector3d axis = getCameraDir();
        Transform3D rotate = new Transform3D();
        rotate.set(new AxisAngle4d(axis, angle));
        rotate.transform(up);
    }

    private Integer getMouseIntersection(MouseEvent e) {
        Point3d resultPoint = null;
        Shape3D s = null;

        pickCanvas.setShapeLocation(e);
        PickResult[] allResults = null;

        try {
            allResults = pickCanvas.pickAllSorted();
        } catch (AssertionError ex) {}

        if (allResults != null) {
            for (PickResult result : allResults) {
                s = (Shape3D) result.getNode(PickResult.SHAPE3D);
                if (s != null) {
                    try {
                        PickIntersection intersection = result.getClosestIntersection(cameraPos);
                        if (s.getGeometry() instanceof QuadArray) {
                            resultPoint = intersection.getPointCoordinatesVW();
                            break;
                        }
                    } catch (NullPointerException ex) {}
                }
            }
        }
        
        if (resultPoint != null && s != null) { //let's snap
            ArrayList<Point3d> topPoints = editor.getTopPoints();
            ArrayList<Point3d> bottomPoints = editor.getBotPoints();
            ArrayList<Point3d> topMidPoints = editor.getTopMidPoints();
            ArrayList<Point3d> bottomMidPoints = editor.getBotMidPoints();
            
            for(int i=0; i<topPoints.size(); i++) {
                if (resultPoint.distance(topPoints.get(i)) < 0.185) {
                    hoverType = POINT;
                    return i;
                } else if (resultPoint.distance(bottomPoints.get(i)) < 0.185) {
                    hoverType = POINT;
                    return i;
                } else if (resultPoint.distance(topMidPoints.get(i)) < 0.185) {
                    hoverType = MID;
                    return i;
                } else if (resultPoint.distance(bottomMidPoints.get(i)) < 0.185) {
                    hoverType = MID;
                    return i;
                }
            }
        }
        
        hoverType = NONE;
        return null;
    }
}
