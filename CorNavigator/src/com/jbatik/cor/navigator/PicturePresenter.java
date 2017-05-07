/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.cor.navigator;

import java.awt.Dimension;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import org.netbeans.api.annotations.common.StaticResource;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Nanta Es <kedipkedip@ymail.com>
 */
public class PicturePresenter {

    @StaticResource
    private static final String ACTION_REMOVE = "com/jbatik/cor/navigator/action_remove.png";

    public static void setPicture_Remove(JButton _jButton) {
        setButton(_jButton);
        _jButton.setIcon(ImageUtilities.loadImageIcon(ACTION_REMOVE, false));
    }
    
    @StaticResource
    private static final String ACTION_ADD = "com/jbatik/cor/navigator/action_add.png";

    public static void setPicture_Add(JButton _jButton) {
        setButton(_jButton);
        _jButton.setIcon(ImageUtilities.loadImageIcon(ACTION_ADD, false));
    }

    public static ImageIcon getDefaultPicture() {
        return ImageUtilities.loadImageIcon(ACTION_ADD, false);
    }

    private static void setButton(JButton _jButton) {
        _jButton.setText("");
        _jButton.setMaximumSize(new Dimension(30, 30));
        _jButton.setMinimumSize(new Dimension(30, 30));
        _jButton.setPreferredSize(new Dimension(30, 30));
    }
}
