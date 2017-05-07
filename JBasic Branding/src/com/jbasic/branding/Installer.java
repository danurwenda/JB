/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbasic.branding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInstall;

public class Installer extends ModuleInstall {

    @Override
    public void restored() {
        // write pdf file to doc directory
        try {
            File temp = new File("jBatik4-Basic-Manual.pdf");
            try (
                    InputStream is = getClass().getResourceAsStream("/com/jbasic/branding/resources/manual_4basic.pdf");
                    FileOutputStream fos = new FileOutputStream(temp)) {
                FileUtil.copy(is, fos);
            }
        } catch (IOException ex) {
//            System.out.println("Resource not found");
            System.err.println("msg " + ex.getMessage());
        }
    }

}
