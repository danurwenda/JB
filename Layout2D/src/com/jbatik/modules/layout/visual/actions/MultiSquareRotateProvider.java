/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.visual.actions;

import com.jbatik.canvas.visual.actions.RotateProvider;
import com.jbatik.core.api.GlobalUndoManager;
import com.jbatik.modules.layout.layering.SubLayoutLayer;
import com.jbatik.modules.layout.visual.widgets.SquareWidget;
import com.jbatik.modules.layout.visual.widgets.SubLayoutWidget;
import java.awt.Point;
import java.util.HashMap;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author RAPID02
 */
public class MultiSquareRotateProvider implements RotateProvider {

    /**
     * Original sqRot of each (selected) slw
     */
    private HashMap<SubLayoutWidget, Float> originals = new HashMap<>();
    private Float original;
    private ObjectScene scene;

    public MultiSquareRotateProvider(ObjectScene s) {
        this.scene = s;
    }

    @Override
    public void rotateStarted(Widget widget) {
        //make sure it's clean
        originals.clear();
        Object object = scene.findObject(widget);
        if (object != null) {
            for (Object o : scene.getSelectedObjects()) {
                Widget w = scene.findWidget(o);
                if (w != null && w instanceof SubLayoutWidget) {
                    SubLayoutWidget slw = (SubLayoutWidget) w;
                    originals.put(slw, slw.getSublayout().getSquareRotationAngle());
                }
            }
        }
    }

    private CompoundEdit edit;

    @Override
    public void rotateFinished(Widget widget) {
        //check whether a change has actually been happened
        SubLayoutWidget slw = (SubLayoutWidget) widget;
        if (!original.equals(slw.getSublayout().getSquareRotationAngle())) {
            if (edit == null || edit.isInProgress() == false) {
                edit = new CompoundEdit();
            }
            originals.entrySet().stream().forEach((entry) -> {
                Float oriSqRot = entry.getValue();
                SubLayoutWidget w = entry.getKey();
                Float newSqRot = w.getSublayout().getSquareRotationAngle();
                SubLayoutSquareRotationUndoableEdit smallEdit = new SubLayoutSquareRotationUndoableEdit(w, oriSqRot, newSqRot);
                edit.addEdit(smallEdit);
            });
            //done adding small edits
            edit.end();
            GlobalUndoManager.getManager().undoableEditHappened(new UndoableEditEvent(scene, edit));
//            scene.updateSelectionWrapper();
        }
        original = null;
    }

    /**
     * Di sini, w adalah sublayoutwidget, p adalah event.getpoint
     *
     * @param w
     * @param p
     * @return
     */
    @Override
    public Point getAnchorPointOnScene(Widget widget, Point point) {
        SubLayoutWidget slw = (SubLayoutWidget) widget;
        original = slw.getSublayout().getSquareRotationAngle();
        SquareWidget hitSquare = slw.hitSquare(point);
        return hitSquare.convertLocalToScene(hitSquare.getCorner());
    }

    void updateSqRot(Widget widget, float sqrot) {
        SubLayoutLayer sl = (SubLayoutLayer) scene.findObject(widget);
        sl.setSquareRotationAngle(sqrot);
    }

    @Override
    public void applyRotation(Widget w, double d) {
        originals.entrySet().stream().forEach((entry) -> {
            updateSqRot(entry.getKey(), (float) (d + entry.getValue()));
        });
    }

    class SubLayoutSquareRotationUndoableEdit extends AbstractUndoableEdit {

        private float originalSqRot;
        private float newSqRot;
        private Widget widget;

        private SubLayoutSquareRotationUndoableEdit(SubLayoutWidget w, Float oriSqRot, Float newSqRot) {
            this.widget = w;
            this.originalSqRot = oriSqRot;
            this.newSqRot = newSqRot;
        }

        @Override
        public String getPresentationName() {
            return "Rotate Square";
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            updateSqRot(widget, newSqRot);
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            updateSqRot(widget, originalSqRot);
        }

    }

}
