/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jbatik.modules.corak.component;

import javafx.scene.paint.Color;
import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.PointArray;
import javax.media.j3d.PointAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;

/**
 *
 * @author risman
 */
public class PivotPointAdjustor extends BranchGroup {
    
    private BranchGroup pivotPointBG;
    private TransformGroup pivotPointTG;
    private Shape3D pivotPoint;
    private PivotPointPlacer placer;
    private Point3d pivotPointPos;
    
    public PivotPointAdjustor(Point3d pos) {
        super();
        this.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
        this.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
        this.setCapability(BranchGroup.ALLOW_DETACH);
        placer = new PivotPointPlacer(pos);
        this.addChild(placer);
    }
    
    public void putPivotPoint(Point3d pos) {
        
        if (pivotPoint == null) {
            pivotPointBG = new BranchGroup();
            pivotPointBG.setCapability(BranchGroup.ALLOW_DETACH);
            pivotPointTG = new TransformGroup();

            pivotPoint = new Shape3D();
            PointArray point = new PointArray(1, PointArray.COORDINATES);
            point.setCapability(PointArray.ALLOW_COORDINATE_WRITE);
            point.setCoordinate(0, pos);
            Appearance ppAppr = new Appearance(); 
            ppAppr.setColoringAttributes(new ColoringAttributes(
                    new Color3f(0.65f, 0.85f, 0.01f), //pivotpoint color
                    ColoringAttributes.SHADE_FLAT));
            ppAppr.setPointAttributes(new PointAttributes(12.0f, true));
            pivotPoint.setGeometry(point);
            pivotPoint.setAppearance(ppAppr);

            pivotPointTG.addChild(pivotPoint);
            pivotPointBG.addChild(pivotPointTG);

            this.addChild(pivotPointBG);
        } else {
            PointArray point = ((PointArray) pivotPoint.getGeometry());
                if (point != null) 
                    point.setCoordinate(0, pos);
        }
        
        pivotPointPos = pos;
               
    }
    
    public Point3d getPivotPointPos() {
        return pivotPointPos;
    }
    
    public BranchGroup getPivotPointBG() {
        return pivotPointBG;
    }
    
    public PivotPointPlacer getPlacer() {
        if (placer.getParent() == null) {
            this.addChild(placer);
        }
        return placer;
    }
    
    public class PivotPointPlacer extends BranchGroup {
        
        private Shape3D placerDot;
        private BranchGroup placerBG;
        private TransformGroup placerTG;
        
        public PivotPointPlacer (Point3d pos) {
            super();
            this.setCapability(BranchGroup.ALLOW_DETACH);
            this.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
            this.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
            
            placerDot = new Shape3D();
            
            PointArray point = new PointArray(1, PointArray.COORDINATES);
            point.setCapability(PointArray.ALLOW_COORDINATE_WRITE);
            Appearance placerAppr = new Appearance();
            placerAppr.setColoringAttributes(new ColoringAttributes(
                    new Color3f(0.71f, 0.92f, 0.01f), //placer color
                    ColoringAttributes.SHADE_FLAT));
            placerAppr.setPointAttributes(new PointAttributes(5.0f,true));
            
            placerDot.setGeometry(point);
            placerDot.setAppearance(placerAppr);
            
            placerBG = new BranchGroup();
            placerBG.setCapability(BranchGroup.ALLOW_DETACH);
            placerTG  = new TransformGroup();
            placerTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
            
            placerTG.addChild(placerDot);
            placerBG.addChild(placerTG);
            
            this.addChild(placerBG);
            updatePos(pos);
        }
        
        public void updatePos(Point3d pos) {
            if (placerDot != null) {
                PointArray point = ((PointArray) placerDot.getGeometry());
                if (point != null) 
                    point.setCoordinate(0, pos);
            }
        }
        
        public Point3d getPos() {
            Point3d get = new Point3d();
            ((PointArray) placerDot.getGeometry()).getCoordinate(0, get);
            return get;
        }
    }
}
    

