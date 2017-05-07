/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jbatik.cor.navigator.table;

import com.jbatik.cor.navigator.table.undoredo.FUnitAddRowUndoableEdit;
import com.jbatik.cor.navigator.table.undoredo.FUnitEditCellUndoableEdit;
import com.jbatik.cor.navigator.table.undoredo.FUnitRemoveRowUndoableEdit;
import com.jbatik.lsystem.turtle.Surface;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

/**
 *
 * @author risman
 */
public class FUnitTableModel extends OrderedIndexTableModel {
    
    private static String columnName = "Name";
    private static String columnFUnit = "Edit FUnit";
    private UndoManager manager;
    private boolean undoEnabled;
    
    public boolean isUndoEnabled() {
        return undoEnabled;
    }
    
    public void setUndoEnabled(boolean isUndoEnabled) {
        this.undoEnabled = isUndoEnabled;
    }

    public FUnitTableModel() {
        super(new Object[]{columnName, columnFUnit});
    }


    public void removeRow(int row, boolean undoable) {
        if (undoable) {
            Vector oldValue = (Vector) dataVector.elementAt(row);
            super.removeRow(row);
            FUnitRemoveRowUndoableEdit edit = new FUnitRemoveRowUndoableEdit(this, oldValue, row);
            addEdit(edit);
        } else {
            super.removeRow(row);
        }
    }
    
    public void addRow(Object[] rowData, boolean undoable) {
        addRow(convertToVector(rowData), undoable);
    }
    
    public void addRow(Vector rowData, boolean undoable) {
        insertRow(getRowCount(), rowData, undoable);
    }
    
    public void insertRow(int i, Vector vector, boolean e) {
        super.insertRow(i, vector); 
        if (e) {
            FUnitAddRowUndoableEdit edit = new FUnitAddRowUndoableEdit(this, vector, i);
            addEdit(edit);
        }
    }
    
    private void addEdit(UndoableEdit edit) {
        if (manager != null && undoEnabled) {
            manager.undoableEditHappened(new UndoableEditEvent(this, edit));
        }
    }
    
    public void setValueAt(Object newValue, int r, int c, boolean undoable) {
        if (undoable) {
            Object old = getValueAt(r, c);
            super.setValueAt(newValue, r, c);
            FUnitEditCellUndoableEdit edit = new FUnitEditCellUndoableEdit(this, old, r, c, newValue);
            addEdit(edit);
        } else {
            super.setValueAt(newValue, r, c);
        }
    }

    @Override
    public void setValueAt(Object aValue, int row, int column) {
        setValueAt(aValue, row, column,true);
    }
    
    public void setUndoManager(UndoManager activeUndoRedo) {
        this.manager = activeUndoRedo;
    }
    
        public List<Surface> extractSurfaces() {
        List<Surface> fs = new ArrayList();
        for (int i = 0; i < getRowCount(); i++) {
            fs.add((Surface) getValueAt(i, 1));
        }
        return fs;
    }
}
