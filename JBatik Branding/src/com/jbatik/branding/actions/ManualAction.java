/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.branding.actions;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * Add OpenIDE-Module-Install: com/jbatik/core/timer/Installer.class
 *
 * @author RAPID02
 */
@ActionID(
        category = "Help",
        id = "com.jbatik.branding.actions.ManualAction")
@ActionRegistration(
        displayName = "#CTL_ManualAction")
@ActionReferences({
    @ActionReference(path = "Menu/Help", position = 100),
    @ActionReference(path = "Shortcuts", name = "F1")
})
@Messages("CTL_ManualAction=Manual")
public final class ManualAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        if (Desktop.isDesktopSupported()) {
            try {
                // the file itself is created by module installer
                // com/jbatik/branding/Installer.java
                File temp = new File("jBatik4-Pro-Manual.pdf");
                Desktop.getDesktop().open(temp);
            } catch (IOException ex) {
                System.out.println("NO PDF READER INSTALLED");
            }
        }
    }
}
