/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.util;

import com.jbatik.modules.layout.layering.SubLayoutLayer;
import com.jbatik.modules.layout.visual.widgets.GroupLayerWidget;
import com.jbatik.modules.layout.visual.widgets.SubLayoutWidget;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author Slurp
 */
public class WidgetUtil {

    public static Rectangle findArea(Widget w) {
        if (w instanceof SubLayoutWidget) {
            Point fp = w.getPreferredLocation();
            Rectangle f = w.getPreferredBounds();
            f.translate(fp.x, fp.y);
            return f;
        } else if (w instanceof GroupLayerWidget) {
            Rectangle f = null;
            for (Widget child : w.getChildren()) {
                Rectangle childR = findArea(child);
                if (childR != null) {
                    if (f == null) {
                        f = childR;
                    } else {
                        f.add(childR);
                    }
                }
            }
            return f;
        }
        return null;
    }

    public static Rectangle findArea(ObjectScene scene, Set objs) {
        Widget candidate = null;
        Iterator it = objs.iterator();
        while (candidate == null && it.hasNext()) {
            //see whether it.next is a SLL
            Object o = it.next();
            if (o instanceof SubLayoutLayer) {
                candidate = scene.findWidget(o);
            }
        }
        if (candidate == null) {
            //out of while loop, but candidate still == null
            return new Rectangle(0, 0, 0, 0);
        } else {
            //return a rectangle that contains all selected object
            //get the widget of the first selected object
            Widget first = candidate;
            Point fp = first.getPreferredLocation();
            Rectangle f = first.getPreferredBounds();
            f.translate(fp.x, fp.y);
            while (it.hasNext()) {
                Object o = it.next();
                if (o instanceof SubLayoutLayer) {
                    Widget w = scene.findWidget(o);
                    Point p = w.getPreferredLocation();
                    Rectangle r = w.getPreferredBounds();
                    r.translate(p.x, p.y);
                    f.add(r);
                }
            }
            return f;
        }
    }
}
