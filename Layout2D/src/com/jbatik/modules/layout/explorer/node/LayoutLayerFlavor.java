/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.explorer.node;

import com.jbatik.modules.layout.layering.LayoutLayer;
import java.awt.datatransfer.DataFlavor;

/**
 *
 * @author RAPID02
 */
public class LayoutLayerFlavor extends DataFlavor {

    public static final DataFlavor LAYOUT_LAYER_FLAVOR = new LayoutLayerFlavor();

    public LayoutLayerFlavor() {
        super(LayoutLayer.class, "LayoutLayer");
    }
}
