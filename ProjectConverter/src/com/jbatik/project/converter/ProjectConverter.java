/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.project.converter;

import java.util.List;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Dimas Y. Danurwenda
 */
public class ProjectConverter {

    private final static RequestProcessor RP = new RequestProcessor(ProjectConverter.class.getName(), 1);

    public static void convert(List<FileObject> projects, FileObject dest) {
        // convert each old project and create new project in dest directory
        ProgressHandle ph = ProgressHandleFactory.createHandle("Converting Projects");
        // init task num
        ph.start(projects.size());
        SyncCounter counter = new SyncCounter(ph, projects.size());
        // creating tasks and starting it
        for (FileObject fo : projects) {
            RP.post(new Converter(fo, dest, counter));
        }
    }

}
