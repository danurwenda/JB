/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.core.api.component;

import java.io.File;
import javax.swing.filechooser.FileView;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Dimas Y. Danurwenda
 */
public class ImageFileView extends FileView {

    @Override
    public String getName(File f) {
        return null; //let the L&F FileView figure this out
    }

    @Override
    public String getDescription(File f) {
        return null; //let the L&F FileView figure this out
    }

    @Override
    public Boolean isTraversable(File f) {
        return null; //let the L&F FileView figure this out
    }

    @Override
    public String getTypeDescription(File f) {
        String extension = FileUtil.getExtension(f.getName());
        String type = null;

        if (extension != null) {
            switch (extension) {
                case ImageFileFilter.jpeg:
                case ImageFileFilter.jpg:
                    type = "JPEG Image";
                    break;
                case ImageFileFilter.gif:
                    type = "GIF Image";
                    break;
                case ImageFileFilter.tiff:
                case ImageFileFilter.tif:
                    type = "TIFF Image";
                    break;
                case ImageFileFilter.png:
                    type = "PNG Image";
                    break;
            }
        }
        return type;
    }

}

