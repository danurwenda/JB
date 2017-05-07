/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.imageeditor;

import com.jbatik.canvas.visual.CanvasWidget;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import org.netbeans.api.visual.widget.Scene;

/**
 * Widget papan catur.
 *
 * @author RAPID02
 */
public class ImageCanvasWidget extends CanvasWidget {

    private static final int CHECKERBOARD_WIDTH = 10;

    public ImageCanvasWidget(Scene aThis, int w, int h) {
        super(aThis, w, h);
    }

    @Override
    protected void paintWidget() {
        Graphics2D g = getGraphics();
        g.setPaint(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        double z = getScene().getZoomFactor();
        //transparent canvas ini pakai kotak2 catur
        double s = CHECKERBOARD_WIDTH / z; //nilai sisi
        double astart = 0;
        int a = 0;
        while (astart < getWidth()) {
            double bstart = 0;
            int b = 0;
            while (bstart < getHeight()) {

                if (((a + b) & 1) == 0) {
                    g.setColor(Color.WHITE);
                } else {
                    g.setColor(Color.LIGHT_GRAY);
                }
                g.fill(new Rectangle2D.Double(
                        astart,
                        bstart,
                        astart + s > getWidth() ? (getWidth() - astart) : s,
                        bstart + s > getHeight() ? (getHeight() - bstart) : s));
                bstart += s;
                b++;
            }
            astart += s;
            a++;
        }
    }
}
