/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.library.actions;

import com.jbatik.core.api.component.ImageFileChooser;
import com.jbatik.core.project.JBatikProject;
import com.jbatik.modules.layout.node.LayoutFileUtil;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.swing.JFileChooser;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "File",
        id = "com.jbatik.modules.layout.importexport.actions.ImportLibraryAction"
)
@ActionRegistration(
        iconBase = "com/jbatik/modules/layout/library/resources/import.png",
        displayName = "#CTL_ImportLibraryAction"
)
@ActionReference(path = "Menu/File", position = 1050, separatorAfter = 1075)
@Messages("CTL_ImportLibraryAction=Import Library")
public final class ImportLibraryAction implements ActionListener {

    private final JBatikProject context;

    public ImportLibraryAction(JBatikProject context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        FileObject libraryFolder = LayoutFileUtil.getLibrariesFolder(context, true);
        JFileChooser fc = new ImageFileChooser(FileUtil.toFile(libraryFolder));
        //Show it.
        int returnVal = fc.showDialog(null, "Choose Library");

        //Process the results.
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            boolean failed = false;
            //copy selected file to library folder, if necessary
            //pertama2 cek apakah ini ngambil dari library folder
            //jika udah ngambil dari library folder, nothing to do
            File[] files = fc.getSelectedFiles();
            for (File ori : files) {
                FileObject selected = FileUtil.toFileObject(ori);
                if (!FileUtil.isParentOf(libraryFolder, selected)) {
                    //jika bukan, copy current file ke libraryfolder
                    String ext = selected.getExt();
                    String newName = FileUtil.findFreeFileName(libraryFolder, selected.getName(), ext);
                    try (InputStream in = selected.getInputStream();
                            OutputStream out = libraryFolder.createAndOpen(newName.concat(".").concat(ext));) {
                        FileUtil.copy(in, out);
                    } catch (IOException ex) {
                        failed = true;
                        Exceptions.printStackTrace(ex);
                        break;
                    }
                }
            }
            if (!failed) {
                NotifyDescriptor nd = new NotifyDescriptor.Message("Library added");
                DialogDisplayer.getDefault().notify(nd);
            }
        }

        //Reset the file chooser for the next time it's shown.
        fc.setSelectedFile(null);
    }
}
