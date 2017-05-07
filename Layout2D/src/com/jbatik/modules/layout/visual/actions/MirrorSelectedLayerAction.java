/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.visual.actions;

import com.jbatik.core.api.GlobalUndoManager;
import com.jbatik.lsystem.parser.exceptions.ParseRuleException;
import com.jbatik.modules.layout.layering.SubLayoutLayer;
import com.jbatik.modules.layout.visual.LayoutScene;
import com.jbatik.modules.layout.visual.widgets.SubLayoutWidget;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map.Entry;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Exceptions;

/**
 * Using 'M' key to mirror all currently selected layer. The implementation of
 * mirroring is inserting a '|' symbol before the first ALPHABET in each axiom,
 * then reflecting the position of layer w.r.t the centroid of the bounding box
 * of currently selected layers.
 *
 * @author Dimas Y. Danurwenda
 */
public class MirrorSelectedLayerAction extends WidgetAction.Adapter {

    LayoutScene scene;

    public MirrorSelectedLayerAction(LayoutScene aThis) {
        this.scene = aThis;
    }
    /**
     * Original position & axiom of each slw
     */
    private HashMap<SubLayoutLayer, Point> originalPos = new HashMap<>();

    /**
     * Save original position for each selected layer.
     *
     * @param widget
     * @param event
     * @return
     */
    @Override
    public State keyPressed(Widget widget, WidgetKeyEvent event) {
        originalPos.clear();
        if (event.getKeyCode() == KeyEvent.VK_M) {
            for (Object o : scene.getSelectedObjects()) {
                Widget w = scene.findWidget(o);
                if (w != null && w instanceof SubLayoutWidget) {
                    SubLayoutLayer sll = (SubLayoutLayer) o;
                    originalPos.put(sll, sll.getLocation());
                }
            }
        }
        return super.keyPressed(widget, event); //To change body of generated methods, choose Tools | Templates.
    }

    private CompoundEdit edit;

    /**
     * Do the mirror
     *
     * @param widget
     * @param event
     * @return
     */
    @Override
    public State keyReleased(Widget widget, WidgetKeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_M) {
            Rectangle bounding = scene.getSelectedRectangle();
            if (edit == null || edit.isInProgress() == false) {
                edit = new CompoundEdit();
            }
            for (Entry<SubLayoutLayer, Point> e : originalPos.entrySet()) {
                SubLayoutLayer sll = e.getKey();
                //calculate mirrored position
                Point destination = sll.getLocation();
                destination.translate(2 * (bounding.x + bounding.width / 2 - destination.x), 0);
                SubLayoutMirrorUndoableEdit smalledit = new SubLayoutMirrorUndoableEdit(sll, sll.getLocation(), destination);
                edit.addEdit(smalledit);
                //calculate mirrored axiom
                String mirrored = getMirroredAxiom(sll.getSublayout().getAxiom());
                //execute mirroring
                sll.setLocation(destination);
                try {
                    sll.getSublayout().setRawAxiom(mirrored);
                    sll.getSublayout().getRenderer().generate();
                } catch (ParseRuleException ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    sll.getSublayout().getRenderer().render();
                }
            }
            //done adding small edits
            edit.end();
            GlobalUndoManager.getManager().undoableEditHappened(new UndoableEditEvent(scene, edit));
        }

        return super.keyReleased(widget, event); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * insert '|' before the first alphabet.
     *
     * @param ax
     * @return
     */
    private String getMirroredAxiom(String ax) {
        if (ax.startsWith("|")) {
            return ax.substring(1);
        }
        return "|".concat(ax);
    }

    class SubLayoutMirrorUndoableEdit extends AbstractUndoableEdit {

        private SubLayoutLayer sll;
        private Point oriP;
        private Point newP;

        private SubLayoutMirrorUndoableEdit(SubLayoutLayer sll, Point pOri, Point posAxiom) {
            this.sll = sll;
            this.oriP = pOri;
            this.newP = posAxiom;
        }

        @Override
        public String getPresentationName() {
            return "Mirror Layer";
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            sll.setLocation(newP);
            sll.getSublayout().setRawAxiom(getMirroredAxiom(sll.getSublayout().getAxiom()));
            try {
                sll.getSublayout().getRenderer().generate();
            } catch (ParseRuleException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                sll.getSublayout().getRenderer().render();
            }
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            sll.setLocation(oriP);
            sll.getSublayout().setRawAxiom(getMirroredAxiom(sll.getSublayout().getAxiom()));
            try {
                sll.getSublayout().getRenderer().generate();
            } catch (ParseRuleException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                sll.getSublayout().getRenderer().render();
            }
        }
    }
}
