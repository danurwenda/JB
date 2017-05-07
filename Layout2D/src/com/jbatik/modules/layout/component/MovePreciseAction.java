/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.component;

import com.jbatik.core.api.GlobalUndoManager;
import com.jbatik.modules.layout.layering.SubLayoutLayer;
import com.jbatik.modules.layout.visual.actions.SubLayoutMoveUndoableEdit;
import com.jbatik.modules.layout.visual.widgets.SubLayoutWidget;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.CompoundEdit;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Widget;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Menggeser semua selected layer by specified dx & dy
 *
 * @author danur
 */
class MovePreciseAction extends AbstractAction {

    private ObjectScene scene;
    private HashMap<Widget, Point> originals = new HashMap<>();
    // PENDING change to icons provided by Dusan
    private static final String ICON_BASE = "com/jbatik/modules/layout/component/resources/movep.png"; // NOI18N

    public MovePreciseAction(ObjectScene scene) {
        this.scene = scene;
        putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, true));
        putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(MovePreciseAction.class, "CTL_MovePreciseAction")); // NOI18N
    }
    private CompoundEdit edit;

    @Override
    public void actionPerformed(ActionEvent e) {
        MovePrecisePanel box = new MovePrecisePanel();
        Object result
                = DialogDisplayer.getDefault().notify(
                        new DialogDescriptor(
                                box, "Move Layers")
                );

        if (result != NotifyDescriptor.OK_OPTION) {
            return;
        }
        int dx = box.getDx();
        int dy = box.getDy();
        if (dx != 0 || dy != 0) {
            //ready to move
            //create undoableedit
            if (edit == null || edit.isInProgress() == false) {
                edit = new CompoundEdit();
            }
            for (Object o : scene.getSelectedObjects()) {
                Widget w = scene.findWidget(o);
                if (w != null && w instanceof SubLayoutWidget && w.isEnabled()) {
                    Point oriP = w.getPreferredLocation();
                    Point newP = new Point(oriP.x + dx, oriP.y + dy);
                    SubLayoutLayer sl = ((SubLayoutLayer) scene.findObject(w));
                    SubLayoutMoveUndoableEdit smallEdit = new SubLayoutMoveUndoableEdit(sl, oriP, newP);
                    edit.addEdit(smallEdit);
                    sl.setLocation(newP);
                }
            }
            //done adding small edits
            edit.end();
            GlobalUndoManager.getManager().undoableEditHappened(new UndoableEditEvent(scene, edit));

        }

    }

}
