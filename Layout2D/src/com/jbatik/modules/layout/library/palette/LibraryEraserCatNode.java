/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.library.palette;

import org.netbeans.spi.palette.PaletteController;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author RAPID02
 */
class LibraryEraserCatNode extends AbstractNode {

    public LibraryEraserCatNode() {
        super(new Children.Array());
        getChildren().add(new Node[]{new EraserNode()});
    }

    @Override
    public String getDisplayName() {
        return "Eraser";
    }

    @Override
    public Object getValue(String attributeName) {
        if (attributeName.equals(PaletteController.ATTR_IS_EXPANDED)) {
            return true;
        }
        return super.getValue(attributeName); //To change body of generated methods, choose Tools | Templates.
    }
}
