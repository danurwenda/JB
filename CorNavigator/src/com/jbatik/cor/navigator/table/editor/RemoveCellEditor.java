/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.cor.navigator.table.editor;

import com.jbatik.cor.navigator.PicturePresenter;
import com.jbatik.cor.navigator.table.ColorTextureTableModel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventObject;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author Nanta Es <kedipkedip@ymail.com>
 */
public class RemoveCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {

    private JTable jTable;
    private JButton jButton;
    private ColorTextureTableModel defaultTableModel;

    public RemoveCellEditor(JTable _jTable, ColorTextureTableModel _defaultTableModel) {
        this.jButton = new JButton();
        jTable = _jTable;
        defaultTableModel = _defaultTableModel;

        PicturePresenter.setPicture_Remove(jButton);
        jButton.addActionListener(RemoveCellEditor.this);
        jButton.setPreferredSize(new Dimension(20, 30));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //prevents removing the last index
        if (jTable.getRowCount() <= 1) {
            return;
        }

        int editingRow = jTable.getEditingRow();

        //untuk row paling bawah, setelah memelih object color/texture
        //kemudian row tsb di delete, getEditingRow-nya bisa benilai min.
        //if berikut untuk nangani kasus minus row di record terbawah.
        if (editingRow == -1) {
            editingRow = jTable.getRowCount() - 1;
        }

        defaultTableModel.removeRow(editingRow, true);//2nd params means this action is undoable
    }

    // This method is called when a cell value is edited by the user.    
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        return jButton;
    }

    // This method is called when editing is completed.
    // It must return the new value to be stored in the cell.        
    @Override
    public Object getCellEditorValue() {
        return jButton.getText();
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }

    @Override
    public boolean stopCellEditing() {
        //return super.stopCellEditing();
        //langsung true aja, pemanggilan ke super ngakibatin tabel model rusak
        //jika record di delete dari paling bawah, dan row lebih dari satu.
        return true;
    }
}
