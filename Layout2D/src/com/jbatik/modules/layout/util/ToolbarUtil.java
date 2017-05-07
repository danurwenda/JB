/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.util;

import java.io.IOException;
import org.openide.awt.Toolbar;
import org.openide.awt.ToolbarPool;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataShadow;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author RAPID02
 */
public class ToolbarUtil {

    public static boolean registerToolbar(String toolbarConfigPath) {
        try {
            FileObject fo = FileUtil.getConfigFile(toolbarConfigPath);
            if (fo == null) {
                return false;
            }
            DataFolder df = DataFolder.findFolder(fo);
            DataFolder target = ToolbarPool.getDefault().getFolder();
            FileObject targetFO = target.getPrimaryFile().getFileObject(fo.getNameExt() + ".shadow");

            if (df != null && targetFO == null) {
                DataShadow ds = df.createShadow(target);
                return true;
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }

    public static void setToolbarVisible(Toolbar toolbar, boolean visible) {
        try {
            ClassLoader cl = Lookup.getDefault().lookup(ClassLoader.class);
            Class cToolbarConfiguration = cl.loadClass("org.netbeans.core.windows.view.ui.toolbars.ToolbarConfiguration");
            // invoke static ToolbarConfiguration.findConfiguration( String name)
            Object toolbarConfig = cToolbarConfiguration.getMethod("findConfiguration", String.class).
                    invoke(cToolbarConfiguration, "Standard");
            // invoke ToolbarConfiguration#setToolbarVisible( Toolbar tb, boolean visible)
            toolbarConfig.getClass().getMethod("setToolbarVisible", Toolbar.class, boolean.class).invoke(toolbarConfig, toolbar, visible);
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }
}
