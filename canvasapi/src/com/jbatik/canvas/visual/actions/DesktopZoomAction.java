/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.canvas.visual.actions;

import com.jbatik.canvas.util.SceneUtil;
import com.jbatik.canvas.visual.DesktopLayer;
import java.awt.Rectangle;
import javax.swing.JComponent;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author Dimas Y. Danurwenda
 */
public  abstract class DesktopZoomAction extends WidgetAction.Adapter {

        double zoomMultiplier;
        Widget tlView, brView;

        public DesktopZoomAction(double i, DesktopLayer desktopLayer) {
            this.zoomMultiplier = i;
            tlView = desktopLayer.getTopLeft();
            brView = desktopLayer.getBottomRight();
        }
        
        public abstract Rectangle getRectangle();

        @Override
        public State mouseWheelMoved(Widget widget, WidgetMouseWheelEvent event) {

            Scene scene = widget.getScene();

            int amount = event.getWheelRotation();

            double scale = 1.0;
            while (amount > 0) {
                scale /= zoomMultiplier;
                amount--;
            }
            while (amount < 0) {
                scale *= zoomMultiplier;
                amount++;
            }
            scale *= scene.getZoomFactor();
            if (scale < 0.03125) {
                scale = 0.03125;// HINT - Corel
            } else if (scale > 8.0) {
                scale = 8.0;// hint - control point buat resize itu dimensinya 8x8
            }
            JComponent view = scene.getView();
            if (view != null) {
                SceneUtil.properZoom(scene, scale, event.getPoint(), tlView, brView, getRectangle());
            } else {
                scene.setZoomFactor(scale);
            }
            return State.CONSUMED;
        }

    }
