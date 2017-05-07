/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.corak.newfile;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.MessageFormat;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

@ActionID(
        category = "File",
        id = "com.jbatik.modules.corak.newfile.NewCorakWizardAction"
)
@ActionRegistration(
        displayName = "#CTL_NewCorakFile"
)
@ActionReferences({
    @ActionReference(path = "Projects/com-jbatik-project/Actions/Corak", position = 100)
})
@NbBundle.Messages("CTL_NewCorakFile=New Corak File")
public final class NewCorakWizardAction implements ActionListener {

    private Project context;

    public NewCorakWizardAction(Project context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        WizardDescriptor wiz = new WizardDescriptor(new NewCorakWizardIterator(context));
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle(Bundle.CTL_NewCorakFile());
        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            try {
                executeInstantiated(wiz);
                // do something
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

        }
    }

    private void executeInstantiated(WizardDescriptor wiz) throws IOException {
        for (Object o : wiz.getInstantiatedObjects()) {
            if (o instanceof FileObject) {
                DataObject obj = DataObject.find((FileObject) o);
                // run default action (hopefully should be here)
                final Node node = obj.getNodeDelegate();
                Action _a = node.getPreferredAction();
                if (_a instanceof ContextAwareAction) {
                    _a = ((ContextAwareAction) _a).createContextAwareInstance(node.getLookup());
                }
                final Action a = _a;
                if (a != null) {
                    EventQueue.invokeLater(() -> {
                        a.actionPerformed(new ActionEvent(node, ActionEvent.ACTION_PERFORMED, ""));
                    });
                }
            }
        }
    }

}
