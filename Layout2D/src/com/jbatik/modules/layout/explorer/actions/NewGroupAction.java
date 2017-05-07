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
        id = "com.jbatik.modules.layout.explorer.actions.NewGroupAction"
)
@ActionRegistration(
        displayName = "#CTL_NewGroupAction"
)
@ActionReference(path = "LayoutLayer/Action", position = 100)
@Messages("CTL_NewGroupAction=New Group")
public final class NewGroupAction implements ActionListener {

    private final LayerController context;

    public NewGroupAction(LayerController context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        //add a group layer as the first (topmost) layer
        context.addNewGroup();
    }
}
