/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.library.palette;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.spi.palette.PaletteController;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataNode;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;

/**
 *
 * @author RAPID02
 */
public class LibraryPNGCategoryNode extends FilterNode {

    public LibraryPNGCategoryNode(Node libDirNode) {
        super(
                libDirNode,
                new LibraryFilterChildren(libDirNode));
    }

    @Override
    public Object getValue(String attributeName) {
        if (attributeName.equals(PaletteController.ATTR_IS_EXPANDED)) {
            return true;
        }
        return super.getValue(attributeName); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getDisplayName() {
        return "PNG Libraries";
    }

    private static class LibraryFilterChildren extends FilterNode.Children {

        private FileObject dir;

        public LibraryFilterChildren(Node libDirNode) {
            super(libDirNode);
            DataNode fn = (DataNode) libDirNode;
            dir = fn.getDataObject().getPrimaryFile();
        }

        @Override
        protected Node[] createNodes(Node k) {
            List<Node> result = new ArrayList<>();
            for (Node node : super.createNodes(k)) {
                //filter to .png files only
                if (node.getDisplayName().toLowerCase().endsWith(".png")) {
                    //TODO : additional check to test whether this ".png" file is really a valid image file
                    result.add(
                            new PNGLibNode(dir, node)
                    );
                }
            }
            return result.toArray(new Node[0]);
        }
    }
}
