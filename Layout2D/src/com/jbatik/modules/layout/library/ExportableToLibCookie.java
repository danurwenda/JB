/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.library;

import com.jbatik.core.api.ExportAsPNG;
import com.jbatik.core.api.JBatikCanvas;

/**
 *
 * @author RAPID01
 */
public interface ExportableToLibCookie extends ExportAsPNG, JBatikCanvas {

    public void writeLib(String path);

}
