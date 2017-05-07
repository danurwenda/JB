/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.canvas.visual;

/**
 *
 * @author RAPID02
 */
public class ZoomAction {

    protected ZoomStrategy strategy;

    public ZoomAction(ZoomStrategy s) {
        this.strategy = s;
    }

    public class ZoomStrategy {

        public ZoomStrategy() {
        }
    }
}
