/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.canvas.visual.actions;

import java.awt.event.MouseEvent;

/**
 * Rotate square action. Modifying sqRot on SubLayout. May only be called on
 * Transform mode;
 *
 * @author Dimas Y. Danurwenda <dimas@danurwenda.com>
 */
public class RotateSquareMouseAction extends RotateMouseAction {

    public RotateSquareMouseAction(RotateProvider p, boolean discreet) {
        super(p, discreet);
    }

    @Override
    public int getMouseButton() {
        return MouseEvent.BUTTON2;
    }

    @Override
    public boolean mustBeSelected() {
        return true;
    }
}
