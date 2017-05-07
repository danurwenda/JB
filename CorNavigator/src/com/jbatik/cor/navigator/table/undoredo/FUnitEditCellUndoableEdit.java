/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.cor.navigator.table.undoredo;

import com.jbatik.cor.navigator.table.FUnitTableModel;
import java.awt.Color;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

/**
 *
 * @author risman
 */
public class FUnitEditCellUndoableEdit extends AbstractUndoableEdit {

    private Object oldvalue;
    private Object newvalue;
    private int row;
    private int col;
    private FUnitTableModel model;

    public FUnitEditCellUndoableEdit(FUnitTableModel model, Object oldvalue, int row, int col, Object newvalue) {
        this.oldvalue = oldvalue;
        this.newvalue = newvalue;
        this.row = row;
        this.col = col;
        this.model = model;
    }

    @Override
    public String getPresentationName() {
        return "Edit FUnit";
    }
    
    @Override
    public void redo() throws CannotRedoException {
        super.redo();
        model.setValueAt(newvalue, row, col,false);
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        model.setValueAt(oldvalue, row, col,false);
    }

}
