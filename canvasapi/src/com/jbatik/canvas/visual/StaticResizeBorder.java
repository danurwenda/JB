/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.canvas.visual;

import com.jbatik.canvas.util.GeomUtil;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.api.visual.action.ResizeControlPointResolver;
import org.netbeans.api.visual.action.ResizeProvider;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.widget.ResourceTable;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author RAPID02
 */
public final class StaticResizeBorder implements Border {

    private static final BasicStroke STROKE = new BasicStroke(1.0f, BasicStroke.JOIN_BEVEL, BasicStroke.CAP_BUTT, 5.0f, new float[]{6.0f, 3.0f}, 0.0f);

    private int thickness;
    private Color color;
    private boolean outer;
    private ResourceTableListener listener = null;
    private Scene scene;

    public StaticResizeBorder(int t, Widget s) {
        this(t, Color.BLACK, false, s);
    }

    public StaticResizeBorder(int thickness, Color color, boolean outer, Widget s) {
        this.thickness = thickness;
        this.color = color;
        this.outer = outer;
        this.scene = s.getScene();
    }

    public StaticResizeBorder(int thickness, String property, Widget attachedWidget, boolean outer) {
        this(thickness, property, attachedWidget.getResourceTable(), outer);
    }

    public StaticResizeBorder(int thickness, String property, ResourceTable table, boolean outer) {
        this.thickness = thickness;
        this.outer = outer;

        Object value = table.getProperty(property);
        if (value instanceof Color) {
            this.color = (Color) value;
        }

        listener = new ResourceTableListener();
        table.addPropertyChangeListener(property, listener);
    }

    @Override
    public Insets getInsets() {
        double t = thickness;
        int tz = (int) Math.ceil(t / scene.getZoomFactor());
        return new Insets(tz, tz, tz, tz);
    }

    public boolean isOuter() {
        return outer;
    }

    @Override
    public void paint(Graphics2D gr, Rectangle bounds) {
        gr.setColor(color);

//        Stroke stroke = gr.getStroke();
//        gr.setStroke(STROKE);
        double t = thickness;
        double tz = t / scene.getZoomFactor();
        Rectangle2D.Double border;
        if (outer) {
            border = new Rectangle2D.Double(bounds.x + 0.5, bounds.y + 0.5, bounds.width - 1.0, bounds.height - 1.0);
        } else {
//            border = new Rectangle2D.Double(bounds.x + thickness + 0.5, bounds.y + thickness + 0.5, bounds.width - thickness - thickness - 1.0, bounds.height - thickness - thickness - 1.0);
            border = new Rectangle2D.Double(bounds.x + tz + 0.5, bounds.y + tz + 0.5, bounds.width - 2 * tz - 1.0, bounds.height - 2 * tz - 1.0);
        }
//        gr.draw(border);
//        gr.setStroke(stroke);

        //here we use Graphic2D.fill since we can't draw a rectangle smaller than 1x1 unit if we use Graphic2D.drawRect instead
        gr.fill(new Rectangle2D.Double(border.x - tz, border.y - tz, tz, tz));
        gr.fill(new Rectangle2D.Double(bounds.x + bounds.width - tz, bounds.y, tz, tz));//top right
        gr.fill(new Rectangle2D.Double(bounds.x, bounds.y + bounds.height - tz, tz, tz));//bottom left
        gr.fill(new Rectangle2D.Double(border.x + border.width, border.y + border.height, tz, tz));

        Point center = new Point(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
        if (bounds.width >= tz * 5) {
            gr.fill(new Rectangle2D.Double(center.x - tz / 2, bounds.y, tz, tz));
            gr.fill(new Rectangle2D.Double(center.x - tz / 2, bounds.y + bounds.height - tz, tz, tz));
        }
        if (bounds.height >= tz * 5) {
            gr.fill(new Rectangle2D.Double(bounds.x, center.y - tz / 2, tz, tz));
            gr.fill(new Rectangle2D.Double(bounds.x + bounds.width - tz, center.y - tz / 2, tz, tz));
        }
    }

    public boolean isOpaque() {
        return outer;
    }

    public class ResourceTableListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent event) {
            color = (Color) event.getNewValue();
        }
    }

}
