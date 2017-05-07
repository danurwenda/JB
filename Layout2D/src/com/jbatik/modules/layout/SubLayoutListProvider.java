/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout;

import com.jbatik.modules.layout.layering.SubLayout;
import java.util.List;

/**
 *
 * @author RAPID02
 */
public interface SubLayoutListProvider {

    public List<SubLayout> getSubLayouts();
}
