/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.imageeditor.tools;

import com.jbatik.imageeditor.ImageLibEditorRegistry;
import com.jbatik.imageeditor.ImageLibraryWidget;
import com.jbatik.imageeditor.salinan.ColorController;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.Widget;
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
        id = "com.jbatik.imageeditor.actions.PickColorAction"
)
@ActionReference(path = "ImageLib/Tools",position = 50)
@ActionRegistration(
        iconBase = "com/jbatik/imageeditor/resources/colorpicker.png",
        displayName = "#CTL_PickColorAction"
)
@NbBundle.Messages("CTL_PickColorAction=Pick Color")
public final class PickColorAction extends ToolAction {

    @Override
    public WidgetAction widgetAction() {
        return new PickColorWidgetAction();
    }

    @Override
    public String toolName() {
        return Bundle.CTL_PickColorAction();
    }
    
    @Override
    public Cursor getCursor(){
        return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
    }

    private static class PickColorWidgetAction extends WidgetAction.Adapter {

        @Override
        public WidgetAction.State mouseClicked(Widget widget, WidgetAction.WidgetMouseEvent event) {
            ImageLibraryWidget ilw = (ImageLibraryWidget) widget;
            Point lpoint = event.getPoint();
            Rectangle area = ilw.getPreferredBounds();
            lpoint.translate(-area.x, -area.y);
            int p = ilw.getSnapshot().getRGB(lpoint.x, lpoint.y);
            Color picked = new Color(p, true);
            ColorController cc = ImageLibEditorRegistry.getActiveTC().getColorController();
            if (event.getButton() == MouseEvent.BUTTON1) {
                cc.setForeground(picked);
            } else if (event.getButton() == MouseEvent.BUTTON3) {
                cc.setBackground(picked);
            }
            return WidgetAction.State.REJECTED;
        }

    }
}
