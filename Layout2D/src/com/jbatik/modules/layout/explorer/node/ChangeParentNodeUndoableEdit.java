/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.explorer.node;

import com.jbatik.modules.layout.layering.LayoutLayer;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

/**
 *
 * @author Dimas Y. Danurwenda
 */
class ChangeParentNodeUndoableEdit extends AbstractUndoableEdit {

    LayoutLayer moved;
    LayerList oldModel;
    int oldPos;
    LayerList newModel;

    public ChangeParentNodeUndoableEdit(LayoutLayer ori, LayerList model, LayerList childrenModel) {
        this.moved = ori;
        this.oldModel = model;
        this.oldPos = oldModel.list().indexOf(ori);
        this.newModel = childrenModel;
    }

    @Override
    public String getPresentationName() {
        return "Move Layer";
    }

    @Override
    public void redo() throws CannotRedoException {
        super.redo();
        oldModel.remove(moved);
        moved.setParent(newModel.getOwner());
        newModel.add(moved);
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo(); //To change body of generated methods, choose Tools | Templates.
        newModel.remove(moved);
        moved.setParent(oldModel.getOwner());
        oldModel.add(oldPos, moved);
    }

}
