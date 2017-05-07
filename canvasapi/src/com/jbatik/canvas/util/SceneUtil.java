/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.canvas.util;

import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JComponent;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author RAPID02
 */
public final class SceneUtil {

    public static double BUFFER_GAP_RATIO = 0.1;

    public static void centerize() {

    }

    /**
     * Align widget w centered both vertically and horizontally on its Scene's
     * visible rectangle
     *
     * @param w
     * @param tlView
     * @param brView
     */
    public static void alignCenterOnView(Widget w, Widget tlView, Widget brView) {
        int tlgx, tlgy;
        Scene s = w.getScene();
        double z = s.getZoomFactor();
        Rectangle r = s.getView().getVisibleRect();
        double rw = r.width;
        double rh = r.height;
        Rectangle paperBound = w.getPreferredBounds();
        double pw = paperBound.width;
        double ph = paperBound.height;
        tlgx = (int) Math.round((rw / z - pw) / 2);
        tlgy = (int) Math.round((rh / z - ph) / 2);
        Point tl = new Point(paperBound.x - tlgx, paperBound.y - tlgy);
        tlView.setPreferredLocation(tl);
        brView.setPreferredLocation(new Point(paperBound.x + paperBound.width + tlgx, paperBound.y + paperBound.height + tlgy));
        s.validate();// HINT - forcing view JComponent to update its size
        //test 
        JComponent view = s.getView();
        Point tlOnView = s.convertSceneToView(tl);
        Rectangle n = new Rectangle(
                tlOnView.x,
                tlOnView.y,
                r.width,
                r.height
        );
        view.scrollRectToVisible(n);
        if (!n.equals(view.getVisibleRect())) {
            //dipanggil lagi gan,, -_-
            //sounds silly but it works
            view.scrollRectToVisible(n);
        }
    }

    public static void properZoom(
            Scene scene,
            double scale,
            Point center,
            Widget tlView,
            Widget brView,
            Rectangle working) {
        JComponent view = scene.getView();
        Rectangle lastRekt = view.getVisibleRect();

        Point tlOld = new Point(lastRekt.x, lastRekt.y);
        tlOld = scene.convertViewToScene(tlOld);
        Point brOld = new Point(lastRekt.x + lastRekt.width, lastRekt.y + lastRekt.height);
        brOld = scene.convertViewToScene(brOld);
        Point zoomCenter = scene.convertSceneToView(center);

        double sr = scene.getZoomFactor() / scale;
        //compute preferred locations of corners
        Point proposedTL = new Point(
                (int) Math.round(sr * (tlOld.x - center.x) + center.x),
                (int) Math.round(sr * (tlOld.y - center.y) + center.y));
        Point proposedBR = new Point(
                (int) Math.round(sr * (brOld.x - center.x) + center.x),
                (int) Math.round(sr * (brOld.y - center.y) + center.y));

        //update buffer area trus bandingin ama proposedTL/BR
        double visibleVerticalGap = lastRekt.height * BUFFER_GAP_RATIO;
        int realVerticalGap = (int) Math.round(visibleVerticalGap / scale);
        double visibleHorizontalGap = lastRekt.width * BUFFER_GAP_RATIO;
        int realHorizontalGap = (int) Math.round(visibleHorizontalGap / scale);
        Point workingBufferTL = new Point(working.x - realHorizontalGap, working.y - realVerticalGap);
        Point workingBufferBR = new Point(working.x + working.width + realHorizontalGap, working.y + working.height + realVerticalGap);

        Rectangle outer = new Rectangle(proposedTL);
        outer.add(new Rectangle(proposedBR.x, proposedBR.y, 1, 1));
        outer.add(new Rectangle(workingBufferTL.x, workingBufferTL.y, 1, 1));
        outer.add(new Rectangle(workingBufferBR.x, workingBufferBR.y, 1, 1));
        tlView.setPreferredLocation(outer.getLocation());
        brView.setPreferredLocation(new Point(outer.x + outer.width, outer.y + outer.height));

        scene.setZoomFactor(scale);

        //done all modification
        scene.validate(); // HINT - forcing to change preferred size of the JComponent view

        Point centerOnNewView = scene.convertSceneToView(center);

        Rectangle n = new Rectangle(
                centerOnNewView.x - (zoomCenter.x - lastRekt.x),
                centerOnNewView.y - (zoomCenter.y - lastRekt.y),
                lastRekt.width,
                lastRekt.height
        );
        view.scrollRectToVisible(n);
        if (!n.equals(view.getVisibleRect())) {
            //dipanggil lagi gan,, -_-
            //sounds silly but it works
            view.scrollRectToVisible(n);
        }
        view.requestFocusInWindow();
    }

    /**
     * Place two widgets on top-left and bottom-right position, such that the
     * given Rectangle can be view with specified zoom level <em>on the
     * center</em> of the screen.
     *
     * @param s the scene
     * @param z requested zoom level
     * @param r the rectangle that will be displayed at the center of the screen
     * @param tlView top-left widget
     * @param brView bottom-right widget
     * @param working rectangle containing all item on working scene
     */
    public static void properZoom(Scene s,
            double z,
            Rectangle r,
            Widget tlView,
            Widget brView,
            Rectangle working) {

        Rectangle lastRekt = s.getView().getVisibleRect();
        double rw = lastRekt.width;
        double rh = lastRekt.height;
        double pw = r.width;
        double ph = r.height;
        int tlgx = (int) Math.round((rw / z - pw) / 2);
        int tlgy = (int) Math.round((rh / z - ph) / 2);
        Point proposedTL = new Point(r.x - tlgx, r.y - tlgy);
        Point proposedBR = new Point(r.x + r.width + tlgx, r.y + r.height + tlgy);
        double visibleVerticalGap = rh * BUFFER_GAP_RATIO;
        int realVerticalGap = (int) Math.round(visibleVerticalGap / z);
        double visibleHorizontalGap = rw * BUFFER_GAP_RATIO;
        int realHorizontalGap = (int) Math.round(visibleHorizontalGap / z);
        Point workingBufferTL = new Point(working.x - realHorizontalGap, working.y - realVerticalGap);
        Point workingBufferBR = new Point(working.x + working.width + realHorizontalGap, working.y + working.height + realVerticalGap);

        Rectangle outer = new Rectangle(proposedTL);
        outer.add(new Rectangle(proposedBR.x, proposedBR.y, 1, 1));
        outer.add(new Rectangle(workingBufferTL.x, workingBufferTL.y, 1, 1));
        outer.add(new Rectangle(workingBufferBR.x, workingBufferBR.y, 1, 1));
        tlView.setPreferredLocation(outer.getLocation());
        brView.setPreferredLocation(new Point(outer.x + outer.width, outer.y + outer.height));
        s.setZoomFactor(z);

        s.validate();// HINT - forcing view JComponent to update its size
        //test 
        JComponent view = s.getView();
        Point tlOnView = s.convertSceneToView(proposedTL);
        Rectangle n = new Rectangle(
                tlOnView.x,
                tlOnView.y,
                lastRekt.width,
                lastRekt.height
        );
        view.scrollRectToVisible(n);
        if (!n.equals(view.getVisibleRect())) {
            //dipanggil lagi gan,, -_-
            //sounds silly but it works
            view.scrollRectToVisible(n);
        }
    }

}
