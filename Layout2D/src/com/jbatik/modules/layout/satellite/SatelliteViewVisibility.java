/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.satellite;

import com.jbatik.modules.layout.visual.LayoutScene;

/**
 *
 * @author RAPID02
 */
class SatelliteViewVisibility {

    /**
     * A three state condition of Explorer visibility. True means user has
     * opened it manually thru menu action or shortcut. False means the window
     * was shown by switcher, but user close it manually. Null means show/hide
     * is fully controlled by switcher.
     */
    static Boolean manual;

    static boolean getVisibility(LayoutScene newLayer) {
        if (newLayer != null) {
            //layer is not null, normally we set explorer to be visible
            //unless explorer was set to close manually
            return !Boolean.FALSE.equals(manual);
        } else {
            //layer is null, normally we set explorer to be hidden
            //unless it was opened manually
            return Boolean.TRUE.equals(manual);
        }
    }

    /**
     * If we're asked to close the explorer while the controller is not null,
     * that means user has just closed it manually.
     *
     * If we're asked to close the explorer when the controller is null, then
     * why is it open in the first time? it must've been opened manually.
     *
     * @param pc
     * @param open
     */
    static void setVisible(LayoutScene pc, boolean open) {
        if (pc != null && !open) {
            //it was closed manually
            manual = false;
        }
        if (pc != null && open) {
            //it was opened manually after closed manually
            manual = null;
        }
        if (pc == null && open) {
            //it was opened manually when it should not be
            manual = true;
        }
        if (pc == null && !open) {
            //it was closed manually after opened manually
            manual = null;
        }
    }

}
