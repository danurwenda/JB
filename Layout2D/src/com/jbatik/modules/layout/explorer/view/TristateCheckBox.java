/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.explorer.view;

import java.awt.Graphics;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import org.netbeans.api.annotations.common.StaticResource;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Dimas Y. Danurwenda
 */
public class TristateCheckBox extends JCheckBox {

    private static final long serialVersionUID = 1L;
    private boolean halfState;
    @StaticResource
    private static final String UNS_ICON_PATH = "com/jbatik/modules/layout/explorer/view/close.png";
    private static final Icon unselected = ImageUtilities.loadImageIcon(UNS_ICON_PATH, false);
    @StaticResource
    private static final String SEL_ICON_PATH = "com/jbatik/modules/layout/explorer/view/open.png";
    private static final Icon selected = ImageUtilities.loadImageIcon(SEL_ICON_PATH, false);
    @StaticResource
    private static final String MIX_ICON_PATH = "com/jbatik/modules/layout/explorer/view/hide.png";
    private static final Icon halfselected = ImageUtilities.loadImageIcon(MIX_ICON_PATH, false);

    @Override
    public void paint(Graphics g) {
        if (isSelected()) {
            halfState = false;
        }
        setIcon(halfState ? halfselected : isSelected() ? selected : unselected);
        super.paint(g);
    }

    public boolean isHalfSelected() {
        return halfState;
    }

    public void setHalfSelected(boolean halfState) {
        this.halfState = halfState;
        if (halfState) {
            setSelected(false);
            repaint();
        }
    }
}
