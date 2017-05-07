/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.core.api;

import java.awt.Image;

/**
 *
 *
 * @author RAPID02
 */
public interface ExportAsPNG {

    /**
     * Default name for this resulting image
     *
     * @return default name, without extension.
     */
    public String getDefaultName();

    /**
     * Write an image file, given a full filepath and PNG exporting
     * configuration
     *
     * @param path Full path to the location of file image, extension <b>may
     * be</b> excluded
     * @param config Optional parameters.
     */
    public void writeImage(String path, PNGExportConfiguration config);

    public Image getPreviewImage(int w, int h, boolean withBackground);
}
