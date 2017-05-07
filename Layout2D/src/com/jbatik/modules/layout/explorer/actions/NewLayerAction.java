/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.explorer.actions;

import com.jbatik.modules.layout.explorer.LayerController;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "LayoutExplorer",
        id = "com.jbatik.modules.layout.explorer.actions.NewLayerAction"
)
@ActionRegistration(
        displayName = "#CTL_NewLayerAction"
)
@ActionReference(path = "LayoutLayer/Action", position = 200)
@Messages("CTL_NewLayerAction=New Layer")
public final class NewLayerAction implements ActionListener {

    private final LayerController context;

    public NewLayerAction(LayerController context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        //add a sublayout layer as the first (topmost) layer
        context.addNewLayer();
    }
}
