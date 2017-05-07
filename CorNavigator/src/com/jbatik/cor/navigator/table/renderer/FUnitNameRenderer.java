/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jbatik.cor.navigator.table.renderer;

import com.jbatik.lsystem.turtle.Surface;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author risman
 */
public class FUnitNameRenderer extends JLabel implements TableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        this.setHorizontalAlignment(SwingConstants.CENTER);
        this.setHorizontalTextPosition(SwingConstants.CENTER);
        setText(((Surface) value).getName());
        return this;
    }
    
}
