/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.layering.actions;

import com.jbatik.core.api.GlobalUndoManager;
import com.jbatik.modules.layout.explorer.node.AddLayerUndoableEdit;
import com.jbatik.modules.layout.explorer.node.LayerList;
import com.jbatik.modules.layout.explorer.node.LayoutLayerNode;
import com.jbatik.modules.layout.layering.LayoutLayer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.UndoableEditEvent;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Edit",
        id = "com.jbatik.modules.layout.layering.actions.CloneAction"
)
@ActionRegistration(
        displayName = "#CTL_CloneAction"
)
@ActionReference(path = "LayoutLayer/Action", position = 110, separatorAfter = 120)
@Messages("CTL_CloneAction=Clone")
public final class CloneAction implements ActionListener {

    private final LayoutLayerNode originalNode;

    public CloneAction(LayoutLayerNode context) {
        this.originalNode = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        try {
            LayerList layerOfSiblings = originalNode.getModel();
            LayoutLayer clone = originalNode.getLayer().clone();
            AddLayerUndoableEdit edit = new AddLayerUndoableEdit(clone, layerOfSiblings);
            GlobalUndoManager.getManager().undoableEditHappened(
                    new UndoableEditEvent(originalNode, edit));
            layerOfSiblings.addFirst(clone);
        } catch (CloneNotSupportedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
