package com.jbatik.branding;

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
            File temp = new File("jBatik4-Pro-Manual.pdf");
            try (
                    InputStream is = getClass().getResourceAsStream("/com/jbatik/branding/resources/manual_4pro.pdf");
                    FileOutputStream fos = new FileOutputStream(temp)) {
                FileUtil.copy(is, fos);
            }
        } catch (IOException ex) {
            System.out.println("Resource not found");
        }
    }

}
