/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.core.api.component;

import java.io.File;
import javax.swing.JFileChooser;

/**
 *
 * @author Dimas Y. Danurwenda
 */
public class ImageFileChooser extends JFileChooser {

    public ImageFileChooser(File currentDirectory) {
        super(currentDirectory);
        //Add a custom file filter and disable the default
        //(Accept All) file filter.
        addChoosableFileFilter(new ImageFileFilter());
        setAcceptAllFileFilterUsed(false);
        setMultiSelectionEnabled(true);
        //Add custom icons for file types.
        setFileView(new ImageFileView());

        //Add the preview pane.
        setAccessory(new ImagePreview(this));
    }

}
