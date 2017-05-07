/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.corak.component;

import com.jbatik.filetype.cor.CorakDataObject;
import com.jbatik.modules.corak.CorakLSystem;
import com.jbatik.modules.corak.canting.Canting;
import com.jbatik.modules.corak.component.Measurer.Mark;
import com.jbatik.modules.corak.component.Measurer.Marker;
import com.jbatik.modules.corak.sunflowtools.MySunflowRenderer;
import com.jbatik.modules.interfaces.Appearance3DChangerCookie;
import com.jbatik.modules.interfaces.Scene3DObserverCookie;
import com.sun.j3d.utils.picking.PickCanvas;
import com.sun.j3d.utils.picking.PickIntersection;
import com.sun.j3d.utils.picking.PickResult;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.ViewingPlatform;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import javax.imageio.ImageIO;
import javax.media.j3d.AmbientLight;
import javax.media.j3d.Appearance;
import javax.media.j3d.Background;
import javax.media.j3d.BadTransformException;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Group;
import javax.media.j3d.IllegalRenderingStateException;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.LineArray;
import javax.media.j3d.LineAttributes;
import javax.media.j3d.Node;
import javax.media.j3d.PointLight;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;
import javax.swing.event.MouseInputListener;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Color3f;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.SingularMatrixException;
import javax.vecmath.Vector3d;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Cancellable;

/**
 *
 * @author RAPID01
 */
public class KainCorak extends Canvas3D implements MouseInputListener, MouseWheelListener, KeyListener {

    private Canting canting;
    private String projectPath;

    private SimpleUniverse u;
    private ViewingPlatform vp;
    private TransformGroup cameraTG;
    private Transform3D cameraT3D;
    private BranchGroup kainBG;
    private TransformGroup kainTG;
    private View view;

    private long timer;

    //View tools
    private Appearance3DChangerCookie.Appearance currentAppearance;
    private Scene3DObserverCookie.Projection currentProjectionMode;
    private Background background;
    private BranchGroup axisBG;
    private Group axisLines;
    private BranchGroup groundBG;
    private Group ground;
    private PointLight camLamp;
    private AmbientLight ambient;
    private boolean axisState;
    private boolean groundState;
    private boolean lightState;
    private final int AXIS_LINES_LENGTH = 10000;

    private BoundingSphere bs;

    //camera stuffs
    private Point3d cameraPos = new Point3d();
    private Point3d cameraFocus = new Point3d();
    private Vector3d up = new Vector3d();
    private Point3d mousePos3D = new Point3d();
    private double camDist;
    private double fieldOfView;
    private final double FARTHEST = 50000;
    private final double CLOSEST = 0.5;
    private double defaultParallelScreenScale;

    //mouse stuffs
    private Point2d lastMousePos1 = new Point2d(); //for left/right click
    private Point2d lastMousePos2 = new Point2d(); //for mid click
    private int lastClicked; //1: left click, 2: mid click, 3: right click
    private boolean pressed = false;

    //keyboard stuffs
    private boolean isADown = false;
    private boolean isWDown = false;
    private boolean isEDown = false;
    private boolean justModAppearance = false;

    //measuring stuffs
    private boolean measuring = false;
    private boolean measurementVisible = true;
    private Measurer measurer;
    private PickCanvas pickCanvas;
    private BoundingSphere cursorBound;
    private BoundingSphere thresholdBound;
    private LinkedList<Point3d> snapList;
    private static float CURSOR_THRESHOLD = 0.3f;
    private static float SNAP_THRESHOLD = 0.4f;
    private Node hoveredNode = null;
    private Node lastHoveredNode = null;
    private Node selectedNode = null;

    //rotate stuff
    private boolean choosingPivotPoint = false;
    private PivotPointAdjustor pivotPointAdjustor;
    ;
    private PickCanvas pickCanvas2;

    //animation stuffs
    private Thread animationThread = null;
    private boolean animatingX = false;
    private boolean animatingY = false;
    private boolean animatingZ = false;

    private ProgressHandle pHandle;
    private boolean stillRendering;

    public KainCorak(GraphicsConfiguration graphicsConfiguration) {
        super(graphicsConfiguration);
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        addKeyListener(this);
        setMinimumSize(new Dimension());
    }

    public Canting getCanting() {
        return canting;
    }

    public void init() {
        //initialize our SimpleUniverse -> u
        u = new SimpleUniverse(this);

        //set viewing platform (the camera)
        vp = u.getViewingPlatform();
        view = u.getViewer().getView();
        view.setScreenScalePolicy(View.SCALE_EXPLICIT);
        view.setBackClipPolicy(View.VIRTUAL_EYE);
        view.setFrontClipPolicy(View.VIRTUAL_EYE);
        view.setBackClipDistance(FARTHEST);
        view.setFrontClipDistance(CLOSEST);
        fieldOfView = view.getFieldOfView();
        view.setDepthBufferFreezeTransparent(false);
        cameraTG = vp.getViewPlatformTransform();
        cameraT3D = new Transform3D();
        cameraTG.getTransform(cameraT3D);

        //initialize root of our scene -> BranchGroup kainBG and TransformGroup kainTG
        kainBG = new BranchGroup();
        kainBG.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
        kainBG.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
        kainBG.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);

