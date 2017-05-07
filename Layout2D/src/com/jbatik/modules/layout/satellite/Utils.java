/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.satellite;

import org.openide.windows.TopComponent;

/**
 *
 * @author RAPID02
 */
public final class Utils {

    public static void setOpenedByUser(TopComponent tc, boolean userOpened) {
        tc.putClientProperty("userOpened", userOpened); //NOI18N
    }

    public static boolean isOpenedByUser(TopComponent tc) {
        Object val = tc.getClientProperty("userOpened");
//        tc.putClientProperty("userOpened", null);
        return null != val && val instanceof Boolean && ((Boolean) val);
    }
}
