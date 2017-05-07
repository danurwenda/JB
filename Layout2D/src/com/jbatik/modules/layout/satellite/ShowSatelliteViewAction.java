/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.satellite;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Open layout explorer manually from menu item. If the explorer is opened
 * manually, it will always shown until it's closed manually.
 *
 * @author RAPID02
 */
@ActionID(
        category = "Window",
        id = "com.jbatik.modules.layout.explorer.ShowSatelliteViewAction"
)
@ActionRegistration(
        iconBase = "com/jbatik/modules/layout/satellite/satelit.png",
        displayName = "#CTL_ShowSatelliteViewAction"
)
@ActionReference(path = "Menu/Window", position = 150)
@Messages("CTL_ShowSatelliteViewAction=Satellite View")
public final class ShowSatelliteViewAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        // SHOW layer explorer
        TopComponent palette = WindowManager.getDefault().findTopComponent("SatelliteViewTopComponent"); // NOI18N
        if (null == palette) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, "Cannot find Satellite View component."); // NOI18N

            return;
        }
        Utils.setOpenedByUser(palette, true);
        palette.open();
        palette.requestActive();
    }
}
