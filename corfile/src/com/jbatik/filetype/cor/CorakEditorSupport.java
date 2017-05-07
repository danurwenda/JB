/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jbatik.filetype.cor;

import java.io.IOException;
import org.netbeans.api.actions.Openable;
import org.openide.filesystems.FileObject;
import org.openide.loaders.SaveAsCapable;

/**
 *
 * @author RAPID02
 */
class CorakEditorSupport implements Openable,SaveAsCapable{
    private CorakDataObject obj;

    public CorakEditorSupport(CorakDataObject aThis) {
        this.obj = aThis;
    }

    @Override
    public void open() {
    }

    @Override
    public void saveAs(FileObject folder, String name) throws IOException {
    }
    
}
