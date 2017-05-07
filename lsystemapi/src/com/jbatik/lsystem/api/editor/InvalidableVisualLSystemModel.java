/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.lsystem.api.editor;

import com.jbatik.lsystem.InvalidableVisualLSystem;
import org.openide.awt.UndoRedo;

/**
 * Wrapper for Invalidable + UndoRedo and Savable. Put it on the Lookup to be
 * listened by LSystemEditor.
 *
 * @author Dimas Y. Danurwenda
 */
public abstract class InvalidableVisualLSystemModel {

    private InvalidableVisualLSystem vls;
    private UndoRedo.Manager undoredomgr;

    public InvalidableVisualLSystemModel(InvalidableVisualLSystem vls, UndoRedo.Manager undoredomgr) {
        this.vls = vls;
        this.undoredomgr = undoredomgr;
    }

    public InvalidableVisualLSystem getVls() {
        return vls;
    }

    public void setVls(InvalidableVisualLSystem vls) {
        this.vls = vls;
    }

    public UndoRedo.Manager getUndoredomgr() {
        return undoredomgr;
    }

    public void setUndoredomgr(UndoRedo.Manager undoredomgr) {
        this.undoredomgr = undoredomgr;
    }

    public abstract void addSavable();
}
