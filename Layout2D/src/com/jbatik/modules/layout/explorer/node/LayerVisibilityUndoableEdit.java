/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.explorer.node;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

/**
 *
 * @author Dimas Y. Danurwenda
 */
class LayerVisibilityUndoableEdit extends AbstractUndoableEdit {

    boolean o;
    boolean n;
    LayoutLayerNode node;

    public LayerVisibilityUndoableEdit(LayoutLayerNode node, boolean b) {
        this.node = node;
        this.o = node.getLayer().isVisible();
        this.n = b;
    }

    @Override
    public String getPresentationName() {
        return "Change Visibility";
    }

    @Override
    public void redo() throws CannotRedoException {
        super.redo();
        node.getLayer().setVisible(n);
        node.propagatedFireIconChange(true);
        node.propagatedFireIconChange(false);
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        node.getLayer().setVisible(o);
        node.propagatedFireIconChange(true);
        node.propagatedFireIconChange(false);
    }

}
