/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.cor.navigator.table;

import com.jbatik.cor.navigator.table.undoredo.AddRowUndoableEdit;
import com.jbatik.cor.navigator.table.undoredo.EditCellUndoableEdit;
import com.jbatik.cor.navigator.table.undoredo.RemoveRowUndoableEdit;
import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

/**
 * Support undo-redo pada aksi2 perubahan tabel.
 *
 * @author RAPID02
 */
public class ColorTextureTableModel extends OrderedIndexTableModel {
    
    private static String columnColor = "Color";
    private static String columnTexture = "Texture";
    private UndoManager manager;
    private boolean undoEnabled;
    
    public boolean isUndoEnabled() {
        return undoEnabled;
    }
    
    public void setUndoEnabled(boolean isUndoEnabled) {
        this.undoEnabled = isUndoEnabled;
    }
    
    public ColorTextureTableModel() {
        super(new Object[]{columnColor, columnTexture});
    }
    
    public void removeRow(int row, boolean undoable) {
        //save old value
        if (undoable) {
            Vector oldValue = (Vector) dataVector.elementAt(row);
            super.removeRow(row);
            RemoveRowUndoableEdit edit = new RemoveRowUndoableEdit(this, oldValue, row);
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
        super.insertRow(i, vector); //To change body of generated methods, choose Tools | Templates.
        if (e) {
            AddRowUndoableEdit edit = new AddRowUndoableEdit(this, vector, i);
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
            //get old value on that cell
            Object old = getValueAt(r, c);
            super.setValueAt(newValue, r, c);
            //create undoableedit
            EditCellUndoableEdit edit = new EditCellUndoableEdit(this, old, r, c, newValue);
            addEdit(edit);
        } else {
            super.setValueAt(newValue, r, c);
        }
    }

    @Override
    public void setValueAt(Object aValue, int row, int column) {
        setValueAt(aValue, row, column,true); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void setUndoManager(UndoManager activeUndoRedo) {
        //test apakah sama
//        if (manager == activeUndoRedo) {
//            System.err.println("udah sama cuy");
//        } else {
//            System.err.println("beda cuy");
//        }
        this.manager = activeUndoRedo;
    }
    
    public List<Color> extractColors() {
        List<Color> cs = new ArrayList();
        for (int i = 0; i < getRowCount(); i++) {
            cs.add((Color) getValueAt(i, 1));
        }
        return cs;
    }
    
    public List<String> extractTextures() {
        List<String> cs = new ArrayList();
        for (int i = 0; i < getRowCount(); i++) {
            File f = (File) getValueAt(i, 2);
            cs.add(f.getName());
        }
        return cs;
    }
}
