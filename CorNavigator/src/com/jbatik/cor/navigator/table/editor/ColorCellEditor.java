/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.cor.navigator.table.editor;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import org.openide.windows.WindowManager;

/**
 *
 * @author Nanta Es <kedipkedip@ymail.com>
 */
public class ColorCellEditor extends AbstractCellEditor implements TableCellEditor {

    private JButton delegate = new JButton();
    private Color savedColor;
    
    public ColorCellEditor() {
        delegate.setBorderPainted(false);
        delegate.setOpaque(false);
        delegate.setContentAreaFilled(false);
        
        ActionListener actionListener = (ActionEvent actionEvent) -> {
            Color color = JColorChooser.showDialog(WindowManager.getDefault().getMainWindow(), "Color Chooser", savedColor);
            changeColor(color);
            fireEditingStopped();
        };
        delegate.addActionListener(actionListener);
    }

    private void changeColor(Color color) {
        if (color != null) {
            savedColor = color;
            delegate.setBackground(color);
        }
    }

    // This method is called when editing is completed.
    // It must return the new value to be stored in the cell.    
    @Override
    public Object getCellEditorValue() {
        return savedColor;
    }

    // This method is called when a cell value is edited by the user.
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        changeColor((Color) value);
        return delegate;
    }

}
