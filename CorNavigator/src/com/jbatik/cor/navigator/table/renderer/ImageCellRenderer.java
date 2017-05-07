/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.cor.navigator.table.renderer;

import com.jbatik.util.ImageUtil;
import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import org.netbeans.api.annotations.common.StaticResource;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Nanta Es <kedipkedip@ymail.com>
 */
public class ImageCellRenderer extends DefaultTableCellRenderer {

    private JButton button = new JButton();
    @StaticResource
    private static final String MISSING_ICON = "com/jbatik/cor/navigator/table/renderer/missing.png";

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {

        ImageIcon icon;
        File f = (File) value;
        if (f.exists()) {
            button.setBackground(Color.WHITE);
            icon = new ImageIcon(f.getPath());
        } else {
            icon = ImageUtilities.loadImageIcon(MISSING_ICON, false);
            button.setBackground(Color.RED);
        }
        Image img = icon.getImage();

        button.setIcon(new ImageIcon(ImageUtil.getScaledImage(img, 200, 80)));
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorderPainted(false);
        return button;
    }
}
