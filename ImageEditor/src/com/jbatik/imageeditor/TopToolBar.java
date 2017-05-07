/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.imageeditor;

import javax.swing.JToolBar;

/**
 *
 * @author RAPID02
 */
public class TopToolBar extends JToolBar {

    JToolBar toolToolbar;

    public void setToolToolbar(JToolBar tb) {
        if (toolToolbar != null) {
            //remove previous toolbar
            remove(toolToolbar);
        }
        if (tb != null) {
            toolToolbar = tb;
            add(tb);
        }
        revalidate();
        repaint();
    }
}
