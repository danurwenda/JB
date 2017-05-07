/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jbatik.cor.navigator.table.undoredo;

import com.jbatik.cor.navigator.table.ColorTextureTableModel;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

/**
 *
 * @author RAPID02
 */
public class RemoveRowUndoableEdit extends AbstractUndoableEdit{
    private DefaultTableModel model;
    private Vector value;
    private int row;

    public RemoveRowUndoableEdit(ColorTextureTableModel aThis, Vector oldValue, int row) {
        this.model=aThis;
        this.value=oldValue;
        this.row=row;
    }

    @Override
    public String getPresentationName() {
        return "Remove Row"; //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void redo() throws CannotRedoException {
        super.redo(); //To change body of generated methods, choose Tools | Templates.
        model.removeRow(row);
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo(); //To change body of generated methods, choose Tools | Templates.
        model.insertRow(row, value);
    }
    
}
