/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.library.palette;

import org.netbeans.spi.palette.PaletteController;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;

/**
 * Bikin kelas ini hanya agar bisa hide item names tapi sekaligus items nya searchable
 * @author danur
 */
class PaletteRoot extends AbstractNode {

    @Override
    public Object getValue(String attributeName) {

        if (attributeName.equals(PaletteController.ATTR_SHOW_ITEM_NAMES)) {
            return false;
        }

        return super.getValue(attributeName); //To change body of generated methods, choose Tools | Templates.
    }

    public PaletteRoot(Children children) {
        super(children);
    }
}
