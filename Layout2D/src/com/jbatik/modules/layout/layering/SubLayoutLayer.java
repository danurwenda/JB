/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.layering;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A layer wrapper to SubLayout. Handles property change on underlying structure
 * (SubLayout).
 *
 * @author RAPID02
 */
public class SubLayoutLayer extends LayoutLayer {

    @Override
    public SubLayoutLayer clone() throws CloneNotSupportedException {
        SubLayoutLayer clone = (SubLayoutLayer) super.clone();
        //TODO : preparing clone of sublayout
        clone.sublayout = new SubLayout(sublayout);
        return clone;
    }

    @Override
    public String getType() {
        return "sublayout";
    }

    private SubLayout sublayout;

    public SubLayout getSublayout() {
        return sublayout;
    }

    public void setSublayout(SubLayout sublayout) {
        this.sublayout = sublayout;
    }

    public SubLayoutLayer() {
        this(new SubLayout(), null);
    }

    public SubLayoutLayer(GroupLayer parent) {
        this(new SubLayout(), parent);
    }

    public SubLayoutLayer(SubLayout s, GroupLayer parent) {
        super(parent);
        this.sublayout = s;
        mappable = true;
        locked = false;
        visible = true;
        name = "Layer";
    }

    //delegating methods to wrapped structure
    //from the most basal class
    //LSystem.iteration
    public int getIteration() {
        return sublayout.getIteration();
    }

    public void setIteration(int iteration) {
        int old = getIteration();
        sublayout.setIteration(iteration);
        pcs.firePropertyChange(SubLayout.ITERATION_PROP, old, iteration);
    }
    //VisualLSystem.angle

    public int getAngle() {
        return sublayout.getAngle();
    }

    public void setAngle(int angle) {
        int old = getAngle();
        sublayout.setAngle(angle);
        pcs.firePropertyChange(SubLayout.ANGLE_PROP, old, angle);
    }

    public float getAngleMultiplier() {
        return sublayout.getAngleMultiplier();
    }

    public void setAngleMultiplier(float angle) {
        float old = getAngleMultiplier();
        sublayout.setAngleMultiplier(angle);
        pcs.firePropertyChange(SubLayout.ANGLE_MULT_PROP, old, angle);
    }
    //VisualLSystem.length

    public int getLength() {
        return sublayout.getLength();
    }

    public void setLength(int angle) {
        int old = getLength();
        sublayout.setLength(angle);
        pcs.firePropertyChange(SubLayout.LENGTH_PROP, old, angle);
    }

    public float getLengthMultiplier() {
        return sublayout.getLengthMultiplier();
    }

    public void setLengthMultiplier(float length) {
        float old = getLengthMultiplier();
        sublayout.setLengthMultiplier(length);
        pcs.firePropertyChange(SubLayout.LENGTH_MULT_PROP, old, length);
    }

    //VisualLSystem.width
    public int getWidth() {
        return sublayout.getWidth();
    }

    public void setWidth(int angle) {
        int old = getWidth();
        sublayout.setWidth(angle);
        pcs.firePropertyChange(SubLayout.WIDTH_PROP, old, angle);
    }

    public float getWidthMultiplier() {
        return sublayout.getWidthMultiplier();
    }

    public void setWidthMultiplier(float width) {
        float old = getWidthMultiplier();
        sublayout.setWidthMultiplier(width);
        pcs.firePropertyChange(SubLayout.WIDTH_MULT_PROP, old, width);
    }

    //SubLayout.position
    public int getX() {
        return sublayout.getX();
    }

    public void setX(int x) {
        int old = getX();
        sublayout.setX(x);
        pcs.firePropertyChange(SubLayout.X_PROP, old, x);
    }

    public int getY() {
        return sublayout.getY();
    }

    public void setY(int y) {
        int old = getY();
        sublayout.setY(y);
        pcs.firePropertyChange(SubLayout.Y_PROP, old, y);
    }

    public void setLocation(Point suggestedLocation) {
        setX(suggestedLocation.x);
        setY(suggestedLocation.y);
    }

    public Point getLocation() {
        return new Point(sublayout.getX(), sublayout.getY());
    }

    //SubLayout.sqrot
    public float getSquareRotationAngle() {
        return sublayout.getSquareRotationAngle();
    }

    public void setSquareRotationAngle(float squarerotationangle) {
        float old = getSquareRotationAngle();
        sublayout.setSquareRotationAngle(squarerotationangle);
        pcs.firePropertyChange(SubLayout.SQROT_PROP, old, squarerotationangle);
    }

    //SubLayout.image mapping
    public static final String IMAGES_MAP = "map";
    public static final String INDEXED_MAP = "indexedmap";

    public Map<Integer, String> getImageColorIndex() {
        return sublayout.getImageColorIndex();
    }

    public void setImageColorIndex(Map<Integer, String> imageColorIndex) {
        Map old = getImageColorIndex();
        sublayout.setImageColorIndex(imageColorIndex);
        pcs.firePropertyChange(IMAGES_MAP, old, imageColorIndex);
    }

    public void setImageForIndex(int colorIndex, String file) {
        String old = getImageForIndex(colorIndex);
        if (file == null) {
            getImageColorIndex().remove(colorIndex);
        } else {
            getImageColorIndex().put(colorIndex, file);
        }
        pcs.firePropertyChange(INDEXED_MAP,
                new ImageMap(colorIndex, old),
                new ImageMap(colorIndex, file));
    }

    public String getImageForIndex(int currentColorIdx) {
        return sublayout.getImageForIndex(currentColorIdx);
    }

    public void resetMapping() {
        Map empty = new HashMap();
        setImageColorIndex(empty);
    }

    public class ImageMap {

        int idx;
        String file;

        public int getIdx() {
            return idx;
        }

        public void setIdx(int idx) {
            this.idx = idx;
        }

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }

        public ImageMap(int idx, String file) {
            this.idx = idx;
            this.file = file;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ImageMap) {
                ImageMap o = (ImageMap) obj;
                return this.idx == o.idx
                        && (this.file == null
                                ? o.file == null
                                : this.file.equals(o.file));
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 79 * hash + this.idx;
            hash = 79 * hash + Objects.hashCode(this.file);
            return hash;
        }
    }
}
