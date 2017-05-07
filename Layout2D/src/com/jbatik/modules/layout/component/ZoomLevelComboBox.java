/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.component;

import com.jbatik.canvas.visual.Zoomable;
import javax.swing.JComboBox;

/**
 *
 * @author RAPID02
 */
public class ZoomLevelComboBox extends JComboBox {

    Zoomable zoomable;

    public ZoomLevelComboBox(Zoomable z) {
        this.zoomable = z;

    }

}
