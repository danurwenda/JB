/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.imageeditor;

/**
 *
 * @author Dimas Y. Danurwenda
 */
public class ImageLibEditorRegistry {

    private static ImageLibEditorTopComponent tc;

    public static ImageLibEditorTopComponent getActiveTC() {
        return tc;
    }

    public static void setActiveTC(ImageLibEditorTopComponent t) {
        tc = t;
    }

}
