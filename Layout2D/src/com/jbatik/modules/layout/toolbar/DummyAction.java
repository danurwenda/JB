/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.toolbar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Dummy",
        id = "com.jbatik.modules.layout.toolbar.DummyAction"
)
@ActionRegistration(
        iconBase = "com/jbatik/modules/layout/toolbar/d.png",
        displayName = "#CTL_DummyAction"
)
@Messages("CTL_DummyAction=Dummy Action")
public final class DummyAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        System.err.println("dummy action calling");
    }
}
