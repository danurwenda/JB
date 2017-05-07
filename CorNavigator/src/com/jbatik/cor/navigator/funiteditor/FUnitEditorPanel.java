/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.cor.navigator.funiteditor;

import com.jbatik.core.format.DotDecimalFormat;
import com.jbatik.lsystem.turtle.BaseVector;
import com.jbatik.lsystem.turtle.Surface;
import com.jbatik.lsystem.turtle.Tube;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Font3D;
import javax.media.j3d.FontExtrusion;
import javax.media.j3d.Group;
import javax.media.j3d.LineArray;
import javax.media.j3d.LineAttributes;
import javax.media.j3d.Locale;
import javax.media.j3d.OrientedShape3D;
import javax.media.j3d.PointArray;
import javax.media.j3d.PointAttributes;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.QuadArray;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Text3D;
import javax.media.j3d.Transform3D;
import javax.media. j3d.TransformGroup;
import javax.media.j3d.VirtualUniverse;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author risman
 */
public class FUnitEditorPanel extends JPanel {

    private Surface funit;

    private VirtualUniverse u;
    private Locale locale;
            
    private BranchGroup rootBG;
    private TransformGroup rootTG;
    
    private BranchGroup shapeBG;
    private TransformGroup shapeTG;
    private Shape3D F;
    private ArrayList<Point3d> topPts = new ArrayList<Point3d>();
    private ArrayList<Point3d> botPts = new ArrayList<Point3d>();
    private ArrayList<Point3d> topMidPts = new ArrayList<Point3d>();
    private ArrayList<Point3d> botMidPts = new ArrayList<Point3d>();
    
    private ArrayList<Shape3D> topHighlightDotsShapes;
    private ArrayList<Shape3D> bottomHighlightDotsShapes;
    private ArrayList<BranchGroup> topHighlightDotsBGs;
    private ArrayList<BranchGroup> bottomHighlightDotsBGs;
    
    private BranchGroup topHighlightDotsRootBG;
    private BranchGroup bottomHighlightDotsRootBG;
    private TransformGroup topHighlightDotsRootTG;
    private TransformGroup bottomHighlightDotsRootTG;
    
    private BranchGroup hoverMidHighlightDotBG;

    private BranchGroup topHoverMidHighlightDotBG;
    private TransformGroup topHoverMidHighlightDotTG;
    private BranchGroup bottomHoverMidHighlightDotBG;
    private TransformGroup bottomHoverMidHighlightDotTG;

    private Shape3D topHoverMidHighlightDot;
    private Shape3D bottomHoverMidHighlightDot;

    private Integer lastHoveredIdx = null;
    private Integer lastSelectedIdx = null;
    
    private Group axisLines;
    private BranchGroup axisBG;
    private Group axisSymbols;
    private TransformGroup xTG;
    private TransformGroup yTG;
    private TransformGroup zTG;
    

    private FUnitViewerPanel viewerTopLeft;
    private FUnitViewerPanel viewerTopRight;
    private FUnitViewerPanel viewerBottomLeft;
    private FUnitViewerPanel viewerBottomRight;

    //form panel
    private JPanel formPanel;
    private JPanel formL1;
    private JPanel formR1;
    private JLabel formLabelR1;
    private JFormattedTextField formFieldR1;
    private NumberFormat formField1Format;
    private JButton formButtonR1;
    private JToggleButton showAxisLinesButton;

    //spliters
    private JSplitPane viewerTopSplit;
    private JSplitPane viewerBottomSplit;
    private JSplitPane viewerTopBottomSplit;
    private JSplitPane viewerFormSplit;

    public FUnitEditorPanel(Surface funit) {
        super();
        this.funit = funit;
        this.setLayout(new BorderLayout());
        initComponents();
        initButtonActions();
        this.setVisible(true);
    }

    public Surface getFUnit() {
        return funit;
    }

    private void initComponents() {

        //init panels
        viewerTopLeft = new FUnitViewerPanel(this, FUnitViewerPanel.XY);
        viewerTopRight = new FUnitViewerPanel(this, FUnitViewerPanel.YZ);
        viewerBottomLeft = new FUnitViewerPanel(this, FUnitViewerPanel.ZX);
        viewerBottomRight = new FUnitViewerPanel(this, FUnitViewerPanel.PERSPECTIVE);
        
        //main rendering algorithm
        createUniverse();
        viewerTopLeft.setupViewBranch();
        viewerTopRight.setupViewBranch();
        viewerBottomLeft.setupViewBranch();
        viewerBottomRight.setupViewBranch();
        prepareShape();
        initHighlightGroups();
        prepareHighlights();
        prepareHoverHighlights();
        setupAxisBranch();
        //end of rendering algorithm
        
        viewerTopLeft.setupCameraAndMouse();
        viewerTopRight.setupCameraAndMouse();
        viewerBottomLeft.setupCameraAndMouse();
        viewerBottomRight.setupCameraAndMouse();

        //splitter between top left and top right
        viewerTopSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, viewerTopLeft, viewerTopRight);
        viewerTopSplit.setDividerSize(6);
        viewerTopSplit.setResizeWeight(0.5);

