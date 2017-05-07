/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.component;

import com.jbatik.canvas.component.SceneRuler;
import com.jbatik.modules.layout.visual.LayoutScene;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.swing.JMenuItem;
import org.jscience.physics.amount.Amount;

/**
 * Ruler untuk sebuah LayoutScene. Akan ditampilkan di sisi atas dan kiri. Untuk
 * sisi kanan dan bawah biarkan default scrollbar.
 *
 * @author Dimas Y. Danurwenda <dimas@danurwenda.com>
 */
public class LayoutSceneRuler extends SceneRuler
        implements ActionListener {

    private LayoutScene layoutScene;

    public LayoutSceneRuler(LayoutScene scene) {
        this(scene, HORIZONTAL);
    }

    public LayoutSceneRuler(LayoutScene scene, int horizontal) {
        super(scene, scene.getlSystem().getDocument().getUnit(), horizontal);
        this.layoutScene = scene;
        setBackground(new Color(240, 240, 240));
        setOpaque(true);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Rectangle r = g.getClipBounds();
        double z = layoutScene.getZoomFactor();
        int step = computeStep(z);
        Point sloc = layoutScene.getLocation();
        double xDiff = sloc.x * z - r.x;
        double yDiff = sloc.y * z - r.y;

        if (displayedUnit.equals(NonSI.INCH)) {
            int dpi = layoutScene.getlSystem().getDocument().getDPI();
            double inchStep = Math.pow(2, Math.floor(Math.log(step * 1.0 / dpi) / Math.log(2)));
            double pxlStep = z * inchStep * dpi;
            if (orientation == HORIZONTAL) {
                drawHorizontalMark(g, inchStep, pxlStep, xDiff, r);
            } else if (orientation == VERTICAL) {
                drawVerticalMark(g, inchStep, pxlStep, yDiff, r);
            }
        } else if (displayedUnit.equals(SI.MILLIMETER) || displayedUnit.equals(SI.CENTIMETER)) {
            int dpi = layoutScene.getlSystem().getDocument().getDPI();
            double metricStep = Math.pow(2, Math.floor(Math.log(step * 1.0 / dpi) / Math.log(2)));
            metricStep = Amount.valueOf(metricStep, NonSI.INCH).doubleValue(displayedUnit);

            boolean found = false;
            double metricStepCandidate = 1;
            while (!found) {
                if (metricStep < 2 * metricStepCandidate) {
                    metricStep = metricStepCandidate;
                    found = true;
                } else if (metricStep < 5 * metricStepCandidate) {
                    metricStep = 2 * metricStepCandidate;
                    found = true;
                } else if (metricStep < 10 * metricStepCandidate) {
                    metricStep = 5 * metricStepCandidate;
                    found = true;
                } else {
                    metricStepCandidate *= 10;
                }
            }
            double pxlStep = z * Amount.valueOf(metricStep, displayedUnit).doubleValue(NonSI.INCH) * dpi;
            if (orientation == HORIZONTAL) {
                drawHorizontalMark(g, metricStep, pxlStep, xDiff, r);
            } else if (orientation == VERTICAL) {
                drawVerticalMark(g, metricStep, pxlStep, yDiff, r);
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        if (orientation == HORIZONTAL) {
            return new Dimension((int) layoutScene.getView().getPreferredSize().getWidth(),
                    25);
        } else if (orientation == VERTICAL) {
            return new Dimension(25, (int) layoutScene.getView().getPreferredSize()
                    .getHeight());
        }
        return null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String unit = ((JMenuItem) e.getSource()).getText();
        if (unit.equals("Pixels")) {
            displayedUnit = NonSI.PIXEL;
        } else if (unit.equals("Inches")) {
            displayedUnit = NonSI.INCH;
        } else if (unit.equals("Millimeters")) {
            displayedUnit = SI.MILLIMETER;
        } else if (unit.equals("Centimeters")) {
            displayedUnit = SI.CENTIMETER;
        }
        repaint();
    }
}
