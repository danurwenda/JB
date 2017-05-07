/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.lsystem;

import java.util.Collections;
import java.util.Map;

/**
 * A subclass of {@link LSystem} that provides additional parameters (angle,
 * width, length) as specified in Lindenmayer's fractals.
 *
 * @author Dimas Danurwenda
 */
public class VisualLSystem extends LSystem {

    public static final String ANGLE_PROP = "angle";
    private int angle;
    public static final String ANGLE_MULT_PROP = "angleMultiplier";
    private float angleMultiplier;

    public float getAngleMultiplier() {
        return angleMultiplier;
    }

    public void setAngleMultiplier(float angleMultiplier) {
        this.angleMultiplier = angleMultiplier;
    }
    public static final String LENGTH_PROP = "length";
    private int length;
    public static final String LENGTH_MULT_PROP = "lengthMultiplier";
    private float lengthMultiplier;

    public float getLengthMultiplier() {
        return lengthMultiplier;
    }

    public void setLengthMultiplier(float lengthMultiplier) {
        this.lengthMultiplier = lengthMultiplier;
    }
    public static final String WIDTH_PROP = "width";
    private int width;
    public static final String WIDTH_MULT_PROP = "widthMultiplier";
    private float widthMultiplier;

    public float getWidthMultiplier() {
        return widthMultiplier;
    }

    public void setWidthMultiplier(float widthMultiplier) {
        this.widthMultiplier = widthMultiplier;
    }

//    private final ArrayList<VLSStructureListener> VLSStructureListeners = new ArrayList<>();

    /**
     * Get the value of width
     *
     * @return the value of width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Set the value of width
     *
     * @param width new value of width
     */
    public void setWidth(int width) {
        this.width = width;
//        VLSStructureUpdate();
    }

    /**
     * Get the value of length
     *
     * @return the value of length
     */
    public int getLength() {
        return length;
    }

    /**
     * Set the value of length
     *
     * @param length new value of length
     */
    public void setLength(int length) {
        this.length = length;
//        VLSStructureUpdate();
    }

    /**
     * Get the value of angle
     *
     * @return the value of angle
     */
    public int getAngle() {
        return angle;
    }

    /**
     * Set the value of angle
     *
     * @param angle new value of angle
     */
    public void setAngle(int angle) {
        this.angle = angle;
//        VLSStructureUpdate();
    }

    public VisualLSystem(String axiom, Map<Character, String> rules, int iteration, int angle, int length, int width) {
        this(axiom, rules, iteration, angle, length, width, 1, 1, 1);
    }

    public VisualLSystem(String axiom, Map<Character, String> rules, int iteration, int angle, int length, int width, float am, float lm, float wm) {
        super(axiom, rules, iteration);
        this.angle = angle;
        this.length = length;
        this.width = width;
        this.angleMultiplier = am;
        this.lengthMultiplier = lm;
        this.widthMultiplier = wm;
    }

    private static final VisualLSystem instance = new VisualLSystem("", Collections.EMPTY_MAP, 1, 0, 1, 1, 1, 1, 1);

    public static VisualLSystem getDefault() {
        return instance;
    }

    VisualLSystemRenderer renderer = null;

    public VisualLSystemRenderer getRenderer() {
        return renderer;
    }

    public void setRenderer(VisualLSystemRenderer renderer) {
        this.renderer = renderer;
    }

//    public void addVLSStructureListener(VLSStructureListener listener) {
//        assert listener != null;
//        synchronized (VLSStructureListeners) {
//            VLSStructureListeners.add(listener);
//        }
//    }
//
//    public void removeVLSStructureListener(VLSStructureListener listener) {
//        synchronized (VLSStructureListeners) {
//            VLSStructureListeners.remove(listener);
//        }
//    }
//
//    private void VLSStructureUpdate() {
//        VLSStructureListener listeners[];
//        listeners = VLSStructureListeners.toArray(new VLSStructureListener[VLSStructureListeners.size()]);
//        for (VLSStructureListener listener : listeners) {
//            listener.angleChanged();
//            listener.widthChanged();
//            listener.lengthChanged();
//        }
//    }
//
//    public interface VLSStructureListener {
//
//        void angleChanged();
//
//        void widthChanged();
//
//        void lengthChanged();
//    }
}
