/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.component;

import com.jbatik.core.api.GlobalUndoManager;
import com.jbatik.modules.layout.layering.SubLayoutLayer;
import com.jbatik.modules.layout.util.WidgetUtil;
import com.jbatik.modules.layout.visual.actions.SubLayoutMoveUndoableEdit;
import com.jbatik.modules.layout.visual.widgets.GroupLayerWidget;
import com.jbatik.modules.layout.visual.widgets.SubLayoutWidget;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.CompoundEdit;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Action that aligns selected components in the specified direction.
 *
 * @author Slurp
 */
public class AlignAction extends AbstractAction {

    // PENDING change to icons provided by Dusan
    private static final String ICON_BASE = "com/jbatik/modules/layout/component/resources/align_"; // NOI18N

    /**
     * Scene to work on.
     */
    private ObjectScene scene;
    /**
     * Dimension to align in.
     */
    private int dimension;
    /**
     * Requested alignment.
     */
    private int alignment;

    /**
     * Creates action that aligns selected components in the specified
     * direction.
     *
     * @param dimension dimension to align in.
     * @param alignment requested alignment.
     */
    AlignAction(ObjectScene scene, int dimension, int alignment) {
        this.scene = scene;
        this.dimension = dimension;
        this.alignment = alignment;
        boolean horizontal = (dimension == LayoutConstants.HORIZONTAL);
        boolean leading = (alignment == LayoutConstants.LEADING);
        String code;
        if (alignment == LayoutConstants.CENTER) {
            code = (horizontal ? "ch" : "cv"); // NOI18N
        } else {
            code = (horizontal ? (leading ? "l" : "r") : (leading ? "u" : "d")); // NOI18N
        }
        String iconResource = ICON_BASE + code + ".png"; // NOI18N
        putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon(iconResource, true));
        putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(AlignAction.class, "CTL_AlignAction_" + code)); // NOI18N
    }

    /**
     * Return true if c is descendant of p
     *
     * @param c
     * @param p
     * @return
     */
    private boolean isDescendant(Widget c, Widget p) {
        Widget maybeP = c.getParentWidget();
        if (maybeP == null) {
            //c has no parent
            return false;
        } else if (maybeP.equals(p)) {
            //found the parent
            return true;
        } else {
            return isDescendant(maybeP, p);
        }
    }

    /**
     * Filtering layer in lower level in tree hierarchy. For example, if the
     * tree hierarchy is as follows : Root has children A and B, A has children
     * A1 and A2, while B has only one child B1.
     *
     * In the case of A, A2 and B1 are selected, this function returns only the
     * set (A, B1) because A2 is a descendant of A.
     *
     * @param s Set of scene's selected LayoutLayer.
     * @return
     */
    private HashSet getRootLayers(Set s) {
        HashSet clone = new HashSet(s);
        HashSet clone2 = new HashSet(s);
        for (Object ll : clone) {
            Widget current = scene.findWidget(ll);
            //iterate to all element (except itself), check if the element 
            //visited is descendant of current element
            for (Iterator i2 = clone2.iterator(); i2.hasNext();) {
                Object ll2 = i2.next();
                Widget visited = scene.findWidget(ll2);
                if (isDescendant(visited, current)) {
                    i2.remove();
                }
            }
        }
        return clone2;
    }

    /**
     * Move ALL sublayoutlayer under this widget
     *
     * @param w GroupLayerWidget
     * @param deltaX
     * @param deltaY
     * @param edit
     */
    private void moveGroup(Widget w, int deltaX, int deltaY, CompoundEdit edit) {
        for (Widget c : w.getChildren()) {
            if (c instanceof SubLayoutWidget) {
                SubLayoutLayer sll = (SubLayoutLayer) scene.findObject(c);
                moveLayer(sll, deltaX, deltaY, edit);
            } else if (c instanceof GroupLayerWidget) {
                //recursive
                moveGroup(c, deltaX, deltaY, edit);
            }
        }
    }

    private void moveLayer(SubLayoutLayer sll, int deltaX, int deltaY, CompoundEdit edit) {
        if (deltaX != 0 || deltaY != 0) {
            SubLayoutMoveUndoableEdit smallEdit = new SubLayoutMoveUndoableEdit(sll, sll.getLocation(), new Point(sll.getX() + deltaX, sll.getY() + deltaY));
            edit.addEdit(smallEdit);
            if (deltaX != 0) {
                sll.setX(sll.getX() + deltaX);
            }
            if (deltaY != 0) {
                sll.setY(sll.getY() + deltaY);
            }
        }
    }

    /**
     * Align selected layers. Do nothing kalau common ancestor dari semua
     * selected layers is already selected.
     *
     * @param e event that invoked the action.
     * @see LayoutConstants
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        CompoundEdit edit = new CompoundEdit();
        //each of these objects is a layer
        HashSet filteredObjs = getRootLayers(scene.getSelectedObjects());
        if (dimension == LayoutConstants.HORIZONTAL) {
            if (alignment == LayoutConstants.LEADING) {
                //semua digeser sehingga rata kiri, ngikut yang paling kiri
                int xMinCandidate;
                int xMin = Integer.MAX_VALUE;
                for (Object o : filteredObjs) {
                    Widget w = scene.findWidget(o);
                    Rectangle area = WidgetUtil.findArea(w);
                    xMinCandidate = area.x;
                    if (xMin > xMinCandidate) {
                        xMin = xMinCandidate;
                    }
                }
                //loop lagi buat setX
                //setX ini dilakukan tidak hanya ke object di dalam filteredObject
                //maupun Scene's selected objects
                //namun ke semua widget yang affected
                //jadi misal ada group layer selected, namun anak2nya tidak selected
                //semua anak dari group layer tersebut ikut digeser
                //NOTE : group layer nya sendiri tidak digeser
                for (Object o : filteredObjs) {
                    Widget w = scene.findWidget(o);
                    Rectangle area = WidgetUtil.findArea(w);
                    int delta = xMin - area.x;
                    //we had delta, now we apply it to the widget(s)
                    if (w instanceof SubLayoutWidget) {
                        SubLayoutLayer sll = (SubLayoutLayer) o;
                        moveLayer(sll, delta, 0, edit);
                    } else if (w instanceof GroupLayerWidget) {
                        moveGroup(w, delta, 0, edit);
                    }
                }
            } else if (alignment == LayoutConstants.TRAILING) {
                //semua digeser sehingga rata kanan, ngikut yang paling kanan
                int xMaxCandidate;
                int xMax = Integer.MIN_VALUE;
                for (Object o : filteredObjs) {
                    Widget w = scene.findWidget(o);
                    Rectangle area = WidgetUtil.findArea(w);
                    xMaxCandidate = area.x + area.width;
                    if (xMax < xMaxCandidate) {
                        xMax = xMaxCandidate;
                    }
                }
                //loop lagi buat setX
                for (Object o : filteredObjs) {
                    Widget w = scene.findWidget(o);
                    Rectangle area = WidgetUtil.findArea(w);
                    int delta = xMax - (area.x + area.width);
                    if (w instanceof SubLayoutWidget) {
                        SubLayoutLayer sll = (SubLayoutLayer) o;
                        moveLayer(sll, delta, 0, edit);
                    } else if (w instanceof GroupLayerWidget) {
                        moveGroup(w, delta, 0, edit);
                    }
                }
            } else if (alignment == LayoutConstants.CENTER) {
                //semua digeser sehingga vertical axis nya sama
                int xMidSum = 0;
                int xMid;
                int layerNum = 0;
                for (Object o : filteredObjs) {
                    Widget w = scene.findWidget(o);
                    Rectangle area = WidgetUtil.findArea(w);
                    layerNum++;
                    xMid = area.width / 2 + area.x;
                    xMidSum += xMid;
                }
                //sudah ketahuan di mana mid yang seharusnya
                xMidSum = xMidSum / layerNum;
                //loop lagi buat setX
                for (Object o : filteredObjs) {
                    Widget w = scene.findWidget(o);
                    Rectangle area = WidgetUtil.findArea(w);
                    xMid = area.width / 2 + area.x;
                    int delta = xMidSum - xMid;
                    if (w instanceof SubLayoutWidget) {

                        SubLayoutLayer sll = (SubLayoutLayer) o;
                        moveLayer(sll, delta, 0, edit);
                    } else if (w instanceof GroupLayerWidget) {
                        moveGroup(w, delta, 0, edit);
                    }

                }
            }
        } else if (dimension == LayoutConstants.VERTICAL) {
            if (alignment == LayoutConstants.LEADING) {
                //semua digeser sehingga rata atas, ngikut yang paling atas
                int yMinCandidate;
                int yMin = Integer.MAX_VALUE;
                for (Object o : filteredObjs) {
                    Widget w = scene.findWidget(o);
                    Rectangle area = WidgetUtil.findArea(w);
                    yMinCandidate = area.y;
                    if (yMin > yMinCandidate) {
                        yMin = yMinCandidate;
                    }
                }
                //loop lagi buat setY
                for (Object o : filteredObjs) {
                    Widget w = scene.findWidget(o);
                    Rectangle area = WidgetUtil.findArea(w);
                    int delta = yMin - area.y;
                    if (w instanceof SubLayoutWidget) {
                        SubLayoutLayer sll = (SubLayoutLayer) o;
                        moveLayer(sll, 0, delta, edit);
                    } else if (w instanceof GroupLayerWidget) {
                        moveGroup(w, 0, delta, edit);
                    }
                }
            } else if (alignment == LayoutConstants.TRAILING) {
                //semua digeser sehingga rata bawah, ngikut yang paling bawah
                int yMaxCandidate;
                int yMax = Integer.MIN_VALUE;
                for (Object o : filteredObjs) {
                    Widget w = scene.findWidget(o);
                    Rectangle area = WidgetUtil.findArea(w);
                    yMaxCandidate = area.y + area.height;
                    if (yMax < yMaxCandidate) {
                        yMax = yMaxCandidate;
                    }
                }
                //loop lagi buat setY
                for (Object o : filteredObjs) {
                    Widget w = scene.findWidget(o);
                    Rectangle area = WidgetUtil.findArea(w);
                    int delta = yMax - (area.y + area.height);
                    if (w instanceof SubLayoutWidget) {
                        SubLayoutLayer sll = (SubLayoutLayer) o;
                        moveLayer(sll, 0, delta, edit);
                    } else if (w instanceof GroupLayerWidget) {
                        moveGroup(w, 0, delta, edit);
                    }
                }
            } else if (alignment == LayoutConstants.CENTER) {
                //semua digeser sehingga vertical axis nya sama
                int yMidSum = 0;
                int yMid;
                int layerNum = 0;
                for (Object o : filteredObjs) {
                    Widget w = scene.findWidget(o);
                    Rectangle area = WidgetUtil.findArea(w);
                    layerNum++;
                    yMid = area.height / 2 + area.y;
                    yMidSum += yMid;
                }
                //sudah ketahuan di mana mid yang seharusnya
                yMidSum = yMidSum / layerNum;
                //loop lagi buat setY
                for (Object o : filteredObjs) {
                    Widget w = scene.findWidget(o);
                    Rectangle area = WidgetUtil.findArea(w);
                    yMid = area.height / 2 + area.y;
                    int delta = yMidSum - yMid;
                    if (w instanceof SubLayoutWidget) {
                        SubLayoutLayer sll = (SubLayoutLayer) o;
                        moveLayer(sll, 0, delta, edit);
                    } else if (w instanceof GroupLayerWidget) {
                        moveGroup(w, 0, delta, edit);
                    }
                }
            }
        }
        //done adding small edits
        edit.end();
        GlobalUndoManager.getManager().undoableEditHappened(new UndoableEditEvent(scene, edit));
    }

    public int getDimension() {
        return dimension;
    }

    public int getAlignment() {
        return alignment;
    }

}
