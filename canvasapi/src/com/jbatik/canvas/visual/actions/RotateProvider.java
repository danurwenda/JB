/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.canvas.visual.actions;

import java.awt.Point;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author Dimas Y. Danurwenda
 */
public interface RotateProvider {
    public void rotateStarted(Widget w);
    public Point getAnchorPointOnScene(Widget w, Point p);
    /**
     * Apply rotation
     * @param w widget with action
     * @param d rotation angle, clockwise, in degree
     */
    public void applyRotation(Widget w, double d);
    public void rotateFinished(Widget w);
}
