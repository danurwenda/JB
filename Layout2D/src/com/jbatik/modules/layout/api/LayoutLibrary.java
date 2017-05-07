/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.api;

import java.awt.Graphics2D;
import javax.swing.event.ChangeListener;

/**
 * Currently only PNG files can be used as library. With this interface devs can
 * produce more library implementation, e.g using SVG files.
 *
 * @author RAPID02
 */
public interface LayoutLibrary {

    /**
     * Using given Graphics, draw the library on a square with size length x
     * length.
     *
     * @param g2
     * @param length
     * @param mirror
     */
    public void drawLibrary(Graphics2D g2, double length, boolean mirror);

    public void addChangeListener(ChangeListener l);

    public boolean isMissing();

    public void removeChangeListener(ChangeListener l);

}
