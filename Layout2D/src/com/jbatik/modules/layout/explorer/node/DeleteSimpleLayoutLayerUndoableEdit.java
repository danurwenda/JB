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
class DeleteSimpleLayoutLayerUndoableEdit extends AbstractUndoableEdit {

    LayoutLayer deleted;
    LayerList model;
    int oldPosition;

    DeleteSimpleLayoutLayerUndoableEdit(LayoutLayer aThis, LayerList model) {
        this.deleted = aThis;
        this.model = model;
        this.oldPosition = model.list().indexOf(deleted);
        assert oldPosition != -1;
    }

    @Override
    public String getPresentationName() {
        return "Delete Layer";
    }

    @Override
    public void redo() throws CannotRedoException {
        super.redo();
        model.remove(deleted);
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        model.add(oldPosition, deleted);
    }

}
