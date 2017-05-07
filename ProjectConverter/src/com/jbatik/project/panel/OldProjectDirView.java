/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.project.panel;

import java.io.File;
import javax.swing.Icon;
import javax.swing.filechooser.FileView;
import org.netbeans.api.annotations.common.StaticResource;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Dimas Y. Danurwenda
 */
public class OldProjectDirView extends FileView {

    @StaticResource
    private static final String oldIcon = "com/jbatik/project/panel/project-icon.png";
    private static final Icon icon = ImageUtilities.loadImageIcon(oldIcon, false);

    @Override
    public Icon getIcon(File f) {
        if (f.isDirectory() && isOldProject(f)) {
            //give special icon
            return icon;
        }
        return super.getIcon(f);
    }

    private boolean isOldProject(File f) {
        //check whether its a jBatik3 directory
        FileObject projectDirectory = FileUtil.toFileObject(f);
        if (projectDirectory != null) {
            boolean b = projectDirectory.getFileObject("layout2D") != null;
            if (!b) {
                b = projectDirectory.getFileObject("corak3D") != null;
            }

            return b;
        }
        return false;
    }

}
