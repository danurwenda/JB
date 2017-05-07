/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.imageeditor.actions;

import com.jbatik.imageeditor.ImageLibEditorRegistry;
import com.jbatik.imageeditor.ImageLibraryWidget;
import com.jbatik.imageeditor.LibraryScene;
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
        id = "com.jbatik.imageeditor.actions.RotateAction"
)
@ActionReference(path = "ImageLib/Actions")
@ActionRegistration(
        iconBase = "com/jbatik/imageeditor/resources/rotate.png",
        displayName = "#CTL_RotateAction"
)
@Messages("CTL_RotateAction=Rotate")
public final class RotateAction implements ActionListener {
    
    @Override
    public void actionPerformed(ActionEvent e) {
        RotateImagePanel box = new RotateImagePanel();
        Object result
                = DialogDisplayer.getDefault().notify(
                        new DialogDescriptor(
                                box, "Rotate Image")
                );
        
        if (result != NotifyDescriptor.OK_OPTION) {
            return;
        }
        int newDeg = box.getRotatingDegree();
        boolean isRelative = box.isRelative();
        LibraryScene scene = ImageLibEditorRegistry.getActiveTC().getScene();
        ImageLibraryWidget ilw = scene.getImageWidget();
        double oldDeg = ilw.getRotationAngle();
        ilw.setRotationAngle(isRelative ? oldDeg + newDeg : newDeg);
        ilw.revalidate();
        scene.validate();
        scene.addSavable();
    }
}
