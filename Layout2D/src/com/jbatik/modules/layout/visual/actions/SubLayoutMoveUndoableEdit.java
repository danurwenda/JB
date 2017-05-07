/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.visual.actions;

import com.jbatik.modules.layout.layering.SubLayoutLayer;
import java.awt.Point;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

/**
 *
 * @author Slurp
 */
public class SubLayoutMoveUndoableEdit extends AbstractUndoableEdit {

    private Point originalLocation;
    private Point suggestedLocation;
    private SubLayoutLayer sll;

    public SubLayoutMoveUndoableEdit(SubLayoutLayer w, Point o, Point n) {
        sll = w;
        originalLocation = o;
        suggestedLocation = n;
    }

    @Override
    public String getPresentationName() {
        return "Move";
    }

    @Override
    public void redo() throws CannotRedoException {
        super.redo();
        sll.setLocation(suggestedLocation);
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        sll.setLocation(originalLocation);
    }

}
