/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.corak.action;

import com.jbatik.modules.corak.canting.OBJWriter;
import com.jbatik.modules.interfaces.ExportableToOBJCookie;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;

@ActionID(
        category = "File",
        id = "com.jbatik.modules.corak.action.ExportToOBJAction"
)
@ActionRegistration(
        iconBase = "com/jbatik/modules/corak/action/toOBJ.png",
        displayName = "#CTL_ExportToOBJAction"
)
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 1428),
    @ActionReference(path = "Toolbars/File", position = 500)
})
@Messages("CTL_ExportToOBJAction=Export As OBJ")
public final class ExportToOBJAction implements ActionListener {

    private final ExportableToOBJCookie context;
    private boolean enclose = false;

    public ExportToOBJAction(ExportableToOBJCookie context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        if (confirm()) {
            if (enclose){
                context.renderEnclosedOBJ();
            }
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("OBJ Files", "obj"));
            int returnVal = fileChooser.showSaveDialog(WindowManager.getDefault().getMainWindow());
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File f = fileChooser.getSelectedFile();
                String filePath = f.getPath();
                if (!filePath.toLowerCase().endsWith(".obj")) {
                    f = new File(filePath + ".obj");
                }
                try (OBJWriter saver = new OBJWriter(f)) {
                    saver.writeNode(context.getRootGroupForOBJ(), "kainBG");
                } catch (FileNotFoundException ex) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("JBatik failed to create new OBJ file.", NotifyDescriptor.ERROR_MESSAGE));
                } catch (IOException ex) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("JBatik failed to create new OBJ file.", NotifyDescriptor.ERROR_MESSAGE));
                }
            }
            if (enclose) {
                context.doneRenderEnclosedOBJ();
            }
        }
    }
    
    private boolean confirm() {
        JPanel confirmPanel = new JPanel();
        JCheckBox confirmCheck = new JCheckBox("Enclose F ends", true);
        confirmPanel.add(confirmCheck);
        DialogDescriptor dd = new DialogDescriptor(confirmPanel, "Export to OBJ");
        dd.setModal(true);
        Object result = DialogDisplayer.getDefault().notify(dd);
        if (result == NotifyDescriptor.OK_OPTION) {
            enclose = confirmCheck.isSelected();
            return true;
        } else {
            return false;
        }
    }
}
