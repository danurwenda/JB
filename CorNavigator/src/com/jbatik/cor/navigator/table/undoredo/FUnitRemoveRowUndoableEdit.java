/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jbatik.cor.navigator.table.undoredo;

import com.jbatik.cor.navigator.table.FUnitTableModel;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

/**
 *
 * @author risman
 */
public class FUnitRemoveRowUndoableEdit extends AbstractUndoableEdit{
    private DefaultTableModel model;
    private Vector value;
    private int row;

    public FUnitRemoveRowUndoableEdit(FUnitTableModel aThis, Vector oldValue, int row) {
        this.model=aThis;
        this.value=oldValue;
        this.row=row;
    }

    @Override
    public String getPresentationName() {
        return "Remove Row"; 
    }
    
    @Override
    public void redo() throws CannotRedoException {
        super.redo(); 
        model.removeRow(row);
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        model.insertRow(row, value);
    }
    
}
