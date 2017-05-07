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
public class AddLayerUndoableEdit extends AbstractUndoableEdit {

    int index;
    LayerList model;
    LayoutLayer layer;

    public AddLayerUndoableEdit(LayoutLayer clone, LayerList childrenModel) {
        this(clone, childrenModel, 0);
    }

    public AddLayerUndoableEdit(LayoutLayer newLayer, LayerList firstLevelModel, int i) {
        this.index = i;
        this.layer = newLayer;
        this.model = firstLevelModel;
    }

    @Override
    public String getPresentationName() {
        return "Add Layer";
    }

    @Override
    public void redo() throws CannotRedoException {
        super.redo();
        model.add(index, layer);
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        model.remove(layer);
    }

}
