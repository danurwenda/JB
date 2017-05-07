/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.imageeditor.actions;

import com.jbatik.imageeditor.ImageLibEditorRegistry;
import com.jbatik.imageeditor.LibraryScene;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "ImageLibTools",
        id = "com.jbatik.imageeditor.actions.ResizeCanvasAction"
)
@ActionReference(path = "ImageLib/Actions")
@ActionRegistration(
        iconBase = "com/jbatik/imageeditor/resources/scale.png",
        displayName = "#CTL_ResizeCanvasAction"
)
@Messages("CTL_ResizeCanvasAction=Resize Canvas")
public final class ResizeCanvasAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        LibraryScene scene = ImageLibEditorRegistry.getActiveTC().getScene();
        Rectangle d = scene.getPaperBounds();
        ResizeCanvasPanel box = new ResizeCanvasPanel(d.width, d.height);
        Object result
                = DialogDisplayer.getDefault().notify(
                        new DialogDescriptor(
                                box, "Change Image Size")
                );

        if (result != NotifyDescriptor.OK_OPTION) {
            return;
        }
        if(box.canvasToImageSize()){
            Rectangle imageSize = scene.getImageWidget().getBounds();
            scene.setPaperDimension(imageSize.width, imageSize.height);
        }
        int newWidth = box.getNewWidth();
        int newHeight = box.getNewHeight();
        if (newWidth != d.getWidth() || newHeight != d.getHeight()) {
            scene.setPaperDimension(newWidth, newHeight);
        }

    }
}
