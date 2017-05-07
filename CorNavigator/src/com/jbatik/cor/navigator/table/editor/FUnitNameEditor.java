/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jbatik.cor.navigator.table.editor;

import com.jbatik.lsystem.turtle.Surface;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author risman
 */
public class FUnitNameEditor extends AbstractCellEditor implements TableCellEditor {
    
    private JTextField textfield = new JTextField();
    private Surface surface;
 
    @Override
    public Object getCellEditorValue() {
        return surface;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        surface = (Surface) value;
        textfield.setHorizontalAlignment(SwingConstants.CENTER);
        textfield.setText(surface.getName());
        textfield.addFocusListener(new FocusListener() {
            
//            private String oldName;

            @Override
            public void focusGained(FocusEvent e) {
//                oldName = textfield.getText();
            }

            @Override
            public void focusLost(FocusEvent e) {
                String newName = textfield.getText();
                surface.setName(newName);
                fireEditingStopped();
                table.repaint();
            }
        });
        return textfield;
    }
    
}
