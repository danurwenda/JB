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
import javax.swing.JToolBar;
import org.netbeans.api.visual.action.WidgetAction;

/**
 * TODO : perlu ada base class untuk action2 yang perlu ada interaksi ke widget
 * dan akibatnya mengubah scene.activetool
 *
 * @author Dimas Y. Danurwenda
 */
public abstract class ToolAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        LibraryScene scene = ImageLibEditorRegistry.getActiveTC().getScene();
        ImageLibraryWidget ilw = scene.getImageWidget();
        //check whether the action is already registered
        WidgetAction.Chain tool = ilw.getActions(toolName());
        if (tool == null) {
            //create tool
            tool = ilw.createActions(toolName());
            //register action
            tool.addAction(widgetAction());
        }
        scene.setActiveTool(toolName());
        //change cursor
        if (getCursor() != null) {
            ilw.setCursor(getCursor());
        }else{
            ilw.setCursor(Cursor.getDefaultCursor());
        }
        //add toolbar to top toolbar
        ImageLibEditorRegistry.getActiveTC().setToolToolbar(getToolbar());
    }

    public JToolBar getToolbar() {
        return null;
    }

    public Cursor getCursor() {
        return null;
    }

    public abstract WidgetAction widgetAction();

    public abstract String toolName();
}
