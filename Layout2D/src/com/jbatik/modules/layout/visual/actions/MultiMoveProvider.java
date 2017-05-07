/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.visual.actions;

import com.jbatik.core.api.GlobalUndoManager;
import com.jbatik.modules.layout.layering.SubLayoutLayer;
import com.jbatik.modules.layout.visual.LayoutScene;
import com.jbatik.modules.layout.visual.widgets.SubLayoutWidget;
import java.awt.Point;
import java.util.HashMap;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.CompoundEdit;
import org.netbeans.api.visual.action.MoveProvider;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author RAPID02
 */
public class MultiMoveProvider implements MoveProvider {

    private HashMap<Widget, Point> originals = new HashMap<>();
    private Point original;
    private LayoutScene scene;

    public MultiMoveProvider(LayoutScene s) {
        this.scene = s;
    }

    @Override
    public void movementStarted(Widget widget) {
        Object object = scene.findObject(widget);
        if (object != null) {
            for (Object o : scene.getSelectedObjects()) {
                Widget w = scene.findWidget(o);
                if (w != null && w instanceof SubLayoutWidget && w.isEnabled()) {
                    originals.put(w, w.getPreferredLocation());
                }
            }
        } else {
            originals.put(widget, widget.getPreferredLocation());
        }
    }

    private CompoundEdit edit;

    @Override
    public void movementFinished(Widget widget) {
        if (!original.equals(widget.getPreferredLocation())) {
            if (edit == null || edit.isInProgress() == false) {
                edit = new CompoundEdit();
            }
            originals.entrySet().stream().forEach((entry) -> {
                Point oriP = entry.getValue();
                Widget w = entry.getKey();
                Point newP = w.getPreferredLocation();
                SubLayoutLayer sl = ((SubLayoutLayer) scene.findObject(w));
                SubLayoutMoveUndoableEdit smallEdit = new SubLayoutMoveUndoableEdit(sl, oriP, newP);
                edit.addEdit(smallEdit);
                sl.setLocation(newP);
            });
            //done adding small edits
            edit.end();
            GlobalUndoManager.getManager().undoableEditHappened(new UndoableEditEvent(scene, edit));
            scene.updateSelectionWrapper();
        }
        originals.clear();
        original = null;
    }

    @Override
    public Point getOriginalLocation(Widget widget) {
        original = widget.getPreferredLocation();
        return original;
    }

    @Override
    public void setNewLocation(Widget widget, Point location) {
        int dx = location.x - original.x;
        int dy = location.y - original.y;
        originals.entrySet().stream().forEach((entry) -> {
            Point point = entry.getValue();
            entry.getKey().setPreferredLocation(new Point(point.x + dx, point.y + dy));
        });
    }
}
