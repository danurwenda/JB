/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.core.api.actions;

import com.jbatik.core.api.ExportAsPNG;
import com.jbatik.core.api.PNGExportConfiguration;
import com.jbatik.core.api.component.ImagePreview;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;

@ActionID(
        category = "File",
        id = "com.jbatik.core.api.actions.ExportAsPNGAction"
)
@ActionRegistration(
        iconBase = "com/jbatik/core/api/actions/toPNG.png",
        displayName = "#CTL_ExportAsPNGAction"
)
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 1429),
    @ActionReference(path = "Toolbars/File", position = 501)
})

@Messages("CTL_ExportAsPNGAction=Export As PNG")
public final class ExportAsPNGAction implements ActionListener {

    private final ExportAsPNG context;
    private JFileChooser fc;

    public ExportAsPNGAction(ExportAsPNG context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        //di awal itu langsung nampilin file chooser
        //dengan file filternya yang supported oleh context
        //harusnya sih gitu, tapi sementara ini file filternya hanya PNG aja dulu
        if (fc == null) {
            fc = new JFileChooser() {
                @Override
                public void approveSelection() {
                    File f = getSelectedFile();
                    if (f.exists() && getDialogType() == SAVE_DIALOG) {
                        int result = JOptionPane.showConfirmDialog(this, "The file exists, overwrite?", "Existing file", JOptionPane.YES_NO_CANCEL_OPTION);
                        switch (result) {
                            case JOptionPane.YES_OPTION:
                                super.approveSelection();
                                return;
                            case JOptionPane.NO_OPTION:
                                return;
                            case JOptionPane.CLOSED_OPTION:
                                return;
                            case JOptionPane.CANCEL_OPTION:
                                cancelSelection();
                                return;
                        }
                    }
                    super.approveSelection();
                }
            };

            //Add a custom file filter and disable the default
            //(Accept All) file filter.
            fc.addChoosableFileFilter(new PNGFilter());
            fc.setAcceptAllFileFilterUsed(false);

            //Add the preview pane.
            fc.setAccessory(new ImagePreview(fc));

        }
        //Set default name
        fc.setSelectedFile(new File(context.getDefaultName() + ".png"));
        //Show it.
        int returnVal = fc.showSaveDialog(WindowManager.getDefault().getMainWindow());

        //Process the results.
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String selected = fc.getSelectedFile().getPath();

            //check apakah selected sudah ends with png apa engga
            if (!(selected.endsWith(".png") || selected.endsWith(".PNG"))) {
                selected = selected.concat(".png");
            }
            //Export As Image ini bebas
            context.writeImage(selected, new PNGExportConfiguration());
        }

    }

    private static class PNGFilter extends FileFilter {

        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }

            String extension = FileUtil.getExtension(f.getName());
            if (extension != null) {
                return extension.equalsIgnoreCase("png");
            }
            return false;
        }

        @Override
        public String getDescription() {
            return "PNG Files";
        }
    }
}
