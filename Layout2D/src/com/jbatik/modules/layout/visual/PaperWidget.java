/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.visual;

import com.jbatik.canvas.visual.CanvasWidget;
import com.jbatik.modules.layout.LayoutDocument;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.api.visual.model.ObjectScene;

/**
 * Widget yang digambarnya kayak kertasnya Corel, ada shadow abu2nya di pinggir
 * dan tebalnya pinggiran selalu 1 pixel di level zoom berapapun.
 *
 * @author RAPID02
 */
public class PaperWidget extends CanvasWidget
        implements PropertyChangeListener {

    LayoutDocument document;
    ObjectScene os;
    boolean drawShadow;

    PaperWidget(ObjectScene aThis, LayoutDocument ld) {
        super(aThis, ld.getWidthInPixel(), ld.getHeightInPixel());
        os = aThis;
        document = ld;
        drawShadow = true;
        ld.addPropertyChangeListener(this);
        //since ld and the instance of PaperWidget are created on LayoutScene,
        //we don't need to call ld.removePropertyChangeListener
    }

    @Override
    protected void paintWidget() {
        Graphics2D g = getGraphics();
        if (drawShadow) {
            //draw drop shadow first
            g.setPaint(Color.LIGHT_GRAY);
            g.fill(new Rectangle2D.Double(5 / (getScene().getZoomFactor()), 5 / (getScene().getZoomFactor()), getWidth(), getHeight()));
        }
        //then draw the paper
        g.setPaint(document.getBackground());
        g.fill(new Rectangle2D.Double(0, 0, getWidth(), getHeight()));
        if (drawShadow) {
            //draw paper outline
            Stroke previous = g.getStroke();
            g.setStroke(new BasicStroke((float) (1.0 / getScene().getZoomFactor())));
            g.setPaint(Color.GRAY);
            g.draw(new Rectangle2D.Double(0, 0, getWidth(), getHeight()));
            g.setStroke(previous);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String type = evt.getPropertyName();
        switch (type) {
            case LayoutDocument.BACKGROUND_PROP:
                repaint();
                break;
            case LayoutDocument.DPI_PROP:
            case LayoutDocument.HEIGHT_PROP:
            case LayoutDocument.WIDTH_PROP:
                setDimension(document.getWidthInPixel(), document.getHeightInPixel());
                getScene().repaint();
                break;
            case LayoutDocument.PAPER_SIZE_PROP:
                break;
            case LayoutDocument.UNIT_PROP:
                break;
        }
    }
}
