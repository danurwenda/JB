/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.imageeditor.actions;

import com.jbatik.imageeditor.ImageLibEditorRegistry;
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
        id = "com.jbatik.imageeditor.actions.HSVShiftAction"
)
@ActionReference(path = "ImageLib/Actions")
@ActionRegistration(
        iconBase = "com/jbatik/imageeditor/resources/hsv.png",
        displayName = "#CTL_HSVShiftAction"
)
@Messages("CTL_HSVShiftAction=HSV Shift")
public final class HSVShiftAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        LibraryScene scene = ImageLibEditorRegistry.getActiveTC().getScene();

        HSVShiftPanel box = new HSVShiftPanel(scene);
        Object result
                = DialogDisplayer.getDefault().notify(
                        new DialogDescriptor(
                                box, "Shift Color")
                );

        if (result != NotifyDescriptor.OK_OPTION) {
            scene.revertColorShifting();
        } else {
            scene.commitColorShifting();
            scene.addSavable();
        }
        scene.validate();
    }
}
