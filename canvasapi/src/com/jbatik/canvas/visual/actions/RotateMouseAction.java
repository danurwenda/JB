/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.canvas.visual.actions;

import java.awt.Point;
import java.awt.event.MouseEvent;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.Widget;

/**
 * A drag action to rotate one or more object on scene.
 *
 * @author Dimas Y. Danurwenda <dimas@danurwenda.com>
 */
public class RotateMouseAction extends WidgetAction.LockedAdapter {

    private final RotateProvider provider;
    private Widget rotatingWidget = null;
    //all point is on Scene's coordinates system
    private Point initialMouseLocation = null;
    private Point fixedPoint = null;
    private Point dragSceneLocation = null;
    private boolean discreet;
    private final double DISCREET_ANGLE = 15d;

    public RotateMouseAction(RotateProvider p, boolean discreet) {
        this.provider = p;
        this.discreet = discreet;
    }

    public int getMouseButton() {
        return MouseEvent.BUTTON1;
    }

    protected boolean mustBeSelected() {
        return false;
    }

    @Override
    protected boolean isLocked() {
        return rotatingWidget != null;
    }

    /**
     * Detect mousePressed event on middle button.
     *
     * @param widget
     * @param event
     * @return
     */
    @Override
    public State mousePressed(Widget widget, WidgetMouseEvent event) {
        if (isLocked()) {
            return State.createLocked(widget, this);
        }
        if (!(mustBeSelected() && !widget.getState().isSelected())) {
            if (event.getButton() == getMouseButton() && event.getClickCount() == 1) {
                Point point = event.getPoint();
                rotatingWidget = widget;
                fixedPoint = provider.getAnchorPointOnScene(widget, point);
                initialMouseLocation = widget.convertLocalToScene(point);
                dragSceneLocation = widget.convertLocalToScene(point);
                provider.rotateStarted(widget);
                return State.createLocked(rotatingWidget, this);
            }
        }
        return State.REJECTED;
    }

    @Override
    public State mouseDragged(Widget widget, WidgetMouseEvent event) {
        return rotate(widget, event.getPoint()) ? State.createLocked(widget, this) : State.REJECTED;
    }

    @Override
    public State mouseReleased(Widget widget, WidgetMouseEvent event) {
        boolean state;
        if (initialMouseLocation != null && initialMouseLocation.equals(widget.convertLocalToScene(event.getPoint()))) {
            //tanpa geser langsung release
            state = true;
        } else {
            state = rotate(widget, event.getPoint());
        }
        if (state) {
            rotatingWidget = null;
            dragSceneLocation = null;
            fixedPoint = null;
            initialMouseLocation = null;
            provider.rotateFinished(widget);
        }
        return state ? State.CONSUMED : State.REJECTED;
    }

    private boolean rotate(Widget widget, Point point) {
        if (rotatingWidget != widget) {
            return false;
        }
        initialMouseLocation = null;
        point = widget.convertLocalToScene(point);
        double delta = Math.toDegrees(resolveDelta(dragSceneLocation, fixedPoint, point));
        if (discreet) {
            delta = DISCREET_ANGLE * Math.floor(delta / DISCREET_ANGLE);
        }
        provider.applyRotation(widget, delta);
        return true;
    }

    private static double resolveDelta(Point o, Point f, Point p) {
        Point a = new Point(o.x - f.x, o.y - f.y);
        Point b = new Point(p.x - f.x, p.y - f.y);
        double xx = Math.atan2(a.x * b.y - a.y * b.x, a.x * b.x + a.y * b.y);
        return xx;
    }

}
