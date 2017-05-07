/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jbatik.modules.corak.component;

import com.jbatik.core.format.DotDecimalFormat;
import java.awt.Font;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedList;
import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Font3D;
import javax.media.j3d.FontExtrusion;
import javax.media.j3d.LineArray;
import javax.media.j3d.LineAttributes;
import javax.media.j3d.Node;
import javax.media.j3d.OrientedShape3D;
import javax.media.j3d.PointArray;
import javax.media.j3d.PointAttributes;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Text3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/**
 *
 * @author RAPID01
 */
public class Measurer extends BranchGroup{
    
    private LinkedList<Mark> marks;
    private Marker marker;
    
    public static final Color3f POINT_COLOR = new Color3f(1.0f, 0.0f, 0.0f);
    public static final Color3f LINE_COLOR = new Color3f(1.0f, 1.0f, 1.0f);
    static final Color3f LABEL_COLOR = new Color3f(1.0f, 1.0f, 1.0f);
    public static final Color3f HIGHLIGHT_COLOR = new Color3f(0.5f, 0.8f, 1.0f);
    public static final Color3f SELECTED_COLOR = new Color3f(0.2f, 0.2f, 1.0f);
    
    public Measurer(Point3d pos) {
        super();
        this.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
        this.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
        this.setCapability(BranchGroup.ALLOW_DETACH);
        marks = new LinkedList<Mark>();
        marker = new Marker(pos);
        this.addChild(marker);
    }
    
    public Mark createMark() {
        Mark mark = new Mark();
        marks.add(mark);
        this.addChild(mark);
        return mark;
    }
    
    public Marker getMarker() {
        if (marker.getParent() == null) {
            this.addChild(marker);
        }
        return marker;
    }
    
    public LinkedList<Mark> getMarks() {
        return marks;
    }
    
    public void clearAllMarks() {
        marks.clear();
        marker.removeMarkerLine();
        this.removeAllChildren();
    }

    public void clearSelected(Node node) {
        Mark selected = (Mark) node.getParent().getParent().getParent();
        //markDot/markLine/label -> TG -> BG -> Mark

        marks.remove(selected);
        this.removeChild(selected);
    }
    
    public class Mark extends BranchGroup {
        private Shape3D firstPoint;
        private Point3d p1;
        
        private Shape3D secondPoint;
        private Point3d p2;
        
        private OrientedShape3D labelShape3D;
        private Text3D labelText3D;
        
        private Shape3D markLine;
        
        private BranchGroup firstPointBG;
        private TransformGroup firstPointTG;
        
        private BranchGroup secondPointBG;
        private TransformGroup secondPointTG;
        
        private BranchGroup labelBG;
        private TransformGroup labelTG;
        
        private BranchGroup markLineBG;
        private TransformGroup markLineTG;
    
        private boolean firstPointPlaced = false;
        
        public Mark() {
            super();
            this.setCapability(BranchGroup.ALLOW_DETACH);
            this.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
            this.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
        }
        
        public void createFirstPoint(Point3d pos) {
            
            firstPointBG = new BranchGroup();
            firstPointBG.setCapability(BranchGroup.ALLOW_DETACH);
            firstPointTG = new TransformGroup();
            
            p1 = pos;
            firstPoint = createPoint(pos, firstPointTG);
            
            firstPointTG.addChild(firstPoint);
            firstPointBG.addChild(firstPointTG);
            
            this.addChild(firstPointBG);

            firstPointPlaced = true;
        }
        
        public void createSecondPoint(Point3d pos) {
            
            secondPointBG = new BranchGroup();
            secondPointBG.setCapability(BranchGroup.ALLOW_DETACH);
            secondPointTG = new TransformGroup();
            
            p2 = pos;
            secondPoint = createPoint(pos, secondPointTG);
            
            secondPointTG.addChild(secondPoint);
            secondPointBG.addChild(secondPointTG);
            
            this.addChild(secondPointBG);
            
            firstPointPlaced = false;
        }
        
