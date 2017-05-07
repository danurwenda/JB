/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.core.api.actions;

import com.jbatik.core.api.SceneObserverCookie;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JColorChooser;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;

@ActionID(
        category = "View",
        id = "com.jbatik.core.api.action.ChangeBackgroundAction"
)
@ActionRegistration(
        displayName = "#CTL_ChangeBackgroundAction", lazy = true,
        iconBase = "com/jbatik/core/api/actions/brush.png"
)
@ActionReferences({
    @ActionReference(path = "Menu/View", position = 625),
    @ActionReference(path = "Toolbars/View", position = 200)
})
@Messages("CTL_ChangeBackgroundAction=Change Background Color")
public final class ChangeBackgroundAction implements ActionListener {

    private SceneObserverCookie context;

    public ChangeBackgroundAction(SceneObserverCookie context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        Color newColor = JColorChooser.showDialog(WindowManager.getDefault().getMainWindow(), "Choose Background Color", context.getBackgroundColor());
        if (newColor != null) {
            context.changeBackgroundColor(newColor);
        }
    }
}
