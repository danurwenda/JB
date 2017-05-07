/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.layering.actions;

import com.jbatik.core.api.GlobalUndoManager;
import com.jbatik.modules.layout.explorer.node.LayerList;
import com.jbatik.modules.layout.explorer.node.LayoutLayerNode;
import com.jbatik.modules.layout.explorer.node.ReorderIndexUndoableEdit;
import com.jbatik.modules.layout.layering.LayoutLayer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.CompoundEdit;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Edit",
        id = "com.jbatik.modules.layout.layering.actions.ToBackAction"
)
@ActionRegistration(
        displayName = "#CTL_ToBackAction"
)
@Messages("CTL_ToBackAction=Send To Back")
public final class ToBackAction implements ActionListener {

    private final List<LayoutLayerNode> context;

    public ToBackAction(List<LayoutLayerNode> layers) {
        this.context = layers;
    }

    private CompoundEdit edit;

    @Override
    public void actionPerformed(ActionEvent ev) {
        if (edit == null || edit.isInProgress() == false) {
            edit = new CompoundEdit();
        }
        for (LayoutLayerNode node : context) {
            toBack(node);
        }
        //done adding small edits
        edit.end();
        GlobalUndoManager.getManager().undoableEditHappened(new UndoableEditEvent(context, edit));
    }

    /**
     * Send it to the last element in Layer.model
     *
     * @param n
     */
    private void toBack(LayoutLayerNode n) {
        LayerList childrenModel = n.getModel();
        LayoutLayer layer = n.getLayer();
        int x = childrenModel.list().indexOf(layer);
        int total = childrenModel.list().size();
        int y = total - 1;

        int[] perm = new int[total];

        // if the positions are the same then no move
        if (x == y) {
            return;
        }

        for (int i = 0; i < perm.length; i++) {
            if (((i < x) && (i < y)) || ((i > x) && (i > y))) {
                // this area w/o change
                perm[i] = i;
            } else {
                if ((i > x) && (i < y)) {
                    // i-th element moves backward
                    perm[i] = i - 1;
                } else {
                    // i-th element moves forward
                    perm[i] = i + 1;
                }
            }
        }

        // set new positions for the elemets on x-th and y-th position
        perm[x] = y;

        if (x < y) {
            perm[y] = y - 1;
        } else {
            perm[y] = y + 1;
        }
        ReorderIndexUndoableEdit small = new ReorderIndexUndoableEdit(perm, childrenModel);
        edit.addEdit(small);
        childrenModel.reorder(perm);
    }
}