        public void createText() {
            
            labelBG = new BranchGroup();
            labelBG.setCapability(BranchGroup.ALLOW_DETACH);
            labelTG = new TransformGroup();
            
            Font font = new Font("Courier", Font.BOLD, 2);
            Font3D font3d = new Font3D(font, new FontExtrusion());
            
            labelText3D = new Text3D();
            labelText3D.setCapability(Text3D.ALLOW_POSITION_READ);
            labelText3D.setCapability(Text3D.ALLOW_POSITION_WRITE);
            labelText3D.setFont3D(font3d);
            labelText3D.setAlignment(Text3D.ALIGN_CENTER);
            
            NumberFormat formatter = new DecimalFormat("#0.00",DotDecimalFormat.getSymbols());
            Double length = p1.distance(p2);
            labelText3D.setString(formatter.format(length));
            
            Appearance textAppr = new Appearance();
            RenderingAttributes textRAppr = new RenderingAttributes();
            textRAppr.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
            textAppr.setRenderingAttributes(textRAppr);
            ColoringAttributes textCAppr = new ColoringAttributes(
                    LABEL_COLOR, ColoringAttributes.SHADE_FLAT);
            textCAppr.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
            textAppr.setColoringAttributes(textCAppr);
            
            labelShape3D = new OrientedShape3D();
            labelShape3D.setGeometry(labelText3D);
            labelShape3D.setAppearance(textAppr);
            labelShape3D.setAlignmentMode(OrientedShape3D.ROTATE_ABOUT_POINT);
            labelShape3D.setConstantScaleEnable(true);
            labelShape3D.setScale(0.28);
            
            //get middle of our 2 points
            Point3f mid = new Point3f(getMarkCenter()); 
            mid.z -= 0.8;
            //0.8 because by default it's not precisely put the text in the middle
            
            Transform3D labelPlace = new Transform3D();
            labelPlace.setTranslation(new Vector3f(mid));
            labelTG.setTransform(labelPlace);
            
            labelTG.addChild(labelShape3D);
            labelBG.addChild(labelTG);
            
            this.addChild(labelBG);
        }
        
        public Point3d getMarkCenter() {
            return new Point3d((p1.x+p2.x)/2, (p1.y+p2.y)/2, (p1.z+p2.z)/2);
        }
        
        public BranchGroup getTextBG() {
            return labelBG;
        }
        
        public boolean isFristPointPlaced() {
            return firstPointPlaced;
        }
        
        public void setFirstPointPlaced(boolean placed) {
            firstPointPlaced = placed;
        }
        
        public void createMarkLine() {
            Appearance lineAppr = new Appearance();
            RenderingAttributes lineRAttr = new RenderingAttributes();
            lineRAttr.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
            lineAppr.setRenderingAttributes(lineRAttr);
            ColoringAttributes lineCAppr = new ColoringAttributes(
                    LINE_COLOR, ColoringAttributes.SHADE_FLAT);
            lineCAppr.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
            lineAppr.setColoringAttributes(lineCAppr);
            lineAppr.setLineAttributes(new LineAttributes(1.5f, LineAttributes.PATTERN_SOLID, true));
            
            LineArray lineArray = new LineArray(2, LineArray.COORDINATES);
            lineArray.setCoordinate(0, p1);
            lineArray.setCoordinate(1, p2);
            markLine = new Shape3D(lineArray, lineAppr);
            markLineBG = new BranchGroup();
            markLineBG.setCapability(BranchGroup.ALLOW_DETACH);
            markLineTG = new TransformGroup();
            
            markLineTG.addChild(markLine);
            markLineBG.addChild(markLineTG);
            this.addChild(markLineBG);
        }
        
        public Shape3D createPoint(Point3d pos, TransformGroup transformParent) {
            Shape3D dot = new Shape3D();
            PointArray point = new PointArray(1, PointArray.COORDINATES);
            point.setCoordinate(0, pos);
            Appearance markAppr = new Appearance();
            RenderingAttributes markRAttr = new RenderingAttributes();
            markRAttr.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
            markAppr.setRenderingAttributes(markRAttr);
            ColoringAttributes markCAppr = new ColoringAttributes(
                    POINT_COLOR, //mark color
                    ColoringAttributes.SHADE_FLAT);
            markCAppr.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
            markAppr.setColoringAttributes(markCAppr);
            markAppr.setPointAttributes(new PointAttributes(3.0f, true));
            dot.setGeometry(point);
            dot.setAppearance(markAppr);
            
            return dot;
        }
        
        public void setVisible(boolean visible) {
            if (firstPoint != null)
                firstPoint.getAppearance().getRenderingAttributes().setVisible(visible);
            if (secondPoint != null) {
                secondPoint.getAppearance().getRenderingAttributes().setVisible(visible);
                markLine.getAppearance().getRenderingAttributes().setVisible(visible);
                labelShape3D.getAppearance().getRenderingAttributes().setVisible(visible);
            }
        }
        
        public void highlight() {
            if (firstPoint != null)
                firstPoint.getAppearance().getColoringAttributes().setColor(HIGHLIGHT_COLOR);
            if (secondPoint != null)
                secondPoint.getAppearance().getColoringAttributes().setColor(HIGHLIGHT_COLOR);
            if (markLine != null)
                markLine.getAppearance().getColoringAttributes().setColor(HIGHLIGHT_COLOR);
            if (labelShape3D != null)
                labelShape3D.getAppearance().getColoringAttributes().setColor(HIGHLIGHT_COLOR);
        }
        
