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
public class FUnitAddRowUndoableEdit extends AbstractUndoableEdit {

    private DefaultTableModel model;
    private Vector value;
    private int row;

    public FUnitAddRowUndoableEdit(FUnitTableModel aThis, Vector vector, int i) {
        this.model = aThis;
        this.value = vector;
        this.row = i;
    }

    @Override
    public String getPresentationName() {
        return "Add new Row";
    }

    @Override
    public void redo() throws CannotRedoException {
        super.redo();
        model.insertRow(row, value);
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        model.removeRow(row);
    }

}
