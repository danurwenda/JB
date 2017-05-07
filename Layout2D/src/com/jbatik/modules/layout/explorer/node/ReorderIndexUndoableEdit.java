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
public class ReorderIndexUndoableEdit extends AbstractUndoableEdit {

    int[] perm;
    int[] permReverse;
    LayerList model;

    public ReorderIndexUndoableEdit(int[] perm, LayerList m) {
        this.perm = perm;
        this.permReverse = createReverse(perm);
        this.model = m;
    }

    @Override
    public String getPresentationName() {
        return "Reorder Layer";
    }

    @Override
    public void redo() throws CannotRedoException {
        super.redo();
        model.reorder(perm);
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        model.reorder(permReverse);
    }

    private int[] createReverse(int[] perm) {
        int[] rev = new int[perm.length];
        for (int i = 0; i < rev.length; i++) {
            rev[perm[i]] = i;
        }
        return rev;
    }

}