        public void unHighlight() {
            if (firstPoint != null)
                firstPoint.getAppearance().getColoringAttributes().setColor(POINT_COLOR);
            if (secondPoint != null)
                secondPoint.getAppearance().getColoringAttributes().setColor(POINT_COLOR);
            if (markLine != null)
                markLine.getAppearance().getColoringAttributes().setColor(LINE_COLOR);
            if (labelShape3D != null)
                labelShape3D.getAppearance().getColoringAttributes().setColor(LABEL_COLOR);
        }
        
        public void select() {
            if (firstPoint != null)
                firstPoint.getAppearance().getColoringAttributes().setColor(SELECTED_COLOR);
            if (secondPoint != null)
                secondPoint.getAppearance().getColoringAttributes().setColor(SELECTED_COLOR);
            if (markLine != null)
                markLine.getAppearance().getColoringAttributes().setColor(SELECTED_COLOR);
            if (labelShape3D != null)
                labelShape3D.getAppearance().getColoringAttributes().setColor(SELECTED_COLOR);
        }
        
        public void unSelect() {
            unHighlight();
        }
    }
    
    public class Marker extends BranchGroup {
        private Shape3D markerDot;
        private Shape3D markerLine;

        private BranchGroup markerBG;
        private TransformGroup markerTG;
        private BranchGroup markerLineBG;
        private TransformGroup markerLineTG;
        
        public Marker(Point3d pos) {
            super();
            this.setCapability(BranchGroup.ALLOW_DETACH);
            this.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
            this.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
            
            markerDot = new Shape3D();
            
            PointArray point = new PointArray(1, PointArray.COORDINATES);
            point.setCapability(PointArray.ALLOW_COORDINATE_WRITE);
            Appearance markerAppr = new Appearance();
            RenderingAttributes markerRAttr = new RenderingAttributes();
            markerRAttr.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
            markerAppr.setRenderingAttributes(markerRAttr);
            markerAppr.setColoringAttributes(new ColoringAttributes(
                    POINT_COLOR, //marker color
                    ColoringAttributes.SHADE_FLAT));
            markerAppr.setPointAttributes(new PointAttributes(5.0f,true));
            
            markerDot.setGeometry(point);
            markerDot.setAppearance(markerAppr);
            
            markerBG = new BranchGroup();
            markerBG.setCapability(BranchGroup.ALLOW_DETACH);
            markerTG  = new TransformGroup();
            markerTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
            
            markerTG.addChild(markerDot);
            markerBG.addChild(markerTG);
            
            this.addChild(markerBG);
            updatePos(pos);
        }
        
        public void initMarkerLine(Point3d pos) {
            //draw temp marker line from first mark
            LineArray lineArray = new LineArray(2, LineArray.COORDINATES);
            lineArray.setCapability(LineArray.ALLOW_COORDINATE_WRITE);
            lineArray.setCoordinate(0, pos);
            lineArray.setCoordinate(1, pos);
            
            Appearance lineAppr = new Appearance();
            RenderingAttributes lineRAttr = new RenderingAttributes();
            lineRAttr.setCapability(RenderingAttributes.ALLOW_VISIBLE_WRITE);
            lineAppr.setRenderingAttributes(lineRAttr);
            lineAppr.setColoringAttributes(new ColoringAttributes(LINE_COLOR, ColoringAttributes.SHADE_FLAT));
            lineAppr.setLineAttributes(new LineAttributes(1.25f, LineAttributes.PATTERN_SOLID, true));
            markerLine = new Shape3D(lineArray, lineAppr);
            markerLine.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
            
            markerLineBG = new BranchGroup();
            markerLineBG.setCapability(BranchGroup.ALLOW_DETACH);
            markerLineTG = new TransformGroup();
            
            markerLineTG.addChild(markerLine);
            markerLineBG.addChild(markerLineTG);
            this.addChild(markerLineBG);
        }
        
        public void removeMarkerLine() {
            this.removeChild(markerLineBG);
        }
        
        public void updatePos(Point3d pos) {
            if (markerDot != null) {
                PointArray point = ((PointArray) markerDot.getGeometry());
                if (point != null) 
                    point.setCoordinate(0, pos);
            }

            //update line end position
            if (markerLine != null) {
                LineArray lineGeom = ((LineArray) markerLine.getGeometry());
                if (lineGeom != null) 
                    lineGeom.setCoordinate(1, pos);
            }
        }
        
        public Point3d getMarkerPos() {
            Point3d get = new Point3d();
            ((PointArray) markerDot.getGeometry()).getCoordinate(0, get);
            return get;
        }
        
        public void setVisible(boolean visible) {
            if (markerDot != null)
                markerDot.getAppearance().getRenderingAttributes().setVisible(visible);
            if (markerLine != null)
                markerLine.getAppearance().getRenderingAttributes().setVisible(visible);
        }
    }
}
