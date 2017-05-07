/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.layering.actions;

import com.jbatik.core.api.GlobalUndoManager;
import com.jbatik.modules.layout.layering.GroupLayer;
import com.jbatik.modules.layout.layering.LayoutLayer;
import com.jbatik.modules.layout.layering.SubLayoutLayer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.CompoundEdit;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Edit",
        id = "com.jbatik.modules.layout.layering.actions.ResetAction"
)
@ActionRegistration(
        displayName = "#CTL_ResetAction"
)
@ActionReference(path = "LayoutLayer/Action", position = 10, separatorAfter = 20)
@Messages("CTL_ResetAction=Reset")
public final class ResetAction implements ActionListener {

    private final List<LayoutLayer> context;

    public ResetAction(List<LayoutLayer> layers) {
        this.context = layers;
    }

    private CompoundEdit edit;

    @Override
    public void actionPerformed(ActionEvent ev) {
        if (edit == null || edit.isInProgress() == false) {
            edit = new CompoundEdit();
        }
        for (LayoutLayer layoutLayer : context) {
            reset(layoutLayer);
        }
        //done adding small edits
        edit.end();
        GlobalUndoManager.getManager().undoableEditHappened(new UndoableEditEvent(context, edit));
    }

    private void reset(LayoutLayer layoutLayer) {
        if (layoutLayer instanceof GroupLayer) {
            GroupLayer groupLayer = (GroupLayer) layoutLayer;
            for (LayoutLayer ll : groupLayer.getModel().list()) {
                reset(ll);
            }
        } else if (layoutLayer instanceof SubLayoutLayer) {
            //add small edits
            SubLayoutLayer sll = (SubLayoutLayer) layoutLayer;
            ResetMappingUndoableEdit smallEdit = new ResetMappingUndoableEdit(sll);
            edit.addEdit(smallEdit);
            sll.resetMapping();
        }
    }
}