        background = new Background(new Color3f(0, 0, 0));
        background.setCapability(Background.ALLOW_COLOR_READ);
        background.setCapability(Background.ALLOW_COLOR_WRITE);
        background.setCapability(Background.ALLOW_IMAGE_READ);
        background.setCapability(Background.ALLOW_IMAGE_WRITE);
        background.setApplicationBounds(new BoundingSphere(new Point3d(0, 0, 0), FARTHEST));
        background.setImageScaleMode(Background.SCALE_FIT_ALL);
        kainBG.addChild(background);

        kainTG = new TransformGroup();
        kainTG.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
        kainTG.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
        kainBG.addChild(kainTG);

        //branch for axis lines
        axisBG = new BranchGroup();
        axisBG.setCapability(BranchGroup.ALLOW_DETACH);
        axisLines = createAxisLines();
        axisBG.addChild(axisLines);

        //branch for ground
        groundBG = new BranchGroup();
        groundBG.setCapability(BranchGroup.ALLOW_DETACH);
        ground = createGround();
        groundBG.addChild(ground);

        kainBG.compile();
        u.addBranchGraph(kainBG);
        currentAppearance = Appearance3DChangerCookie.Appearance.WIREFRAME;

    }

    private Group createAxisLines() {
        Group lineGroup = new Group();

        Color3f paleRed = new Color3f(0.5f, 0.3f, 0.3f);
        Color3f paleGreen = new Color3f(0.3f, 0.5f, 0.3f);
        Color3f paleBlue = new Color3f(0.3f, 0.3f, 0.5f);

        Point3f zero = new Point3f(0, 0, 0);

        lineGroup.addChild(createLine(zero, new Point3f(AXIS_LINES_LENGTH, 0, 0), paleRed, false, 2.0f)); //x positive
        lineGroup.addChild(createLine(zero, new Point3f(-AXIS_LINES_LENGTH, 0, 0), paleRed, true, 2.0f)); //x negative
        lineGroup.addChild(createLine(zero, new Point3f(0, AXIS_LINES_LENGTH, 0), paleGreen, false, 2.0f)); //y positive
        lineGroup.addChild(createLine(zero, new Point3f(0, -AXIS_LINES_LENGTH, 0), paleGreen, true, 2.0f)); //y negative
        lineGroup.addChild(createLine(zero, new Point3f(0, 0, AXIS_LINES_LENGTH), paleBlue, false, 2.0f)); //z positive
        lineGroup.addChild(createLine(zero, new Point3f(0, 0, -AXIS_LINES_LENGTH), paleBlue, true, 2.0f)); //z negative

        return lineGroup;
    }

    private Shape3D createLine(Point3f start, Point3f end, Color3f color, boolean isDashed, float lineWidth) {
        Appearance app = new Appearance();
        app.setColoringAttributes(new ColoringAttributes(color, ColoringAttributes.SHADE_FLAT));
        if (isDashed) {
            app.setLineAttributes(new LineAttributes(lineWidth, LineAttributes.PATTERN_DASH, true));
        } else {
            app.setLineAttributes(new LineAttributes(lineWidth, LineAttributes.PATTERN_SOLID, true));
        }
        LineArray lineArr = new LineArray(2, LineArray.COORDINATES);
        lineArr.setCoordinate(0, start);
        lineArr.setCoordinate(1, end);
        Shape3D line = new Shape3D(lineArr, app);
        return line;
    }

    private Group createGround() {
        Group lineGroup = new Group();

        Color3f groundColor = new Color3f(0.3f, 0.3f, 0.3f);

        int far = (int) (0.5 * AXIS_LINES_LENGTH);

        for (int x = -far; x < far; x++) {
            Point3f start = new Point3f(x, 0, far);
            Point3f end = new Point3f(x, 0, -far);
            lineGroup.addChild(createLine(start, end, groundColor, false, 1.0f));
        }

        for (int z = -far; z < far; z++) {
            Point3f start = new Point3f(far, 0, z);
            Point3f end = new Point3f(-far, 0, z);
            lineGroup.addChild(createLine(start, end, groundColor, false, 1.0f));
        }

        return lineGroup;
    }

    public void prepareCorak(String projectPath, CorakLSystem cor, CorakDataObject obj, boolean encloseOBJ) {

        this.projectPath = projectPath;

        //initiate our "turtle" -> Canting
        canting = new Canting(cor, obj);

        //start creating shapes based on Cor
        timer = System.currentTimeMillis();
        canting.generate(encloseOBJ);

        BoundingSphere lightingBounds = new BoundingSphere(new Point3d(0, 0, 0), FARTHEST);
        camLamp = new PointLight();
        camLamp.setCapability(PointLight.ALLOW_POSITION_READ);
        camLamp.setCapability(PointLight.ALLOW_POSITION_WRITE);
        camLamp.setColor(new Color3f(1.0f, 1.0f, 1.0f));
        camLamp.setInfluencingBounds(lightingBounds);

        ambient = new AmbientLight();
        ambient.setColor(new Color3f(1.0f, 1.0f, 1.0f));
        ambient.setInfluencingBounds(lightingBounds);

        canting.getBatikBG().addChild(camLamp);
        canting.getBatikBG().addChild(ambient);

        //attach generated shapes to root TransformGroup
        Enumeration kaintgchildren = kainTG.getAllChildren();
        while (kaintgchildren.hasMoreElements()) {
            Node child = (Node) kaintgchildren.nextElement();
            if (child instanceof Measurer) {
                ((Measurer) child).removeAllChildren();
            } else {
                kainTG.removeChild(child);
            }
        }
//        canting.getBatikBG().compile();
        kainTG.addChild(canting.getBatikBG());

        timer = System.currentTimeMillis() - timer;
        //System.out.println("Timer= " + timer);

        bs = (BoundingSphere) canting.getBatikBG().getBounds();
    }

    public BranchGroup getKainBG() {
        return kainBG;
    }

    public void setCurrentAppearance(Appearance3DChangerCookie.Appearance currentAppearance) {
        this.currentAppearance = currentAppearance;
        refreshAppearance();
    }

    public Appearance3DChangerCookie.Appearance getCurrentAppearance() {
        return currentAppearance;
    }

    public void setProjectionMode(Scene3DObserverCookie.Projection mode) {
        if (currentProjectionMode != mode) {
            if (mode == Scene3DObserverCookie.Projection.PERSPECTIVE) {
                view.setProjectionPolicy(View.PERSPECTIVE_PROJECTION);
            } else {
                view.setProjectionPolicy(View.PARALLEL_PROJECTION);
            }
            currentProjectionMode = mode;
        }
    }

    public Scene3DObserverCookie.Projection getProjectionMode() {
        return currentProjectionMode;
    }

    public Point3d getObjCenter() {
        Point3d objCenter = new Point3d();
        bs.getCenter(objCenter);
        return objCenter;
    }

    public void cameraDefault() {

        //formula:
        //Z = minimal distance for the camera to see all the things
        //Z = R / sin(FOV)
        double z = getObjRadius() / Math.sin(Math.toDegrees(fieldOfView));

        z *= 4; //z not far enough!

        cameraFocus = getObjCenter();
        cameraPos.set(cameraFocus.getX(), cameraFocus.getY(), z);
        up.set(0, 1, 0);
        moveCamera();

        defaultParallelScreenScale = 0.7 / z;

        if (currentProjectionMode == Scene3DObserverCookie.Projection.PARALLEL) {
            view.setScreenScale(defaultParallelScreenScale);
        } else {
            view.setScreenScale(1.0);
        }
    }

    public double getObjRadius() {
        return bs.getRadius();
    }

    public void moveCamera() {
        try {
            cameraT3D.lookAt(cameraPos, cameraFocus, up);
            cameraT3D.invert();
            cameraTG.setTransform(cameraT3D);
            //lamp position at camera
            camLamp.setPosition(new Point3f(cameraPos));

            //camera distance to object
            camDist = cameraPos.distance(getObjCenter());

        } catch (SingularMatrixException ex) {
            ex.printStackTrace();
        }
    }

    public void changeBackgroundColor(Color newColor) {
        Color3f bgcolor3f = new Color3f();
        bgcolor3f.set(newColor);
        background.setColor(bgcolor3f);
    }

    public Color getBackgroundColor() {
        Color3f bgcolor3f = new Color3f();
        background.getColor(bgcolor3f);
        return bgcolor3f.get();
    }

    public void setAxisLines(boolean axisOn) {
        if (!axisOn) {
            kainBG.removeChild(axisBG);
            axisState = false;
        } else {
            kainBG.addChild(axisBG);
            axisState = true;
        }
    }

    public boolean getAxisLines() {
        return axisState;
    }

    public void setGround(boolean groundOn) {
        if (!groundOn) {
            kainBG.removeChild(groundBG);
            groundState = false;
        } else {
            kainBG.addChild(groundBG);
            groundState = true;
        }
    }

    public boolean getGround() {
        return groundState;
    }

    public void setLights(boolean enabled) {
        lightState = enabled;
        refreshAppearance();
    }

    public boolean getLights() {
        return lightState;
    }

    public void refreshAppearance() {
        canting.setAppearance(currentAppearance, lightState);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (measuring && measurementVisible) {
            if (measurer != null) {
                try {
                    measurer.getMarker().setVisible(false);
                } catch (NullPointerException ex) {
                }
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (e.getX() > 0 && e.getX() < this.getWidth()
                && e.getY() > 0 && e.getY() < this.getHeight()
                && (canting.getCor() != null)
                && measuring
                && measurementVisible) {
            MyPickResult result = getMouseIntersection(e, true);
            Point3d pos = result.getPoint();
            if (pos != null) {
                if (measurer == null) {
                    measurer = new Measurer(pos);
                    kainTG.addChild(measurer);
                    initSnapList();
                } else {
                    measurer.getMarker().updatePos(pos);
                    measurer.getMarker().setVisible(true);
                }
                hoveredNode = result.getNode();
            } else {
                hoveredNode = null;
            }

            //if hovers any mark, highlight it
            if (hoveredNode != null
                    && hoveredNode.getParent().getParent().getParent() instanceof Mark) {

                //before highlight, unhighlight previous mark
                if (lastHoveredNode != null) {
                    if (selectedNode != null && lastHoveredNode.getParent().getParent().getParent()
                            == selectedNode.getParent().getParent().getParent()) //if it is selected
                    {
                        ((Mark) selectedNode.getParent().getParent().getParent()).select();
                    } else {
                        ((Mark) lastHoveredNode.getParent().getParent().getParent()).unHighlight();
                    }
                    lastHoveredNode = null;
                }

                //highlight
                ((Mark) hoveredNode.getParent().getParent().getParent()).highlight();
                lastHoveredNode = hoveredNode;

            } else {
                //unhighlight previous mark
                if (lastHoveredNode != null) {
                    if (selectedNode != null && lastHoveredNode.getParent().getParent().getParent()
                            == selectedNode.getParent().getParent().getParent()) //if it is selected
                    {
                        ((Mark) selectedNode.getParent().getParent().getParent()).select();
                    } else {
                        ((Mark) lastHoveredNode.getParent().getParent().getParent()).unHighlight();
                    }
                    lastHoveredNode = null;
                }
            }
        }

        if (e.getX() > 0 && e.getX() < this.getWidth()
                && e.getY() > 0 && e.getY() < this.getHeight()
                && (canting.getCor() != null)
                && choosingPivotPoint) {
            MyPickResult result = getMouseIntersection2(e);
            Point3d pos = result.getPoint();
            if (pos != null) {
                if (pivotPointAdjustor == null) {
                    pivotPointAdjustor = new PivotPointAdjustor(pos);
                    kainTG.addChild(pivotPointAdjustor);
                } else {
                    pivotPointAdjustor.getPlacer().updatePos(pos);
                }
            }

        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (measurer != null && measurementVisible) {
            Marker marker = measurer.getMarker();
            Point3d markerPos = marker.getMarkerPos();

            //if there's any selected node, unselect it
            if (selectedNode != null) {
                ((Mark) selectedNode.getParent().getParent().getParent()).unSelect();
                selectedNode = null;
            }

            if (hoveredNode != null
                    && hoveredNode.getParent().getParent().getParent() instanceof Mark) {

                //select the clicked one
                selectedNode = hoveredNode;
                ((Mark) selectedNode.getParent().getParent().getParent()).select();
            } //click to add a mark dot
            else if (measuring) {
                Mark currentMark; //could be the last mark, or a new one
                try {
                    currentMark = measurer.getMarks().getLast();
                    if (!currentMark.isFristPointPlaced()) { //last mark completed
                        Mark newMark = measurer.createMark(); //create new one
                        newMark.createFirstPoint(markerPos);
                        marker.initMarkerLine(markerPos);
                    } else { //use last mark
                        currentMark.createSecondPoint(markerPos);
                        currentMark.createText();
                        currentMark.createMarkLine();
                        marker.removeMarkerLine();
                    }
                } catch (NoSuchElementException ex) { //no mark found
                    currentMark = measurer.createMark();
                    currentMark.createFirstPoint(markerPos);
                    marker.initMarkerLine(markerPos);
                }
            }
        }

        if (choosingPivotPoint) {
            pivotPointAdjustor.putPivotPoint(pivotPointAdjustor.getPlacer().getPos());
        }
    }

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
    public void mouseDragged(MouseEvent e) {

        if (e.getX() > 0 && e.getX() < this.getWidth()
                && e.getY() > 0 && e.getY() < this.getHeight()
                && (canting.getCor() != null)) {
            //left/right click drag: move camera left/right/up/down
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
                distH = distH / this.getWidth() * 180.0;
                distV = distV / this.getHeight() * 180.0;

                if (!e.isControlDown()) {
                    //rotate
                    pitchCamera(Math.toRadians(-distV));
                    yawCamera(Math.toRadians(distH));
                } else {
                    //roll
                    if (e.getX() > this.getWidth() / 2) {
                        distV = -distV;
                    }
                    if (e.getY() > this.getHeight() / 2) {
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
    public void mouseWheelMoved(MouseWheelEvent e) {
        if ((canting.getCor() != null) && !pressed) {
            //(canting.getCor() != null) -> make sure object exists
            //!pressed -> zooming while dragging can mess up savedCameraPos

            int amount = e.getWheelRotation();

            if (isADown || isWDown || isEDown) { //mod structure

                CorakLSystem cor = getCanting().getCor();

                if (isADown) {
                    cor.setAngle(cor.getAngle() - amount);
                }

                if (isWDown) {
                    if (amount < 0 || cor.getWidth() > 1) {
                        cor.setWidth(cor.getWidth() - amount);
                    }
                }

                if (isEDown) {
                    if (amount < 0 || cor.getLength() > 1) {
                        cor.setLength(cor.getLength() - amount);
                    }
                }

                this.prepareCorak(projectPath, cor, getCanting().getCorDataObject(), false);

                justModAppearance = true;

            } else { //zoom
                //get mouse pos in 3d
                this.getPixelLocationInImagePlate(e.getX(), e.getY(), mousePos3D);
                Transform3D mouseTransform = new Transform3D();
                this.getImagePlateToVworld(mouseTransform);
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

                if (currentProjectionMode == Scene3DObserverCookie.Projection.PARALLEL) {
                    //if parallel projection
                    //zoom don't make any difference, so, scale the view

                    if (e.getWheelRotation() < 0) {
                        view.setScreenScale(view.getScreenScale() * 1.109);
                    } else {
                        view.setScreenScale(view.getScreenScale() / 1.109);
                    }

                }

                moveCamera();
            }
        }

    }

    @Override
    public void keyTyped(KeyEvent ke) {
    }

    @Override
    public void keyPressed(KeyEvent ke) {

        char lastPressed = Character.toLowerCase(ke.getKeyChar());

        //remove mark
        if (ke.getKeyCode() == KeyEvent.VK_DELETE && selectedNode != null) {
            measurer.clearSelected(selectedNode);
        }

        //mod angle
        if (lastPressed == 'a') {
            isADown = true;
        }

        //mod width
        if (lastPressed == 'w') {
            isWDown = true;
        }

        //mod length
        if (lastPressed == 'e') {
            isEDown = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent ke) {

        char lastPressed = Character.toLowerCase(ke.getKeyChar());
        //mod angle
        if (lastPressed == 'a') {
            isADown = false;
            if (justModAppearance) {
                refreshAppearance();
            }
            justModAppearance = false;
        }

        //mod width
        if (lastPressed == 'w') {
            isWDown = false;
            if (justModAppearance) {
                refreshAppearance();
            }
            justModAppearance = false;
        }

        //mod length
        if (lastPressed == 'e') {
            isEDown = false;
            if (justModAppearance) {
                refreshAppearance();
            }
            justModAppearance = false;
        }
    }

    public BoundingSphere getObjectBounds() {
        return bs;
    }

    //Convert coordinate distances from scales in JPanel to scales in Java3D.
    //Just approximation.
    private double distanceConvert(double inPanel) {
        double factor;
        if (currentProjectionMode == Scene3DObserverCookie.Projection.PERSPECTIVE) {
            factor = (double) this.getWidth() / (camDist * 0.77); //trial & error
        } else {
            factor = (double) this.getWidth() / (0.55 / view.getScreenScale()); //trial & error
        }
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
        if (choosingPivotPoint && pivotPointAdjustor.getPivotPointBG() != null) {
            center = pivotPointAdjustor.getPivotPointPos();
        } else {
            center = getObjCenter();
        }
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
        if (choosingPivotPoint && pivotPointAdjustor.getPivotPointBG() != null) {
            center = pivotPointAdjustor.getPivotPointPos();
        } else {
            center = getObjCenter();
        }
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

    public void writePNG(final Dimension d, boolean withBackground, final String filepath, boolean sf) {
        if (!stillRendering) {
            Thread renderingThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    stillRendering = true;
                    long timer = System.currentTimeMillis();
                    pHandle.start();
                    try {
                        if (sf) {
                            Color3f bgcolor = new Color3f();
                            background.getColor(bgcolor);
                            MySunflowRenderer r = new MySunflowRenderer(
                                    canting,
                                    currentAppearance,
                                    currentProjectionMode,
                                    cameraPos,
                                    cameraFocus,
                                    up,
                                    fieldOfView,
                                    view.getScreenScale(),
                                    FARTHEST,
                                    CLOSEST,
                                    lightState,
                                    d.width,
                                    d.height,
                                    withBackground,
                                    bgcolor.get()
                            );
                            r.toPNG(filepath);
                            r.cleanup();
                        } else {
                            File output = new File(filepath);
                            try {
                                ImageIO.write(getRenderedImage(d, withBackground), "png", output);
                            } catch (IOException ex) {
                                System.out.println("writing to png using Java3D offscreen canvas failed.");
                            }

                            output = null;
                        }
                    } catch (OutOfMemoryError | IllegalRenderingStateException e) {
                        stillRendering = false;
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(
                                "Rendering PNG failed because of memory error.\nTry smaller output.",
                                "Failed",
                                NotifyDescriptor.DEFAULT_OPTION,
                                NotifyDescriptor.WARNING_MESSAGE));
                    }
                    System.gc();
                    System.out.println("RENDERING TIME: " + (System.currentTimeMillis() - timer) / 1000 + " s");
                    stillRendering = false;
                    pHandle.finish();
                }
            });

            String filename = (new File(filepath)).getName();
            filename = filename.substring(0, filename.length() - 4);

            String msg = "";
            msg = "Rendering " + filename + ".png";

            pHandle = ProgressHandleFactory.createHandle(msg, new Cancellable() {
                @Override
                public boolean cancel() {
                    pHandle.setDisplayName("Cancelling render ....");
                    Thread[] threads = new Thread[Thread.activeCount()];
                    Thread.enumerate(threads);
                    for (Thread thread : threads) {
                        if (MySunflowRenderer.isSunflowRenderingThread(thread)) {
                            thread.stop();
                        }
                    }
                    renderingThread.interrupt();
                    boolean deleted = false;
                    while (!deleted) {
                        deleted = (new File(filepath)).delete();
                    }
                    stillRendering = false;
                    pHandle.setDisplayName("Render cancelled.");
                    return true;
                }
            });
            renderingThread.start();
        } else {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(
                    "Please wait until the current rendering process finished.",
                    "Still Rendering",
                    NotifyDescriptor.DEFAULT_OPTION,
                    NotifyDescriptor.PLAIN_MESSAGE));
        }
    }

    /**
     * offscreencanvas ini harus pakai instance yang sama, soalnya ada batasan
     * bahwa rendering hanya bisa dilakukan di 32 buah canvas3d walaupun abis
     * diadd ntar diremove
     */
    private Canvas3D offscreenCanvas;

    public BufferedImage getRenderedImage(Dimension d, boolean withBackground) {
        if (offscreenCanvas == null) {
            offscreenCanvas = new Canvas3D(SimpleUniverse.getPreferredConfiguration(), true);
        }
        offscreenCanvas.setSize(getSize());
        offscreenCanvas.getScreen3D().setSize(getScreen3D().getSize());
        offscreenCanvas.getScreen3D().setPhysicalScreenWidth(getScreen3D().getPhysicalScreenWidth());
        offscreenCanvas.getScreen3D().setPhysicalScreenHeight(getScreen3D().getPhysicalScreenHeight());
        BufferedImage bufImage = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
        ImageComponent2D imgComp = new ImageComponent2D(ImageComponent2D.FORMAT_RGBA, bufImage);
        BufferedImage transparentImage = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
        ImageComponent2D transparentComp = new ImageComponent2D(ImageComponent2D.FORMAT_RGBA, transparentImage);

        Color3f tempColor = new Color3f();
        background.getColor(tempColor);
        if (!withBackground) {
            background.setImage(transparentComp);
        }

        view.addCanvas3D(offscreenCanvas);
        offscreenCanvas.setOffScreenBuffer(imgComp);
        offscreenCanvas.renderOffScreenBuffer();
        offscreenCanvas.waitForOffScreenRendering();

        if (!withBackground) {
            background.setImage(null);
        }

        bufImage = offscreenCanvas.getOffScreenBuffer().getImage();
        // To release the reference of buffer inside Java 3D.
        offscreenCanvas.setOffScreenBuffer(null);
        view.removeCanvas3D(offscreenCanvas);

        return bufImage;
    }

    public void setMeasurementMode(boolean enabled) {
        measuring = enabled;

        if (enabled) {
            if (choosingPivotPoint) { //disable choose-pivot-point mode
                setChoosePivotPointMode(false);
            }
            //initialize PickCanvas for mouse picking
            if (pickCanvas == null) {
                pickCanvas = new PickCanvas(this, kainBG);
                pickCanvas.setMode(PickCanvas.GEOMETRY);
            }
            if (snapList == null) {
                snapList = new LinkedList<Point3d>();
            }
        } else //clear remaining first point and remove the marker
        if (measurer != null) {
            try {
                Mark currentMark = measurer.getMarks().getLast();
                if (currentMark.isFristPointPlaced()) {
                    measurer.removeChild(currentMark);
                    currentMark.setFirstPointPlaced(false);
                }
            } catch (NoSuchElementException ex) {
            }
            measurer.getMarker().removeMarkerLine();
        }
    }

    public boolean getMeasurementModeEnabled() {
        return measuring;
    }

    void setMeasurementVisible(boolean visible) {
        measurementVisible = visible;

        if (measurer != null) {
            measurer.getMarker().setVisible(visible);
            for (Mark mark : measurer.getMarks()) {
                mark.setVisible(visible);
            }
        }
    }

    boolean getMeasurementVisible() {
        return measurementVisible;
    }

    void clearAllMeasurment() {
        if (measurer != null) {
            measurer.clearAllMarks();
            snapList.clear();
            initSnapList();
        }
    }

    private MyPickResult getMouseIntersection(MouseEvent e, boolean withSnap) {

        MyPickResult myResult = new MyPickResult();

        Point3d closestObjectPos = null;
        Point3d closestMarkPos = null;
        Node closestObject = null;
        Node closestMark = null;

        // 0: closest object; 1: closest mark
        pickCanvas.setShapeLocation(e);
        PickResult[] allResults = null;
        try {
            allResults = pickCanvas.pickAllSorted();
        } catch (AssertionError ex) {
        }
        if (allResults != null) {
            for (PickResult result : allResults) {
                Shape3D s = (Shape3D) result.getNode(PickResult.SHAPE3D);
                if (s != null) { //only pick a Shape3D object
                    if (closestObjectPos != null && closestMarkPos != null) {
                        break;
                    }
                    try {
                        PickIntersection intersection = result.getClosestIntersection(cameraPos);
                        if (!(s.getParent().getParent().getParent() instanceof Mark)
                                && !((s.getParent().getParent().getParent() instanceof Marker))) {
                            // markdot/markline/label -> TG -> BG -> Mark/Marker
                            try {
                                closestObjectPos = intersection.getPointCoordinatesVW();
                                closestObject = s;
                            } catch (NullPointerException ex) {
                            }
                        } else if ((s.getParent().getParent().getParent() instanceof Mark)) {
                            try {
                                closestMarkPos = intersection.getPointCoordinatesVW();
                                closestMark = s;
                            } catch (NullPointerException ex) {
                            }
                        }
                    } catch (NullPointerException ex) {
                    }
                }
            }

            Point3d resultPos = null;
            Node resultNode = null;

            if (closestMark != null) {
                resultPos = closestMarkPos; //priority of result -> mark
                resultNode = closestMark;
            } else if (closestObject != null) {
                resultPos = closestObjectPos;
                resultNode = closestObject;
            }

            if (withSnap) {  //snap resultPos to correct position

                //start trying to if cursor are inside object bounding sphere
                if ((closestObjectPos != null && bs.intersect(closestObjectPos))
                        || closestMarkPos != null && bs.intersect(closestMarkPos)) {

                    Point3d closestObjectPos2 = null;
                    Point3d closestMarkPos2 = null;
                    Node closestObject2 = null;
                    Node closestMark2 = null;

                    //initiate snap bounds if hasn't been initiated before
                    //"snappable" targets to be considered are the ones inside threshold
                    if (cursorBound == null) {
                        cursorBound = new BoundingSphere(resultPos, CURSOR_THRESHOLD);
                    }
                    if (thresholdBound == null) {
                        thresholdBound = new BoundingSphere(resultPos, SNAP_THRESHOLD);
                    }

                    //update the bounds
                    //only if the cursor is no longer inside current cursor bounds
                    if (!cursorBound.intersect(resultPos)) {
                        cursorBound.setCenter(resultPos);
                        thresholdBound.setCenter(resultPos);
                    }

                    //snap to nearest point in the list of snap targets
                    double nearestDistance = FARTHEST;
                    double distance;
                    Point3d nearestTarget = null;

                    //snap to vertex. all vertexes listed in snapList
                    if (snapList.size() > 0) {
                        for (Point3d snapTarget : snapList) {
                            if (thresholdBound.intersect(snapTarget)) {
                                distance = snapTarget.distance(resultPos);
                                if (distance < nearestDistance) {
                                    nearestDistance = distance;
                                    nearestTarget = snapTarget;
                                }

                                if (nearestTarget != null) {
                                    resultPos.set(nearestTarget); //snap!

                                    //pick the snapped node
                                    Vector3d camToSnapped = new Vector3d();
                                    camToSnapped.x = resultPos.x - cameraPos.x;
                                    camToSnapped.y = resultPos.y - cameraPos.y;
                                    camToSnapped.z = resultPos.z - cameraPos.z;
                                    pickCanvas.setShapeRay(cameraPos, camToSnapped);

                                    PickResult[] allResultsSnapped = null;
                                    try {
                                        allResultsSnapped = pickCanvas.pickAllSorted();
                                    } catch (AssertionError ex) {
                                    }
                                    if (allResultsSnapped != null) {
                                        for (PickResult result : allResultsSnapped) {
                                            Shape3D s = (Shape3D) result.getNode(PickResult.SHAPE3D);
                                            if (s != null) { //only pick a Shape3D object
                                                if (closestObjectPos2 != null && closestMarkPos2 != null) {
                                                    break;
                                                }
                                                try {
                                                    PickIntersection intersection = result.getClosestIntersection(cameraPos);
                                                    if (!(s.getParent().getParent().getParent() instanceof Mark)
                                                            && !((s.getParent().getParent().getParent() instanceof Marker))) {
                                                        // markdot/markline/label -> TG -> BG -> Mark/Marker
                                                        try {
                                                            closestObjectPos2 = intersection.getPointCoordinatesVW();
                                                            closestObject2 = s;
                                                        } catch (NullPointerException ex) {
                                                        }
                                                    } else if ((s.getParent().getParent().getParent() instanceof Mark)) {
                                                        try {
                                                            closestMarkPos2 = intersection.getPointCoordinatesVW();
                                                            closestMark2 = s;
                                                        } catch (NullPointerException ex) {
                                                        }
                                                    }
                                                } catch (NullPointerException ex) {
                                                }
                                            }
                                        }

                                        if (closestMark2 != null) {
                                            resultPos = closestMarkPos2; //priority of result -> mark
                                            resultNode = closestMark2;
                                        } else if (closestObject2 != null) {
                                            resultPos = closestObjectPos2;
                                            resultNode = closestObject2;
                                        }
                                    }

                                }
                            }
                        }

                    }

                }
            }

            myResult.setPoint(resultPos);
            myResult.setNode(resultNode);

        }
        return myResult;
    }

    private void initSnapList() {
        LinkedList<Point3d> allVertexes = canting.getAllVertex();
        snapList = allVertexes;
    }

    public boolean isAnimatingX() {
        return animatingX;
    }

    public boolean isAnimatingY() {
        return animatingY;
    }

    public boolean isAnimatingZ() {
        return animatingZ;
    }

    public void initAnimationThread() {
        animationThread = new Thread(new Runnable() {

            boolean animate = true;

            @Override
            public void run() {
                while (animate) {
                    while (animatingX || animatingY || animatingZ) {
                        try {
                            if (animatingX) {
                                pitchCamera(0.005);
                            }
                            if (animatingY) {
                                yawCamera(0.005);
                            }
                            if (animatingZ) {
                                rollCamera(0.005);
                            }
                            moveCamera();
                            Thread.sleep(5);
                        } catch (BadTransformException | InterruptedException ex) {
                        }
                    }
                }
            }
        });
    }

    public void stopAnimation() {
        animatingX = false;
        animatingY = false;
        animatingZ = false;
    }

    public void toggleXAnimation() {
        if (animatingX) {
            animatingX = false;
        } else {
            animatingX = true;
            if (animationThread == null) {
                initAnimationThread();
                animationThread.start();
            }
        }
    }

    public void toggleYAnimation() {
        if (animatingY) {
            animatingY = false;
        } else {
            animatingY = true;
            if (animationThread == null) {
                initAnimationThread();
                animationThread.start();
            }
        }
    }

    public void toggleZAnimation() {
        if (animatingZ) {
            animatingZ = false;
        } else {
            animatingZ = true;
            if (animationThread == null) {
                initAnimationThread();
                animationThread.start();
            }
        }
    }

    private MyPickResult getMouseIntersection2(MouseEvent e) {

        MyPickResult myResult = new MyPickResult();
        Point3d closestObjectPos = null;
        Node closestObject = null;

        pickCanvas2.setShapeLocation(e);
        PickResult[] allResults = null;
        try {
            allResults = pickCanvas2.pickAllSorted();
        } catch (AssertionError ex) {
        };
        if (allResults != null) {
            for (PickResult result : allResults) {
                Shape3D s = (Shape3D) result.getNode(PickResult.SHAPE3D);
                if (s != null) {
                    if (closestObjectPos != null) {
                        break;
                    }
                    try {
                        PickIntersection intersection = result.getClosestIntersection(cameraPos);
                        closestObjectPos = intersection.getPointCoordinatesVW();
                        closestObject = s;
                    } catch (NullPointerException ex) {
                    };
                }
            }

            myResult.setPoint(closestObjectPos);
            myResult.setNode(closestObject);
        }

        return myResult;
    }

    public void setChoosePivotPointMode(boolean enabled) {
        choosingPivotPoint = enabled;

        if (enabled) {
            if (pickCanvas2 == null) {
                pickCanvas2 = new PickCanvas(this, kainBG);
                pickCanvas2.setMode(PickCanvas.GEOMETRY);
            }
        } else { //remove 
            if (pivotPointAdjustor != null) {
                kainTG.removeChild(pivotPointAdjustor);
                pivotPointAdjustor = null;
            }
        }
    }

    public boolean getChoosePivotPointModeEnabled() {
        return choosingPivotPoint;
    }

    public void cleanup() {
        u.cleanup();
    }

}
