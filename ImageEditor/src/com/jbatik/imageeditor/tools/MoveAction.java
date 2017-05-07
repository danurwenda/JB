/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.imageeditor.tools;

import com.jbatik.imageeditor.ImageLibEditorRegistry;
import com.jbatik.imageeditor.ImageLibraryWidget;
import com.jbatik.imageeditor.LibraryScene;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.netbeans.api.visual.action.WidgetAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 * TODO : perlu ada base class untuk action2 yang perlu ada interaksi ke widget
 * dan akibatnya mengubah scene.activetool
 *
 * @author Dimas Y. Danurwenda
 */
@ActionID(
        category = "ImageLibTools",
        id = "com.jbatik.imageeditor.actions.MoveAction"
)
//set position to 0 agar muncul paling atas
@ActionReference(path = "ImageLib/Tools",position = 0)
@ActionRegistration(
        iconBase = "com/jbatik/imageeditor/resources/move.png",
        displayName = "#CTL_MoveAction"
)
@NbBundle.Messages("CTL_MoveAction=Move")
public final class MoveAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        LibraryScene scene = ImageLibEditorRegistry.getActiveTC().getScene();
        ImageLibraryWidget ilw = scene.getImageWidget();
        //check whether the action is already registered
        WidgetAction.Chain tool = ilw.getActions(LibraryScene.MOVE_TOOL);
        if (tool == null) {
            //create tool
            tool = ilw.createActions(LibraryScene.MOVE_TOOL);
            //register action
            tool.addAction(scene.getMoveAction());
            //create move axis tool
            tool = ilw.createActions(LibraryScene.MOVE_AXIS_TOOL);
            //register action
            tool.addAction(scene.getMoveAxisAxtion());
        }
        ilw.getScene().setActiveTool(LibraryScene.MOVE_TOOL);
        ilw.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        //change cursor
        ImageLibEditorRegistry.getActiveTC().setCursor(Cursor.getDefaultCursor());
         //add toolbar to top toolbar
        ImageLibEditorRegistry.getActiveTC().setToolToolbar(null);
    }
}
