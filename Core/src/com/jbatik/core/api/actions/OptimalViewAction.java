package com.jbatik.core.api.actions;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.jbatik.core.api.FitView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "View",
        id = "com.jbatik.core.api.actions.OptimalViewAction"
)
@ActionRegistration(
        displayName = "#CTL_CameraDefaultAction",
        iconBase = "com/jbatik/core/api/actions/stf.png"
)
@ActionReferences({
    @ActionReference(path = "Menu/View", position = 662),
    @ActionReference(path = "Toolbars/View", position = 100)
})
@Messages("CTL_CameraDefaultAction=Optimal View")
public final class OptimalViewAction implements ActionListener {

    private FitView context;

    public OptimalViewAction(FitView context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        context.optimalView();
    }
}
