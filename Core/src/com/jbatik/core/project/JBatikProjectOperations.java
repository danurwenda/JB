/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jbatik.core.project;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.CopyOperationImplementation;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.MoveOrRenameOperationImplementation;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Dimas Y. Danurwenda <dimas@danurwenda.com>
 */
final class JBatikProjectOperations implements
        DeleteOperationImplementation,
        MoveOrRenameOperationImplementation, 
        CopyOperationImplementation {
    private final JBatikProject p;

    public JBatikProjectOperations(JBatikProject aThis) {
        this.p = aThis;
    }

    @Override
    public void notifyDeleting() throws IOException {
    }

    @Override
    public void notifyDeleted() throws IOException {
    }

    @Override
    public List<FileObject> getMetadataFiles() {
        return new ArrayList<>();
    }

    @Override
    public List<FileObject> getDataFiles() {
        List<FileObject> files = new ArrayList<>();
        FileObject[] projectChildren = p.getProjectDirectory().getChildren();
        for (FileObject fileObject : projectChildren) {
            addFile(p.getProjectDirectory(), fileObject.getNameExt(), files);
        }
        return files;
    }

    private void addFile(FileObject projectDirectory, String fileName, List<FileObject> result) {
        FileObject file = projectDirectory.getFileObject(fileName);
        if (file != null) {
            result.add(file);
        }
    }

    @Override
    public void notifyRenaming() throws IOException {
    }

    @Override
    public void notifyRenamed(String nueName) throws IOException {
    }

    @Override
    public void notifyMoving() throws IOException {
    }

    @Override
    public void notifyMoved(Project original, File originalPath, String nueName) throws IOException {
    }

    @Override
    public void notifyCopying() throws IOException {
    }

    @Override
    public void notifyCopied(Project original, File originalPath, String nueName) throws IOException {
    }
    
}
