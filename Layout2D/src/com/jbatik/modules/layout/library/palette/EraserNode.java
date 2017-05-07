/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.library.palette;

import java.awt.Image;
import org.netbeans.api.annotations.common.StaticResource;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;

/**
 *
 * @author RAPID02
 */
public class EraserNode extends AbstractNode {

    public EraserNode() {
        super(Children.LEAF);
        setShortDescription("Eraser");
    }

    @StaticResource
    private final String ERASER_ICON = "com/jbatik/modules/layout/library/palette/resources/eraser80.png";
    private final Image eraserIcon = ImageUtilities.loadImage(ERASER_ICON);

    @Override
    public Image getIcon(int t) {
        return eraserIcon;
    }

}
