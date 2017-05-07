/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.api;

import com.jbatik.modules.layout.layering.SubLayout;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author RAPID02
 */
public interface SubLayoutRenderer {

    public Widget render(Scene sc, SubLayout s);
}
