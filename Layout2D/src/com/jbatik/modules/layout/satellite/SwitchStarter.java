/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.satellite;

import java.awt.EventQueue;
import org.openide.windows.OnShowing;

/**
 *
 * @author RAPID02
 */
@OnShowing
public class SwitchStarter implements Runnable {

    @Override
    public void run() {
        assert EventQueue.isDispatchThread();

        //start listening to activated TopComponents and Nodes 
        //to see if layer explorer window should be displayed
        SatelliteViewSwitch.getDefault().startListening();
    }

}
