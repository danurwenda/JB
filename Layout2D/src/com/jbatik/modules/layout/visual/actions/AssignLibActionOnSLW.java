/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.visual.actions;

import com.jbatik.core.api.GlobalUndoManager;
import com.jbatik.canvas.util.ViewCleaner;
import com.jbatik.modules.layout.layering.SubLayoutLayer;
import com.jbatik.modules.layout.library.palette.EraserNode;
import com.jbatik.modules.layout.visual.LayoutScene;
import com.jbatik.modules.layout.api.LibMappable;
import com.jbatik.modules.layout.visual.widgets.SquareWidget;
import com.jbatik.modules.layout.visual.widgets.SubLayoutWidget;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.netbeans.api.visual.action.AcceptProvider;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.widget.Widget;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;

/**
 *
 * @author Dimas Y. Danurwenda
 */
public class AssignLibActionOnSLW implements AcceptProvider {

    LayoutScene s;

    public AssignLibActionOnSLW(LayoutScene s) {
        this.s = s;
    }

    private Image getImageFromTransferable(Transferable t) {
        Object o = null;
        try {
            o = t.getTransferData(DataFlavor.imageFlavor);
        } catch (UnsupportedFlavorException | IOException ex) {
//            Exceptions.printStackTrace(ex);
        }
        return o instanceof Image
                ? (Image) o
                : null;
    }

    private String getImageNameFromTransferable(Transferable transferable) {
        Object o = null;

        try {
            o = transferable.getTransferData(DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException | IOException ex) {
//            Exceptions.printStackTrace(ex);
        }

        return o instanceof String ? (String) o : "";
    }

    private Point lastSnippetLocation;

    @Override
    public ConnectorState isAcceptable(Widget widget, Point point, Transferable transferable) {
        if (widget instanceof LibMappable) {
            LibMappable mappable = (LibMappable) widget;
            if (mappable.isMappable()) {
                Point pointView = s.convertSceneToView(widget.convertLocalToScene(point));
                ViewCleaner.schedule(s.getView());

                Image dragImage = getImageFromTransferable(transferable);
                if (dragImage != null) {
                    if (!pointView.equals(lastSnippetLocation)) {
                        lastSnippetLocation = pointView;
                        //redraw snippet
                        JComponent view = s.getView();
                        Rectangle visRect = view.getVisibleRect();
                        AffineTransform at = AffineTransform.getTranslateInstance(
                                pointView.getX(),
                                pointView.getY());
                        //flush previous snippet
                        view.paintImmediately(visRect.x, visRect.y, visRect.width, visRect.height);
                        Node eraser = NodeTransfer.node(transferable, NodeTransfer.DND_COPY);
                        if (eraser == null || !(eraser instanceof EraserNode)) {
                            //draw new snippet (jika bukan eraser)
                            Graphics2D g2 = (Graphics2D) view.getGraphics();
                            g2.drawImage(dragImage,
                                    at,
                                    null);
                        }
                    }
                    SubLayoutWidget slw = (SubLayoutWidget) widget;
                    SquareWidget hitSquare = slw.hitSquare(point);
                    int idx = hitSquare.getColorIndex();
                    slw.kedip2(idx);
                    return ConnectorState.ACCEPT;
                }
            }
        }
        return ConnectorState.REJECT;
    }

    @Override
    public void accept(Widget widget, Point point, Transferable transferable) {
        SubLayoutWidget slw = (SubLayoutWidget) widget;
        SquareWidget hitSquare = slw.hitSquare(point);
        int idx = hitSquare.getColorIndex();
        SubLayoutLayer sl = (SubLayoutLayer) s.findObject(slw);
        String oldImage = sl.getSublayout().getImageForIndex(idx);
        Node eraser = NodeTransfer.node(transferable, NodeTransfer.DND_COPY);
        if (eraser != null && eraser instanceof EraserNode) {
            //transferring eraser
            if (oldImage != null) {
                //hapus
                ImageIndexMappingUndoableEdit edit = new ImageIndexMappingUndoableEdit(slw, idx, oldImage, null);
                GlobalUndoManager.getManager().undoableEditHappened(new UndoableEditEvent(slw, edit));
                //ga sama, saatnya update dan put ke undomanager
                updateImageMapping(slw, idx, null);
            }
        } else {
            String droppedImage = getImageNameFromTransferable(transferable);
            //cek apakah sudah sama
            if (!droppedImage.equals(oldImage)) {
                ImageIndexMappingUndoableEdit edit = new ImageIndexMappingUndoableEdit(slw, idx, oldImage, droppedImage);
                GlobalUndoManager.getManager().undoableEditHappened(new UndoableEditEvent(slw, edit));
                //ga sama, saatnya update dan put ke undomanager
                updateImageMapping(slw, idx, droppedImage);

            }
            //bebersih gambar drag
            JComponent view = s.getView();
            Rectangle visRect = view.getVisibleRect();
            view.paintImmediately(visRect.x, visRect.y, visRect.width, visRect.height);
        }
    }

    void updateImageMapping(SubLayoutWidget slw, int idx, String imageName) {
        SubLayoutLayer sl = (SubLayoutLayer) s.findObject(slw);
        sl.setImageForIndex(idx, imageName);
        slw.repaintColorIndex(idx);
        s.requestActive();
    }

    class ImageIndexMappingUndoableEdit extends AbstractUndoableEdit {

        private SubLayoutWidget widget;
        private String originalImage;
        private String suggestedImage;
        private int imageIndex;

        public ImageIndexMappingUndoableEdit(SubLayoutWidget w, int i, String o, String n) {
            widget = w;
            imageIndex = i;
            originalImage = o;
            suggestedImage = n;
        }

        @Override
        public String getPresentationName() {
            return "Assign library";
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            updateImageMapping(widget, imageIndex, suggestedImage);
            s.validate();
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            updateImageMapping(widget, imageIndex, originalImage);
            s.validate();
        }
    }

}
