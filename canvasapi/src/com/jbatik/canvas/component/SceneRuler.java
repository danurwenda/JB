/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.canvas.component;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;
import javax.swing.JComponent;
import org.netbeans.api.visual.widget.Scene;

/**
 * Ruler untuk sebuah Scene. Akan ditampilkan di sisi atas dan kiri. Untuk sisi
 * kanan dan bawah biarkan default scrollbar.
 *
 * @author Dimas Y. Danurwenda <dimas@danurwenda.com>
 */
public class SceneRuler extends JComponent {

    public static int HORIZONTAL = 0;
    public static int VERTICAL = 1;

    private Scene scene;
    protected int orientation;
    protected Unit<Length> displayedUnit;

    public SceneRuler(Scene scene) {
        this(scene, NonSI.PIXEL, HORIZONTAL);
    }

    public SceneRuler(Scene s, int orientation) {
        this(s, NonSI.PIXEL, orientation);
    }

    public SceneRuler(Scene scene, Unit<Length> unit, int horizontal) {
        super();
        this.scene = scene;
        this.displayedUnit = unit;
        this.orientation = horizontal;
        setBackground(new Color(240, 240, 240));
        setOpaque(true);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Rectangle r = g.getClipBounds();
        double z = scene.getZoomFactor();
        int step = computeStep(z);
        Point sloc = scene.getLocation();
        double xDiff = sloc.x * z - r.x;
        double yDiff = sloc.y * z - r.y;

        if (displayedUnit.equals(NonSI.PIXEL)) {
            int pxlStep = (int) (z * step);
            if (orientation == HORIZONTAL) {
                drawHorizontalMark(g, step, pxlStep, xDiff, r);
            } else if (orientation == VERTICAL) {
                drawVerticalMark(g, step, pxlStep, yDiff, r);
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        if (orientation == HORIZONTAL) {
            return new Dimension((int) scene.getView().getPreferredSize().getWidth(),
                    25);
        } else if (orientation == VERTICAL) {
            return new Dimension(25, (int) scene.getView().getPreferredSize()
                    .getHeight());
        }
        return null;
    }

    /**
     * 2000 pixel on zoomFactor 12.5%, 1000 pixel on zoomFactor 25%, 500 pixel
     * on 50%, 200 pixel on 100%, 100 pixel on 200%, 50 pixel on 400%
     */
    protected int computeStep(double z) {
        if (z <= 0.125) {
            return 2000;
        } else if (z <= 0.25) {
            return 1000;
        } else if (z <= 0.5) {
            return 500;
        } else if (z <= 1) {
            return 200;
        } else if (z <= 2) {
            return 100;
        } else {
            return 50;
        }
    }

    protected void drawHorizontalMark(Graphics g, double markStep, double pxlStep, double diff, Rectangle r) {
        int xPos;
        //draw 0 and to the left (negative direction)
        double negMark = diff;
        int negCount = 0;
        while (negMark >= 0) {
            //layak digambar
            xPos = (int) (r.x + negMark);
            g.drawLine(xPos, 0, xPos, 3);
            g.drawString("" + (negCount * markStep), xPos, 16);
            negMark -= pxlStep;
            negCount--;
        }
        //draw to the right (positive direction)
        double posMark = diff + pxlStep;
        int posCount = 1;
        while (posMark < r.width) {
            //layak digambar
            xPos = (int) (r.x + posMark);
            g.drawLine(xPos, 0, xPos, 3);
            g.drawString("" + (posCount * markStep), xPos, 16);
            posMark += pxlStep;
            posCount++;
        }
    }

    protected void drawVerticalMark(Graphics g, double markStep, double pxlStep, double diff, Rectangle r) {
        int yPos;
        //draw 0 and to the left (negative direction)
        double negMark = diff;
        int negCount = 0;
        while (negMark >= 0) {
            //layak digambar
            yPos = (int) (r.y + negMark);
            g.drawLine(0, yPos, 3, yPos);
            g.drawString("" + (negCount * markStep), 1, yPos + 13);
            negMark -= pxlStep;
            negCount--;
        }
        //draw to the right (positive direction)
        double posMark = diff + pxlStep;
        int posCount = 1;
        while (posMark < r.height) {
            //layak digambar
            yPos = (int) (r.y + posMark);
            g.drawLine(0, yPos, 3, yPos);
            g.drawString("" + (posCount * markStep), 1, yPos + 13);
            posMark += pxlStep;
            posCount++;
        }
    }
}
