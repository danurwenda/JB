/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.layering.actions;

import com.jbatik.modules.layout.layering.SubLayoutLayer;
import java.util.Map;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

/**
 *
 * @author Dimas Y. Danurwenda
 */
class ResetMappingUndoableEdit extends AbstractUndoableEdit {

    SubLayoutLayer sll;
    Map map;

    public ResetMappingUndoableEdit(SubLayoutLayer sll) {
        this.sll = sll;
        this.map = sll.getImageColorIndex();
    }

    @Override
    public String getPresentationName() {
        return "Reset Library Mapping";
    }

    @Override
    public void redo() throws CannotRedoException {
        super.redo();
        sll.resetMapping();
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        sll.setImageColorIndex(map);
    }

}