        //splitter between bottom left and bottom right
        viewerBottomSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, viewerBottomLeft, viewerBottomRight);
        viewerBottomSplit.setDividerSize(6);
        viewerBottomSplit.setResizeWeight(0.5);

        //splitter between top and bottom
        viewerTopBottomSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, viewerTopSplit, viewerBottomSplit);
        viewerTopBottomSplit.setDividerSize(6);
        viewerTopBottomSplit.setResizeWeight(0.5);
        
        //init form
        formPanel = new JPanel(new GridLayout(1,1));
        
        formL1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        formL1.setAlignmentY(JPanel.TOP_ALIGNMENT);
        
        showAxisLinesButton = new JToggleButton();
        formL1.add(showAxisLinesButton);
        
        formR1 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        
        formLabelR1 = new JLabel("New regular polygon: ");
        formR1.add(formLabelR1);

        formField1Format = NumberFormat.getIntegerInstance();
        formFieldR1 = new JFormattedTextField(formField1Format);
        formFieldR1.setColumns(3);
        formR1.add(formFieldR1);

        formButtonR1 = new JButton("Apply");
        formR1.add(formButtonR1);
        
        formPanel.add(formL1);
        formPanel.add(formR1);

        //splitter between viewer and form
        viewerFormSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, viewerTopBottomSplit, formPanel);
        viewerFormSplit.setDividerSize(6);
        viewerFormSplit.setDividerLocation(500);

        this.add(viewerFormSplit);
    }

    private void initButtonActions() {
        //action for axis lines button
        showAxisLinesButton.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    showAxisLines(true);
                    showAxisLinesButton.setText("Hide axis lines");
                } else {
                    showAxisLines(false);
                    showAxisLinesButton.setText("Show axis lines");
                }
            }
        });
        showAxisLinesButton.doClick();
        
        //action for Apply button
        formButtonR1.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                Integer sides;

                try {
                    sides = Integer.parseInt(formFieldR1.getText());
                } catch (NumberFormatException ex) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor("Please insert an integer number.", "Error", NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.ERROR_MESSAGE, new Object[]{NotifyDescriptor.OK_OPTION}, null));
                    return;
                }

                if (sides < 2) { //minimum value
                    sides = 2;
                    formFieldR1.setText("2");
                }

                Object apply = DialogDisplayer.getDefault().notify(new NotifyDescriptor("Do you want to change this FUnit\ninto a new " + sides + " sided regular polygon?", "Notice", NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.PLAIN_MESSAGE, null, null));
                if (apply == NotifyDescriptor.OK_OPTION) {
                    funit.setPoints((new Tube(sides)).getPoints());
                    refreshShape();
                    prepareHighlights();
                    updateFUnit();
                }

            }
        });
    }

    private void createUniverse() {
        
        u = new VirtualUniverse();
        locale = new Locale(u);
    }
    
    private void refreshShape() {
        shapeBG.detach();
        shapeBG = createFUnit();
        rootTG.addChild(shapeBG);
        viewerTopLeft.setupCameraAndMouse();
        viewerTopRight.setupCameraAndMouse();
        viewerBottomLeft.setupCameraAndMouse();
        viewerBottomRight.setupCameraAndMouse();
    }
    
    private void prepareShape() {
        
        rootBG = new BranchGroup();
        rootBG.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
        rootBG.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
        rootBG.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);

        rootTG = new TransformGroup();
        rootTG.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
        rootTG.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
        rootTG.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
        rootBG.addChild(rootTG);
        
        shapeBG = createFUnit();
        
        rootTG.addChild(shapeBG);

