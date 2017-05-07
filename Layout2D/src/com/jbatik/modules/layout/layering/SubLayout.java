/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.layering;

import com.jbatik.lsystem.InvalidableVisualLSystem;
import java.awt.Point;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A SubLayout is a complete VisualLSystem plus properties related to its
 * visualization in a Layout
 *
 * @author Dimas Y. Danurwenda <dimas@danurwenda.com>
 */
public class SubLayout extends InvalidableVisualLSystem {

    private static class DefaultMap {

        static HashMap getDefault() {
            HashMap m = new HashMap(0);
            m.put('A', "F;+\"?A");
            return m;
        }
    }

    private int x;// x offset relative to 0,0 of LayoutCanvas
    private int y;// y offset relative to 0,0 of LayoutCanvas
    private float squareRotationAngle;
    private Map<Integer, String> imageColorIndex;

    /**
     * CopyCon
     *
     * @param s copied
     */
    public SubLayout(SubLayout s) {
        super(s.rawAxiom, s.rawDetails, s.iteration, s.getAngle(), s.getLength(), s.getWidth());
        this.x = s.x;
        this.y = s.y;
        this.squareRotationAngle = s.squareRotationAngle;
        this.imageColorIndex = new HashMap<>(s.imageColorIndex);
        
        setAngleMultiplier(s.getAngleMultiplier());
        setLengthMultiplier(s.getLengthMultiplier());
        setWidthMultiplier(s.getWidthMultiplier());
    }

    public SubLayout(String axiom, Map rules, int iteration, int angle, int length, int width, float sqrot) {
        this(axiom, rules, iteration, angle, length, width, sqrot, 1, 1, 1);
    }

    public SubLayout(String axiom, Map rules, int iteration, int angle, int length, int width, float sqrot, float aM, float lM, float wM) {
        super(axiom, rules, iteration, angle, length, width);
        this.x = 0;
        this.y = 0;
        this.squareRotationAngle = sqrot;
        this.imageColorIndex = new HashMap<>();
        setAngleMultiplier(aM);
        setLengthMultiplier(lM);
        setWidthMultiplier(wM);
    }

    public SubLayout(String ax, String d, int i, int a, int l, int w) {
        super(ax, d, i, a, l, w);
        this.x = 0;
        this.y = 0;
        this.squareRotationAngle = 0;
        this.imageColorIndex = new HashMap<>();

    }

    public SubLayout() {
        this("A", DefaultMap.getDefault(), 1, 0, 100, 100, 0);
    }

    public Map<Integer, String> getImageColorIndex() {
        return imageColorIndex;
    }

    public void setImageColorIndex(Map<Integer, String> imageColorIndex) {
        this.imageColorIndex = imageColorIndex;
    }

    public void setImageForIndex(int colorIndex, String file) {
        if (file == null) {
            imageColorIndex.remove(colorIndex);
        } else {
            imageColorIndex.put(colorIndex, file);
        }
    }

    public String getImageForIndex(int currentColorIdx) {
        return imageColorIndex.get(currentColorIdx);
    }
    public static final String X_PROP = "x";

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }
    public static final String Y_PROP = "y";

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public static final String SQROT_PROP = "squareRotationAngle";

    public float getSquareRotationAngle() {
        return squareRotationAngle;
    }

    public void setSquareRotationAngle(float squareRotationAngle) {
        this.squareRotationAngle = squareRotationAngle;
    }

    /**
     * Set the start location of the sublayout.
     *
     * @param suggestedLocation
     */
    public void setLocation(Point suggestedLocation) {
        this.x = suggestedLocation.x;
        this.y = suggestedLocation.y;
    }
    private static final SubLayout instance = new SubLayout("", Collections.EMPTY_MAP, 1, 0, 1, 1, 0);

    public static SubLayout getDefault() {
        return instance;
    }

}
