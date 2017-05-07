/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.imageeditor.tools;

import com.jbatik.core.api.component.NumericTextField;
import com.jbatik.imageeditor.ImageLibEditorRegistry;
import com.jbatik.imageeditor.ImageLibraryWidget;
import com.jbatik.imageeditor.LibraryScene;
import com.jbatik.imageeditor.jhfilter.CoordinatedPixelColorFilter;
import com.jbatik.imageeditor.jhfilter.MapColorsWithToleranceFilter;
import com.jbatik.imageeditor.jhfilter.Pixel;
import com.jbatik.imageeditor.salinan.ColorController;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.Widget;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "ImageLibTools",
        id = "com.jbatik.imageeditor.actions.ChangeColorAction"
)
@ActionReference(path = "ImageLib/Tools", position = 10)
@ActionRegistration(
        iconBase = "com/jbatik/imageeditor/resources/ember.png",
        displayName = "#CTL_ChangeColorAction"
)
@Messages({
    "CTL_ChangeColorAction=Change Color", "CTL_Tolerance=Tolerance : ",
    "CTL_Contiguous=Contiguous"
})
public final class ChangeColorAction extends ToolAction {

    @Override
    public WidgetAction widgetAction() {
        return new WidgetAction.Adapter() {
            @Override
            public WidgetAction.State mouseClicked(Widget widget, WidgetAction.WidgetMouseEvent event) {
                if (event.getButton() == MouseEvent.BUTTON2) {
                    return WidgetAction.State.REJECTED;
                }
                ImageLibraryWidget ilw = (ImageLibraryWidget) widget;
                Point lpoint = event.getPoint();

                BufferedImage bi = ilw.getSnapshot();
                int target = bi.getRGB(bi.getWidth() / 2 + lpoint.x, bi.getHeight() / 2 + lpoint.y);
                
                ColorController cc = ImageLibEditorRegistry.getActiveTC().getColorController();
                int t = 0;
                try {
                    t = tolVal.getLongValue().intValue();
                } catch (ParseException ex) {
                    Exceptions.printStackTrace(ex);
                }
                int rep = 0;
                if (event.getButton() == MouseEvent.BUTTON1) {
                    rep = cc.getForeground().getRGB();
                } else if (event.getButton() == MouseEvent.BUTTON3) {
                    rep = cc.getBackground().getRGB();
                }
                MapColorsWithToleranceFilter mcf = new MapColorsWithToleranceFilter(target, rep, t);
                BufferedImage after = mcf.filter(ilw.getOriginalImage(), null);

                ilw.setImage(after);
                ilw.repaint();
                LibraryScene scene = ImageLibEditorRegistry.getActiveTC().getScene();
                scene.addSavable();

                ImageLibEditorRegistry.getActiveTC().getUndoManager().undoableEditHappened(
                        new UndoableEditEvent(
                                after,
                                new FloodColorUndoableEdit(ilw, mcf.getChangedPixel(), rep))
                );

                return WidgetAction.State.REJECTED;
            }
        };
    }

    JToolBar toolbar;
    NumericTextField tolVal;
//    JCheckBox contiguous;

    @Override
    public JToolBar getToolbar() {
        if (toolbar == null) {
            JLabel toleranceLabel = new JLabel(Bundle.CTL_Tolerance());
            tolVal = new NumericTextField("70", 3);
            tolVal.setMaximumSize(tolVal.getPreferredSize());
//            contiguous = new JCheckBox(Bundle.CTL_Contiguous());
            toolbar = new JToolBar();
            toolbar.add(toleranceLabel);
            toolbar.add(tolVal);
            toolbar.add(Box.createHorizontalStrut(4));
//            toolbar.add(contiguous);
        }
        return toolbar; //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Cursor getCursor() {
        return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
    }

    @Override
    public String toolName() {
        return Bundle.CTL_ChangeColorAction();
    }

    private static class FloodColorUndoableEdit extends AbstractUndoableEdit {

        @Override
        public String getPresentationName() {
            return "Flood Color";
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            CoordinatedPixelColorFilter restore = new CoordinatedPixelColorFilter(changedPixel, newColor);
            BufferedImage after = restore.filter(ilw.getOriginalImage(), null);

            ilw.setImage(after);
            ilw.repaint();
            LibraryScene scene = ImageLibEditorRegistry.getActiveTC().getScene();
            scene.validate();
            scene.addSavable();
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            CoordinatedPixelColorFilter restore = new CoordinatedPixelColorFilter(changedPixel);
            BufferedImage after = restore.filter(ilw.getOriginalImage(), null);

            ilw.setImage(after);
            ilw.repaint();
            LibraryScene scene = ImageLibEditorRegistry.getActiveTC().getScene();
            scene.validate();
            scene.addSavable();
        }

        ImageLibraryWidget ilw;
        List<Pixel> changedPixel;
        int newColor;

        public FloodColorUndoableEdit(ImageLibraryWidget ilw, List<Pixel> changedPixel, int n) {
            this.ilw = ilw;
            this.changedPixel = new ArrayList<>(changedPixel);
            this.newColor = n;
        }
    }
}
