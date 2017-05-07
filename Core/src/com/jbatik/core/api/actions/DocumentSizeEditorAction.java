/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.core.api.actions;

import com.jbatik.core.api.DocumentPaper;
import com.jbatik.core.api.component.DocumentSizeEditorPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Edit",
        id = "com.jbatik.core.api.actions.DocumentSizeEditorAction"
)
@ActionRegistration(
        iconBase = "com/jbatik/core/api/actions/papersize.png",
        displayName = "#CTL_DocumentSizeEditorAction"
)
@ActionReference(path = "Menu/Edit", position = 2750, separatorBefore = 2625)
@Messages("CTL_DocumentSizeEditorAction=Paper Size")
public final class DocumentSizeEditorAction implements ActionListener {

    private final DocumentPaper context;

    public DocumentSizeEditorAction(DocumentPaper context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        DocumentSizeEditorPanel panel = new DocumentSizeEditorPanel(context);
        DialogDescriptor dd = new DialogDescriptor(panel, "Paper Size");
        dd.setModal(true);
        Object result = DialogDisplayer.getDefault().notify(dd);
        if (result != NotifyDescriptor.OK_OPTION) {
            return;
        }
        try {
            //ambil stat
            context.setDPI((int) panel.getDPI());
            context.setUnit(panel.getUnit());
            context.setWidth(panel.getDocumentWidth());
            context.setHeight(panel.getDocumentHeight());
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
