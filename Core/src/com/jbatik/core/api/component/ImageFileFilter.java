/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.core.api.component;

import java.io.File;
import javax.swing.filechooser.FileFilter;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Dimas Y. Danurwenda
 */
public class ImageFileFilter extends FileFilter {

    public final static String jpeg = "jpeg";
    public final static String jpg = "jpg";
    public final static String gif = "gif";
    public final static String tiff = "tiff";
    public final static String tif = "tif";
    public final static String png = "png";

    //Accept all directories and all gif, jpg, tiff, or png files.

    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension = FileUtil.getExtension(f.getName());
        if (extension != null) {
            return extension.equals(tiff)
                    || extension.equals(tif)
                    || extension.equals(gif)
                    || extension.equals(jpeg)
                    || extension.equals(jpg)
                    || extension.equals(png);
        }

        return false;
    }

    //The description of this filter
    @Override
    public String getDescription() {
        return "Just Images";
    }
}

