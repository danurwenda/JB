/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.canvas.visual;

import com.jbatik.canvas.util.SceneUtil;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JComponent;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 * Width, height and position of DesktopLayer will be adjusted to screen
 * dimension
 */
public class DesktopLayer extends LayerWidget {

    private final Widget tlWorkingBuffer;
    private final Widget brWorkingBuffer;

    boolean sizeKnown;

    Rectangle lastRekt = new Rectangle();
    //working buffer will change only if working rectangle is changed
    private Widget tlView;
    private Widget brView;

    public void resetCorners() {
        tlView.setPreferredLocation(new Point());
        brView.setPreferredLocation(new Point());
    }

    public Widget getTopLeft() {
        return tlView;
    }

    public Widget getBottomRight() {
        return brView;
    }

    public DesktopLayer(Scene s) {
        super(s);
        sizeKnown = false;
        tlView = new Widget(s);
        brView = new Widget(s);
        addChild(tlView);
        addChild(brView);
        tlWorkingBuffer = new Widget(s);
        brWorkingBuffer = new Widget(s);
        addChild(tlWorkingBuffer);
        addChild(brWorkingBuffer);
    }
    
    
}
