/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.imageeditor.actions;

import com.jbatik.imageeditor.ImageLibEditorRegistry;
import com.jbatik.imageeditor.ImageLibraryWidget;
import com.jbatik.imageeditor.LibraryScene;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "ImageLibTools",
        id = "com.jbatik.imageeditor.actions.ResizeImageAction"
)
//@ActionReference(path = "ImageLib/Actions")
@ActionRegistration(
        iconBase = "com/jbatik/imageeditor/resources/scale.png",
        displayName = "#CTL_ResizeImageAction"
)
@Messages("CTL_ResizeImageAction=Resize Image")
public final class ResizeImageAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        LibraryScene scene = ImageLibEditorRegistry.getActiveTC().getScene();
        ImageLibraryWidget ilw = scene.getImageWidget();
        Rectangle d = ilw.getBounds();
        ResizeImagePanel box = new ResizeImagePanel(d.width, d.height);
        Object result
                = DialogDisplayer.getDefault().notify(
                        new DialogDescriptor(
                                box, "Change Image Size")
                );

        if (result != NotifyDescriptor.OK_OPTION) {
            return;
        }
        int newWidth = box.getNewWidth();
        int newHeight = box.getNewHeight();
        if (newWidth != d.getWidth() || newHeight != d.getHeight()) {
            scene.setPaperDimension(newWidth, newHeight);
        }

    }
}
