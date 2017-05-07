/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout;

import com.jbatik.core.api.DocumentPaper;
import java.awt.Color;

/**
 * Document paper with background color.
 *
 * @author RAPID02
 */
public class LayoutDocument extends DocumentPaper {

    public LayoutDocument(double w, double h, int dpi, String u, Color bg) {
        super(w, h, dpi, u);
        this.background = bg;
    }

    private Color background;
    public static final String BACKGROUND_PROP = "background";

    public Color getBackground() {
        return background;
    }

    public void setBackground(Color b) {
        Color old = background;
        this.background = b;
        pcs.firePropertyChange(BACKGROUND_PROP, old, b);
    }
}
