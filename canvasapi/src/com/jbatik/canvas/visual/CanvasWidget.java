/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.canvas.visual;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 * Widget yang digambarnya kayak kertasnya Corel, ada shadow abu2nya di pinggir
 * dan tebalnya pinggiran selalu 1 pixel di level zoom berapapun.
 *
 * @author RAPID02
 */
public class CanvasWidget extends Widget {

    int width;
    int height;

    public CanvasWidget(Scene aThis, int w, int h) {
        super(aThis);
        this.width = w;
        this.height = h;
        setPreferredBounds(new Rectangle(0, 0, getWidth(), getHeight()));
    }

    @Override
    protected void paintWidget() {
        Graphics2D g = getGraphics();
        g.setPaint(Color.BLACK);
        g.drawRect(0, 0, width, height);
    }

    public final int getHeight() {
        return height;
    }

    public final int getWidth() {
        return width;
    }

    protected void resetBounds() {
        setPreferredBounds(new Rectangle(0, 0, (int) Math.round(getWidth()), (int) Math.round(getHeight())));
    }

    public Dimension getDimension() {
        return new Dimension(width, height);
    }

    public void setDimension(int newWidth, int newHeight) {
        this.width = newWidth;
        this.height = newHeight;
        resetBounds();
    }

}
