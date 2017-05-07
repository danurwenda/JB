/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.cor.navigator.table.editor;

import java.awt.Component;
import javax.swing.AbstractCellEditor;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author Nanta Es <kedipkedip@ymail.com>
 */
public class LabelCellEditor extends AbstractCellEditor implements TableCellEditor {

    private JLabel jLabel = new JLabel();
    private String cellData;

    public LabelCellEditor(int _tableFontSize) {
        jLabel.setHorizontalAlignment(JLabel.CENTER);
    }

    @Override
    public Object getCellEditorValue() {
        cellData = jLabel.getText();
        return jLabel.getText();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (value != null) {
            cellData = value.toString();
        } else {
            cellData = "";
        }
        jLabel.setText(cellData);
        return jLabel;
    }

}
