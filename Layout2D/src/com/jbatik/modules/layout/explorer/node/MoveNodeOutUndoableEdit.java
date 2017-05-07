/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.explorer.node;

import com.jbatik.modules.layout.layering.GroupLayer;
import com.jbatik.modules.layout.layering.LayoutLayer;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

/**
 * Undoable Edit for "emptying a group and moving all its children one level up"
 * @author Dimas Y. Danurwenda
 */
class MoveNodeOutUndoableEdit extends AbstractUndoableEdit {

    LayerList upperModel;
    LayerList lowerModel;
    LayoutLayer moved;

    MoveNodeOutUndoableEdit(LayoutLayer moved, LayerList oldModel, LayerList newModel) {
        this.moved = moved;
        this.lowerModel = oldModel;
        this.upperModel = newModel;
    }

    @Override
    public String getPresentationName() {
        return "Move to Outer";
    }

    @Override
    public void redo() throws CannotRedoException {
        super.redo();
        lowerModel.remove(moved);
        GroupLayer gl = lowerModel.getOwner();
        int i = upperModel.list().indexOf(gl);
        moved.setParent(upperModel.getOwner());
        upperModel.add(i, moved);
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        upperModel.remove(moved);
        moved.setParent(lowerModel.getOwner());
        lowerModel.add(moved);
    }

}
