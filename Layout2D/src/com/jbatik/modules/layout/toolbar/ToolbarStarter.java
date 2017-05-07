/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.toolbar;

import org.openide.windows.OnShowing;

/**
 *
 * @author RAPID02
 */
@OnShowing
public class ToolbarStarter implements Runnable {

    private static final String toolbarName = "MyToolbar";

    @Override
    public void run() {
//        ToolbarUtil.registerToolbar("MyModule/Toolbars/" + toolbarName);
    }

}