//        rootBG.compile();

        locale.addBranchGraph(rootBG);
    }
    
    private BranchGroup createFUnit() {

        BaseVector bv = new BaseVector();

        shapeBG = new BranchGroup();
        shapeBG.setCapability(BranchGroup.ALLOW_DETACH);
        shapeBG.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
        shapeBG.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
        shapeBG.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);

        shapeTG = new TransformGroup();
        shapeTG.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
        shapeTG.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
        shapeTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        shapeBG.addChild(shapeTG);

        Point3d[] botPtsArray = bv.vRound(1, funit);
        botPts.clear();
        botPts.addAll(0, Arrays.asList(botPtsArray));
        bv.Move(1);
        Point3d[] topPtsArray = bv.vRound(1, funit);
        topPts.clear();
        topPts.addAll(0, Arrays.asList(topPtsArray));
        
        botMidPts = getMidPts(botPts);
        topMidPts = getMidPts(topPts);
        
        int n = funit.pointsNum();

        Point3d[] fPoints = new Point3d[n * 4];

        for (int j = 0, k = 0; j < n * 4; j += 4, k++) {
            fPoints[j] = botPtsArray[k];
            fPoints[j+1] = botPtsArray[(k + 1) % n];
            fPoints[j+2] = topPtsArray[(k + 1) % n];
            fPoints[j+3] = topPtsArray[k];
        }

        F = new Shape3D();
        F.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
        F.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
        F.setCapability(Shape3D.ALLOW_APPEARANCE_OVERRIDE_READ);
        F.setCapability(Shape3D.ALLOW_APPEARANCE_OVERRIDE_WRITE);
        F.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
        F.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);

        //geometry
        QuadArray quadArray = new QuadArray(
                fPoints.length,
                QuadArray.COORDINATES
                        | QuadArray.COLOR_3
                        | QuadArray.NORMALS
                        | QuadArray.TEXTURE_COORDINATE_2
        );
        quadArray.setCapability(QuadArray.ALLOW_COORDINATE_READ);
        quadArray.setCapability(QuadArray.ALLOW_COORDINATE_WRITE);

        Color3f[] colors = new Color3f[fPoints.length];
        Arrays.fill(colors, new Color3f(0.7f, 0.7f, 0.7f));

        quadArray.setCoordinates(0, fPoints);
        quadArray.setColors(0, colors);

        F.setGeometry(quadArray);

        //appearance
        Appearance wireAppear = new Appearance();

        wireAppear.setCapability(Appearance.ALLOW_MATERIAL_READ);
        wireAppear.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
        wireAppear.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_READ);
        wireAppear.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_WRITE);
        wireAppear.setCapability(Appearance.ALLOW_TEXTURE_READ);
        wireAppear.setCapability(Appearance.ALLOW_TEXTURE_WRITE);

        PolygonAttributes polyAttrib = new PolygonAttributes();
        polyAttrib.setPolygonMode(PolygonAttributes.POLYGON_LINE);
        polyAttrib.setCullFace(PolygonAttributes.CULL_NONE);
        wireAppear.setPolygonAttributes(polyAttrib);
        F.setAppearance(wireAppear);

        shapeTG.addChild(F);

        return shapeBG;
    }
    
    private void updateShape() {
        int n = botPts.size();
        Point3d[] fPoints = new Point3d[n * 4];

        for (int j = 0, k = 0; j < n * 4; j += 4, k++) {
            fPoints[j] = botPts.get(k);
            fPoints[j+1] = botPts.get((k + 1) % n);
            fPoints[j+2] = topPts.get((k + 1) % n);
            fPoints[j+3] = topPts.get(k);
        }
        
        QuadArray quadArray = new QuadArray(
                fPoints.length,
                QuadArray.COORDINATES
                        | QuadArray.COLOR_3
                        | QuadArray.NORMALS
                        | QuadArray.TEXTURE_COORDINATE_2
        );
        quadArray.setCapability(QuadArray.ALLOW_COORDINATE_READ);
        quadArray.setCapability(QuadArray.ALLOW_COORDINATE_WRITE);

        Color3f[] colors = new Color3f[fPoints.length];
        Arrays.fill(colors, new Color3f(0.7f, 0.7f, 0.7f));

        quadArray.setCoordinates(0, fPoints);
        quadArray.setColors(0, colors);

        F.setGeometry(quadArray);
    }
    
    public void updateFUnit() {
        funit.setPoints(toSurfacePoints(botPts));
        repositionAxisSymbols();
        viewerTopLeft.cameraDefault();
        viewerTopRight.cameraDefault();
        viewerBottomLeft.cameraDefault();
        viewerBottomRight.cameraDefault();
    }
    
    private Point3d[] toSurfacePoints(ArrayList<Point3d> bottomPoints) {
        Point3d[] newPoints = new Point3d[bottomPoints.size()];
        for (int i=0; i<newPoints.length; i++) {
            newPoints[i] = new Point3d();
            newPoints[i].x = -bottomPoints.get(i).x;
            newPoints[i].y = bottomPoints.get(i).z;
            newPoints[i].z = bottomPoints.get(i).y;
        }
        return newPoints;
    }
    
    private void initHighlightGroups() {
        topHighlightDotsShapes = new ArrayList<Shape3D>();
        bottomHighlightDotsShapes = new ArrayList<Shape3D>();
        topHighlightDotsBGs = new ArrayList<BranchGroup>();
        bottomHighlightDotsBGs = new ArrayList<BranchGroup>();
        topHighlightDotsRootBG = new BranchGroup();
        topHighlightDotsRootBG.setCapability(BranchGroup.ALLOW_DETACH);
        topHighlightDotsRootTG = new TransformGroup();
        bottomHighlightDotsRootBG = new BranchGroup();
        bottomHighlightDotsRootBG.setCapability(BranchGroup.ALLOW_DETACH);
        bottomHighlightDotsRootTG = new TransformGroup();
    
        topHighlightDotsRootTG.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
        topHighlightDotsRootTG.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
        topHighlightDotsRootTG.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
        topHighlightDotsRootBG.addChild(topHighlightDotsRootTG);
        bottomHighlightDotsRootTG.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
        bottomHighlightDotsRootTG.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
        bottomHighlightDotsRootTG.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
        bottomHighlightDotsRootBG.addChild(bottomHighlightDotsRootTG);
        
        shapeTG.addChild(topHighlightDotsRootBG);
        shapeTG.addChild(bottomHighlightDotsRootBG);
    }
    
    private void prepareHighlights() {
        topHighlightDotsRootBG.detach();
        bottomHighlightDotsRootBG.detach();
        
        topHighlightDotsRootTG.removeAllChildren();
        bottomHighlightDotsRootTG.removeAllChildren();
        topHighlightDotsShapes.clear();
        bottomHighlightDotsShapes.clear();
        topHighlightDotsBGs.clear();
        bottomHighlightDotsBGs.clear();
        lastHoveredIdx = null;
        lastSelectedIdx = null;
        
        for (Point3d topPt : topPts) {
            BranchGroup brg = new BranchGroup();
            brg.setCapability(BranchGroup.ALLOW_DETACH);
            TransformGroup trg = new TransformGroup();
            Appearance dotAppr = new Appearance();
            dotAppr.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_READ);
            dotAppr.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_WRITE);
            RenderingAttributes dotRAttr = new RenderingAttributes();
            dotRAttr.setCapability(RenderingAttributes.ALLOW_VISIBLE_READ);
            dotRAttr.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
            dotAppr.setRenderingAttributes(dotRAttr);
            ColoringAttributes topDotCAppr = new ColoringAttributes(
                    new Color3f(0.33f, 0.33f, 0.95f),
                    ColoringAttributes.SHADE_FLAT);
            topDotCAppr.setCapability(ColoringAttributes.ALLOW_COLOR_READ);
            topDotCAppr.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
            dotAppr.setColoringAttributes(topDotCAppr);
            dotAppr.setPointAttributes(new PointAttributes(6.0f, true));
            Shape3D dotShape = new Shape3D();
            PointArray dot = new PointArray(1, PointArray.COORDINATES);
            dot.setCapability(PointArray.ALLOW_COORDINATE_READ);
            dot.setCapability(PointArray.ALLOW_COORDINATE_WRITE);
            dot.setCoordinate(0, topPt);
            dotShape.setGeometry(dot);
            dotShape.setAppearance(dotAppr);
            trg.addChild(dotShape);
            brg.addChild(trg);
            topHighlightDotsRootTG.addChild(brg);
            topHighlightDotsShapes.add(dotShape);
            topHighlightDotsBGs.add(brg);
        }
        
        for (Point3d botPt : botPts) {
            BranchGroup brg = new BranchGroup();
            brg.setCapability(BranchGroup.ALLOW_DETACH);
            TransformGroup trg = new TransformGroup();
            Appearance dotAppr = new Appearance();
            dotAppr.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_READ);
            dotAppr.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_WRITE);
            RenderingAttributes dotRAttr = new RenderingAttributes();
            dotRAttr.setCapability(RenderingAttributes.ALLOW_VISIBLE_READ);
            dotRAttr.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
            dotAppr.setRenderingAttributes(dotRAttr);
            ColoringAttributes bottomDotAppr = new ColoringAttributes(
                    new Color3f(0.33f, 0.33f, 0.95f),
                    ColoringAttributes.SHADE_FLAT);
            bottomDotAppr.setCapability(ColoringAttributes.ALLOW_COLOR_READ);
            bottomDotAppr.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
            dotAppr.setColoringAttributes(bottomDotAppr);
            dotAppr.setPointAttributes(new PointAttributes(6.0f, true));
            Shape3D dotShape = new Shape3D();
            PointArray dot = new PointArray(1, PointArray.COORDINATES);
            dot.setCapability(PointArray.ALLOW_COORDINATE_READ);
            dot.setCapability(PointArray.ALLOW_COORDINATE_WRITE);
            dot.setCoordinate(0, botPt);
            dotShape.setGeometry(dot);
            dotShape.setAppearance(dotAppr);
            trg.addChild(dotShape);
            brg.addChild(trg);
            bottomHighlightDotsRootTG.addChild(brg);
            bottomHighlightDotsShapes.add(dotShape);
            bottomHighlightDotsBGs.add(brg);
        }
        
        shapeTG.addChild(topHighlightDotsRootBG);
        shapeTG.addChild(bottomHighlightDotsRootBG);
        
    }
    
    private void prepareHoverHighlights() {
        Appearance dotAppr = new Appearance();
        RenderingAttributes topDotRAttr = new RenderingAttributes();
        topDotRAttr.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
        dotAppr.setRenderingAttributes(topDotRAttr);
        ColoringAttributes topDotCAppr = new ColoringAttributes(
                new Color3f(0.4f, 0.7f, 0.95f),
                ColoringAttributes.SHADE_FLAT);
        topDotCAppr.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
        dotAppr.setColoringAttributes(topDotCAppr);
        dotAppr.setPointAttributes(new PointAttributes(6.0f, true));
        
        hoverMidHighlightDotBG = new BranchGroup();
        hoverMidHighlightDotBG.setCapability(BranchGroup.ALLOW_DETACH);

        topHoverMidHighlightDotBG = new BranchGroup();
        topHoverMidHighlightDotTG = new TransformGroup();
        topHoverMidHighlightDotBG.addChild(topHoverMidHighlightDotTG);
        bottomHoverMidHighlightDotBG = new BranchGroup();
        bottomHoverMidHighlightDotTG = new TransformGroup();
        bottomHoverMidHighlightDotBG.addChild(bottomHoverMidHighlightDotTG);

        hoverMidHighlightDotBG.addChild(topHoverMidHighlightDotBG);
        hoverMidHighlightDotBG.addChild(bottomHoverMidHighlightDotBG);
        
        topHoverMidHighlightDot = new Shape3D();
        
        PointArray topMidDot = new PointArray(1, PointArray.COORDINATES);
        topMidDot.setCapability(PointArray.ALLOW_COORDINATE_READ);
        topMidDot.setCapability(PointArray.ALLOW_COORDINATE_WRITE);
        topHoverMidHighlightDot.setGeometry(topMidDot);
        topHoverMidHighlightDot.setAppearance(dotAppr);
        topHoverMidHighlightDotTG.addChild(topHoverMidHighlightDot);

        bottomHoverMidHighlightDot = new Shape3D();
        
        PointArray bottomMidDot = new PointArray(1, PointArray.COORDINATES);
        bottomMidDot.setCapability(PointArray.ALLOW_COORDINATE_READ);
        bottomMidDot.setCapability(PointArray.ALLOW_COORDINATE_WRITE);
        bottomHoverMidHighlightDot.setGeometry(bottomMidDot);
        bottomHoverMidHighlightDot.setAppearance(dotAppr);
        bottomHoverMidHighlightDotTG.addChild(bottomHoverMidHighlightDot);
    }
    
    public void insertPoint(int idx, int viewMode) {
        Point3d newBotPoint = new Point3d();
        Point3d newTopPoint = new Point3d();
        if (viewMode == FUnitViewerPanel.XY) {
            newBotPoint.x = botMidPts.get(idx).x + 0.1f;
            newBotPoint.y = botMidPts.get(idx).y + 0.1f;
            newBotPoint.z = botMidPts.get(idx).z;
            
            newTopPoint.x = topMidPts.get(idx).x + 0.1f;
            newTopPoint.y = topMidPts.get(idx).y + 0.1f;
            newTopPoint.z = topMidPts.get(idx).z;
            
        } else if (viewMode == FUnitViewerPanel.YZ) {
            newBotPoint.x = botMidPts.get(idx).x;
            newBotPoint.y = botMidPts.get(idx).y + 0.1f;
            newBotPoint.z = botMidPts.get(idx).z + 0.1f;
            
            newTopPoint.x = topMidPts.get(idx).x;
            newTopPoint.y = topMidPts.get(idx).y + 0.1f;
            newTopPoint.z = topMidPts.get(idx).z + 0.1f;
            
        } else if (viewMode == FUnitViewerPanel.ZX) {
            newBotPoint.x = botMidPts.get(idx).x + 0.1f;
            newBotPoint.y = botMidPts.get(idx).y;
            newBotPoint.z = botMidPts.get(idx).z + 0.1f;
            
            newTopPoint.x = topMidPts.get(idx).x + 0.1f;
            newTopPoint.y = topMidPts.get(idx).y;
            newTopPoint.z = topMidPts.get(idx).z + 0.1f;
        }
        
        botPts.add(idx+1, newBotPoint);
        topPts.add(idx+1, newTopPoint);
        botMidPts = getMidPts(botPts);
        topMidPts = getMidPts(topPts);
        
        BranchGroup topBrg = new BranchGroup();
        topBrg.setCapability(BranchGroup.ALLOW_DETACH);
        TransformGroup topTrg = new TransformGroup();
        Appearance topAPpr = new Appearance();
        topAPpr.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_READ);
        topAPpr.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_WRITE);
        RenderingAttributes topDotRAttr = new RenderingAttributes();
        topDotRAttr.setCapability(RenderingAttributes.ALLOW_VISIBLE_READ);
        topDotRAttr.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
        topAPpr.setRenderingAttributes(topDotRAttr);
        ColoringAttributes topDotCAppr = new ColoringAttributes(
                new Color3f(0.33f, 0.33f, 0.95f),
                ColoringAttributes.SHADE_FLAT);
        topDotCAppr.setCapability(ColoringAttributes.ALLOW_COLOR_READ);
        topDotCAppr.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
        topAPpr.setColoringAttributes(topDotCAppr);
        topAPpr.setPointAttributes(new PointAttributes(6.0f, true));
        Shape3D topDotShape = new Shape3D();
        PointArray topDot = new PointArray(1, PointArray.COORDINATES);
        topDot.setCapability(PointArray.ALLOW_COORDINATE_READ);
        topDot.setCapability(PointArray.ALLOW_COORDINATE_WRITE);
        topDot.setCoordinate(0, newTopPoint);
        topDotShape.setGeometry(topDot);
        topDotShape.setAppearance(topAPpr);
        topTrg.addChild(topDotShape);
        topBrg.addChild(topTrg);
        topHighlightDotsRootTG.addChild(topBrg);
        topHighlightDotsShapes.add(idx+1, topDotShape);
        topHighlightDotsBGs.add(idx+1, topBrg);
        
        BranchGroup bottomBrg = new BranchGroup();
        bottomBrg.setCapability(BranchGroup.ALLOW_DETACH);
        TransformGroup bottomTrg = new TransformGroup();
        Appearance bottomAPpr = new Appearance();
        bottomAPpr.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_READ);
        bottomAPpr.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_WRITE);
        RenderingAttributes bottomDotRAttr = new RenderingAttributes();
        bottomDotRAttr.setCapability(RenderingAttributes.ALLOW_VISIBLE_READ);
        bottomDotRAttr.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
        bottomAPpr.setRenderingAttributes(bottomDotRAttr);
        ColoringAttributes bottomDotCAppr = new ColoringAttributes(
                new Color3f(0.33f, 0.33f, 0.95f),
                ColoringAttributes.SHADE_FLAT);
        bottomDotCAppr.setCapability(ColoringAttributes.ALLOW_COLOR_READ);
        bottomDotCAppr.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
        bottomAPpr.setColoringAttributes(bottomDotCAppr);
        bottomAPpr.setPointAttributes(new PointAttributes(6.0f, true));
        Shape3D bottomDotShape = new Shape3D();
        PointArray bottomDot = new PointArray(1, PointArray.COORDINATES);
        bottomDot.setCapability(PointArray.ALLOW_COORDINATE_READ);
        bottomDot.setCapability(PointArray.ALLOW_COORDINATE_WRITE);
        bottomDot.setCoordinate(0, newTopPoint);
        bottomDotShape.setGeometry(bottomDot);
        bottomDotShape.setAppearance(bottomAPpr);
        bottomTrg.addChild(bottomDotShape);
        bottomBrg.addChild(bottomTrg);
        bottomHighlightDotsRootTG.addChild(bottomBrg);
        bottomHighlightDotsShapes.add(idx+1, bottomDotShape);
        bottomHighlightDotsBGs.add(idx+1, bottomBrg);
        
        updateShape();
        updateFUnit();
    }
    
    public void removePoint(int idx) {
        bottomHighlightDotsRootTG.removeChild(bottomHighlightDotsBGs.get(idx));
        bottomHighlightDotsBGs.remove(idx);
        bottomHighlightDotsShapes.remove(idx);
        botPts.remove(idx);
        topHighlightDotsRootTG.removeChild(topHighlightDotsBGs.get(idx));
        topHighlightDotsBGs.remove(idx);
        topHighlightDotsShapes.remove(idx);
        topPts.remove(idx);
        botMidPts = getMidPts(botPts);
        topMidPts = getMidPts(topPts);
        
        
        updateShape();
        updateFUnit();
    }
    
    public void popupEdit(int idx) {
        double prevX = botPts.get(idx).x;
        double prevY = botPts.get(idx).y;
        double prevZ = botPts.get(idx).z;
        DecimalFormat df = new DecimalFormat("#.###",DotDecimalFormat.getSymbols());
        df.setRoundingMode(RoundingMode.DOWN);
        JPanel coordEditorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        JLabel xLabel = new JLabel("X: ");
        JTextField xField = new JTextField(df.format(prevX));
        xField.setColumns(10);
        JLabel yLabel = new JLabel("Y: ");
        JTextField yField = new JTextField(df.format(prevY));
        yField.setColumns(10);
        JLabel zLabel = new JLabel("Z: ");
        JTextField zField = new JTextField(df.format(prevZ));
        zField.setColumns(10);
        coordEditorPanel.add(xLabel);
        coordEditorPanel.add(xField);
        coordEditorPanel.add(yLabel);
        coordEditorPanel.add(yField);
        coordEditorPanel.add(zLabel);
        coordEditorPanel.add(zField);
        DialogDescriptor dd = new DialogDescriptor(coordEditorPanel, "Edit Point");
        
        dd.setModal(true);
        Object result = DialogDisplayer.getDefault().notify(dd);
        if (result == NotifyDescriptor.OK_OPTION) {
            try {
                double newX = Double.parseDouble(xField.getText());
                double newY = Double.parseDouble(yField.getText());
                double newZ = Double.parseDouble(zField.getText());
                Point3d diff = new Point3d(newX-prevX, newY-prevY, newZ-prevZ);
                updatePoint(idx, diff);
                updateFUnit();
            } catch (NumberFormatException | NullPointerException excp) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor("Please insert a valid coordinate.", "Error", NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.ERROR_MESSAGE, new Object[]{NotifyDescriptor.OK_OPTION}, null));
            }
        }
    }
    
    public void popupRotate(int viewMode) {
        JPanel rotatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        String axistext = "";
        Vector3d rotateVector = new Vector3d();
        if (viewMode == FUnitViewerPanel.XY) {
            axistext = "Z";
            rotateVector.set(0,0,1);
        } else if (viewMode == FUnitViewerPanel.YZ) {
            axistext = "X";
            rotateVector.set(1,0,0);
        } else if (viewMode == FUnitViewerPanel.ZX) {
            axistext = "Y";
            rotateVector.set(0,1,0);
        }
        JLabel rotateFormLabel = new JLabel("rotate around " + axistext + " axis : ");
        JTextField rotateFormField = new JTextField("");
        rotateFormField.setColumns(10);
        JLabel degreesLabel = new JLabel("degrees");
        rotatePanel.add(rotateFormLabel);
        rotatePanel.add(rotateFormField);
        rotatePanel.add(degreesLabel);
        DialogDescriptor dd = new DialogDescriptor(rotatePanel, "Rotate FUnit");
        
        dd.setModal(true);
        Object result = DialogDisplayer.getDefault().notify(dd);
        if (result == NotifyDescriptor.OK_OPTION) {
            try {
                Transform3D rotate = new Transform3D();
                double rad = -Math.toRadians(Double.parseDouble(rotateFormField.getText()));
                rotate.set(new AxisAngle4d(rotateVector, rad));
                for (Point3d pt : botPts) {
                    rotate.transform(pt);
                }
                funit.setPoints(toSurfacePoints(botPts));
                refreshShape();
                prepareHighlights();
                updateFUnit();
            } catch (NumberFormatException | NullPointerException excp) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor("Please insert a valid value.", "Error", NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.ERROR_MESSAGE, new Object[]{NotifyDescriptor.OK_OPTION}, null));
            }
        }
    }
    
    public void popupTranslate(int viewMode) {
        Point3d midPoint = funit.getMidPoint();
        double prevX = -midPoint.x;
        double prevY = midPoint.z;
        double prevZ = midPoint.y;
        DecimalFormat df = new DecimalFormat("#.###",DotDecimalFormat.getSymbols());
        df.setRoundingMode(RoundingMode.DOWN);
        JPanel coordEditorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        JLabel xLabel = new JLabel("X: ");
        JTextField xField = new JTextField(df.format(prevX));
        xField.setColumns(10);
        JLabel yLabel = new JLabel("Y: ");
        JTextField yField = new JTextField(df.format(prevY));
        yField.setColumns(10);
        JLabel zLabel = new JLabel("Z: ");
        JTextField zField = new JTextField(df.format(prevZ));
        zField.setColumns(10);
        coordEditorPanel.add(xLabel);
        coordEditorPanel.add(xField);
        coordEditorPanel.add(yLabel);
        coordEditorPanel.add(yField);
        coordEditorPanel.add(zLabel);
        coordEditorPanel.add(zField);
        if (viewMode == FUnitViewerPanel.XY) {
            zLabel.setEnabled(false);
            zField.setEnabled(false);
        } else if (viewMode == FUnitViewerPanel.YZ) {
            xLabel.setEnabled(false);
            xField.setEnabled(false);
        } else if (viewMode == FUnitViewerPanel.ZX) {
            yLabel.setEnabled(false);
            yField.setEnabled(false);
        }
        DialogDescriptor dd = new DialogDescriptor(coordEditorPanel, "Move surface's middle point");
        
        dd.setModal(true);
        Object result = DialogDisplayer.getDefault().notify(dd);
        if (result == NotifyDescriptor.OK_OPTION) {
            try {
                double newX = Double.parseDouble(xField.getText());
                double newY = Double.parseDouble(yField.getText());
                double newZ = Double.parseDouble(zField.getText());
                Point3d diff = new Point3d(newX-prevX, newY-prevY, newZ-prevZ);
                for (int i=0; i<botPts.size(); i++) {
                    updatePoint(i, diff);
                }
                updateFUnit();
            } catch (NumberFormatException | NullPointerException excp) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor("Please insert a valid coordinate.", "Error", NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.ERROR_MESSAGE, new Object[]{NotifyDescriptor.OK_OPTION}, null));
            }
        }
    }
    
    public void updatePoint(int idx, Point3d diff) {
        botPts.get(idx).x = botPts.get(idx).x + diff.x;
        botPts.get(idx).y = botPts.get(idx).y + diff.y;
        botPts.get(idx).z = botPts.get(idx).z + diff.z;
        ((PointArray) bottomHighlightDotsShapes.get(idx).getGeometry()).setCoordinate(0, botPts.get(idx));
        
        topPts.get(idx).x = topPts.get(idx).x + diff.x;
        topPts.get(idx).y = topPts.get(idx).y + diff.y;
        topPts.get(idx).z = topPts.get(idx).z + diff.z;
        ((PointArray) topHighlightDotsShapes.get(idx).getGeometry()).setCoordinate(0, topPts.get(idx));
        
        botMidPts = getMidPts(botPts);
        topMidPts = getMidPts(topPts);
        
        updateShape();
    }
    
    public void setupAxisBranch() {
        
        axisBG = new BranchGroup();
        axisBG.setCapability(BranchGroup.ALLOW_DETACH);
        axisLines = createAxisLines();
        axisBG.addChild(axisLines);
        axisSymbols = createAxisSymbols();
        axisBG.addChild(axisSymbols);
        
    }
    
    private Group createAxisLines() {
        Group lineGroup = new Group();

        Color3f paleRed = new Color3f(0.5f, 0.3f, 0.3f);
        Color3f paleGreen = new Color3f(0.3f, 0.5f, 0.3f);
        Color3f paleBlue = new Color3f(0.3f, 0.3f, 0.5f);

        Point3f zero = new Point3f(0, 0, 0);
        lineGroup.addChild(createLine(zero, new Point3f(10000, 0, 0), paleRed, false, 1.5f)); //x positive
        lineGroup.addChild(createLine(zero, new Point3f(-10000, 0, 0), paleRed, true, 1.5f)); //x negative
        lineGroup.addChild(createLine(zero, new Point3f(0, 10000, 0), paleGreen, false, 1.5f)); //y positive
        lineGroup.addChild(createLine(zero, new Point3f(0, -10000, 0), paleGreen, true, 1.5f)); //y negative
        lineGroup.addChild(createLine(zero, new Point3f(0, 0, 10000), paleBlue, false, 1.5f)); //z positive
        lineGroup.addChild(createLine(zero, new Point3f(0, 0, -10000), paleBlue, true, 1.5f)); //z negative
        
        return lineGroup;
    }
    
    private Group createAxisSymbols() {
        
        Group symbols = new Group();
        
        BranchGroup xBG = new BranchGroup();
        xTG = new TransformGroup();
        xTG.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        xTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        xBG.addChild(xTG);
        BranchGroup yBG = new BranchGroup();
        yTG = new TransformGroup();
        yTG.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        yTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        yBG.addChild(yTG);
        BranchGroup zBG = new BranchGroup();
        zTG = new TransformGroup();
        zTG.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        zTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        zBG.addChild(zTG);

        Font font = new Font("Arial", Font.BOLD, 2);
        Font3D font3d = new Font3D(font, new FontExtrusion());

        Text3D labelX = new Text3D();
        labelX.setCapability(Text3D.ALLOW_POSITION_READ);
        labelX.setCapability(Text3D.ALLOW_POSITION_WRITE);
        labelX.setFont3D(font3d);
        labelX.setAlignment(Text3D.ALIGN_CENTER);
        labelX.setString("x");

        Appearance textXAppr = new Appearance();
        ColoringAttributes textXCAppr = new ColoringAttributes(
                (new Color3f(Color.RED)), ColoringAttributes.SHADE_FLAT);
        textXCAppr.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
        textXAppr.setColoringAttributes(textXCAppr);

        OrientedShape3D shapeX = new OrientedShape3D();
        shapeX.setGeometry(labelX);
        shapeX.setAppearance(textXAppr);
        shapeX.setAlignmentMode(OrientedShape3D.ROTATE_ABOUT_POINT);
        shapeX.setConstantScaleEnable(true);
        shapeX.setScale(0.8);
        
        Text3D labelY = new Text3D();
        labelY.setCapability(Text3D.ALLOW_POSITION_READ);
        labelY.setCapability(Text3D.ALLOW_POSITION_WRITE);
        labelY.setFont3D(font3d);
        labelY.setAlignment(Text3D.ALIGN_CENTER);
        labelY.setString("y");
        
        Appearance textYAppr = new Appearance();
        ColoringAttributes textYCAppr = new ColoringAttributes(
                (new Color3f(Color.GREEN)), ColoringAttributes.SHADE_FLAT);
        textYCAppr.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
        textYAppr.setColoringAttributes(textYCAppr);

        OrientedShape3D shapeY = new OrientedShape3D();
        shapeY.setGeometry(labelY);
        shapeY.setAppearance(textYAppr);
        shapeY.setAlignmentMode(OrientedShape3D.ROTATE_ABOUT_POINT);
        shapeY.setConstantScaleEnable(true);
        shapeY.setScale(0.8);
        
        Text3D labelZ = new Text3D();
        labelZ.setCapability(Text3D.ALLOW_POSITION_READ);
        labelZ.setCapability(Text3D.ALLOW_POSITION_WRITE);
        labelZ.setFont3D(font3d);
        labelZ.setAlignment(Text3D.ALIGN_CENTER);
        labelZ.setString("z");
        
        Appearance textZAppr = new Appearance();
        ColoringAttributes textZCAppr = new ColoringAttributes(
                (new Color3f(Color.CYAN)), ColoringAttributes.SHADE_FLAT);
        textZCAppr.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
        textZAppr.setColoringAttributes(textZCAppr);

        OrientedShape3D shapeZ = new OrientedShape3D();
        shapeZ.setGeometry(labelZ);
        shapeZ.setAppearance(textZAppr);
        shapeZ.setAlignmentMode(OrientedShape3D.ROTATE_ABOUT_POINT);
        shapeZ.setConstantScaleEnable(true);
        shapeZ.setScale(0.8);
        
        xTG.addChild(shapeX);
        yTG.addChild(shapeY);
        zTG.addChild(shapeZ);
        
        symbols.addChild(xBG);
        symbols.addChild(yBG);
        symbols.addChild(zBG);
        
        repositionAxisSymbols();
        
        return symbols;
    }
    
    private void repositionAxisSymbols() {
        
        double maxX = 0;
        double maxY = 0;
        double maxZ = 0;
        
        ArrayList<Point3d> points = new ArrayList<Point3d>();
        points.addAll(topPts);
        points.addAll(botPts);
        
        for (Point3d point : points) {
            if (point.x > maxX) maxX = point.x;
            if (point.y > maxY) maxY = point.y;
            if (point.z > maxZ) maxZ = point.z;
        }
        
        Transform3D xPos = new Transform3D();
        xPos.setTranslation(new Vector3d(maxX + 0.15, 0, -0.8));
        xTG.setTransform(xPos);
        Transform3D yPos = new Transform3D();
        yPos.setTranslation(new Vector3d(0, maxY + 0.15, -0.8));
        yTG.setTransform(yPos);
        Transform3D zPos = new Transform3D();
        zPos.setTranslation(new Vector3d(0, 0, maxZ - 0.65));
        zTG.setTransform(zPos);
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
    
    public void showAxisLines(boolean active) {
        if (active) {
            locale.addBranchGraph(axisBG);
        } else {
            locale.removeBranchGraph(axisBG);
        }
    }
    
    public BranchGroup getRootBG() {
        return rootBG;
    }
    
    public BranchGroup getShapeBG() {
        return shapeBG;
    }
    
    public Locale getULocale() {
        return locale;
    }
    
        
    public void setHoverHighlightOn(int hoverHighlightType, int idx) {
        lastHoveredIdx = idx;
        if (hoverHighlightType == FUnitViewerPanel.POINT) {
            topHighlightDotsShapes.get(idx).getAppearance().getColoringAttributes().setColor(0.4f, 0.7f, 0.95f);
            bottomHighlightDotsShapes.get(idx).getAppearance().getColoringAttributes().setColor(0.4f, 0.7f, 0.95f);
        } else {

            ((PointArray)topHoverMidHighlightDot.getGeometry()).setCoordinate(0, topMidPts.get(idx));
            ((PointArray)bottomHoverMidHighlightDot.getGeometry()).setCoordinate(0, botMidPts.get(idx));

            if (hoverMidHighlightDotBG.getParent() == null) {
                rootBG.addChild(hoverMidHighlightDotBG);
            }
        }
    }

    public void setHoverHighlightOff() {
        if (lastHoveredIdx != null) {
            topHighlightDotsShapes.get(lastHoveredIdx).getAppearance().getColoringAttributes().setColor(0.33f, 0.33f, 0.95f);
            bottomHighlightDotsShapes.get(lastHoveredIdx).getAppearance().getColoringAttributes().setColor(0.33f, 0.33f, 0.95f);
        }
        
        if (hoverMidHighlightDotBG.getParent() != null) {
            rootBG.removeChild(hoverMidHighlightDotBG);
        }
    }
    
    public void setSelectHighlightOn(int idx) {
        lastSelectedIdx = idx;
        topHighlightDotsShapes.get(idx).getAppearance().getColoringAttributes().setColor(0.67f, 0.95f, 0.67f);
        bottomHighlightDotsShapes.get(idx).getAppearance().getColoringAttributes().setColor(0.67f, 0.95f, 0.67f);
    }

    public void setSelectHighlightOff() {
        if (lastSelectedIdx != null) {
            topHighlightDotsShapes.get(lastSelectedIdx).getAppearance().getColoringAttributes().setColor(0.33f, 0.33f, 0.95f);
            bottomHighlightDotsShapes.get(lastSelectedIdx).getAppearance().getColoringAttributes().setColor(0.33f, 0.33f, 0.95f);
        }
    }
    
    private ArrayList<Point3d> getMidPts(ArrayList<Point3d> pts) {
        ArrayList<Point3d> midPts = new ArrayList<Point3d>();
        for (int i = 0; i<pts.size(); i++) {
            int j = (i+1) % pts.size();
            double x = pts.get(i).x + ((pts.get(j).x - pts.get(i).x)/2);
            double y = pts.get(i).y + ((pts.get(j).y - pts.get(i).y)/2);
            double z = pts.get(i).z + ((pts.get(j).z - pts.get(i).z)/2);
            midPts.add(new Point3d(x,y,z));
        }
        
        return midPts;
    }
    
    public ArrayList<Point3d> getTopPoints() {
        return topPts;
    }
    
    public ArrayList<Point3d> getBotPoints() {
        return botPts;
    }
    
    public ArrayList<Point3d> getTopMidPoints() {
        return topMidPts;
    }
    
    public ArrayList<Point3d> getBotMidPoints() {
        return botMidPts;
    }
}