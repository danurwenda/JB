/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jbatik.cor.navigator.table.renderer;

import com.jbatik.lsystem.turtle.Surface;
import java.awt.Color;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author risman
 */
public class FUnitPreviewRenderer extends DefaultTableCellRenderer {
    
    JButton button = new JButton();
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {
        Icon icon = ((Surface) value).getPreviewIcon(80);
        button.setIcon(icon);
        button.setBackground(Color.BLACK);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        return button;
    }
    
}
