/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.visual.actions;

import com.jbatik.canvas.util.GeomUtil;
import com.jbatik.canvas.visual.actions.RotateProvider;
import com.jbatik.core.api.GlobalUndoManager;
import com.jbatik.core.format.DotDecimalFormat;
import com.jbatik.lsystem.parser.exceptions.ParseRuleException;
import com.jbatik.lsystem.util.LSCompacter;
import com.jbatik.modules.layout.layering.SubLayout;
import com.jbatik.modules.layout.layering.SubLayoutLayer;
import com.jbatik.modules.layout.visual.LayoutScene;
import com.jbatik.modules.layout.visual.widgets.SubLayoutWidget;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Exceptions;

/**
 * The action using this provider will be applied into <em>Scene</em>.
 *
 * @author Dimas Y. Danurwenda
 */
public class SelectedLayerRotateProvider implements RotateProvider {

    private LayoutScene scene;  //the scene
    private CompoundEdit edit;  //edit contains all position/axiom edit from each affected layer

    public static class SubLayoutRotationUndoableEdit extends AbstractUndoableEdit {

        private SubLayoutLayer sll;
        private PosAxiom oriP;
        private PosAxiom newP;

        public SubLayoutRotationUndoableEdit(SubLayoutLayer sll, PosAxiom pOri, PosAxiom posAxiom) {
            this.sll = sll;
            this.oriP = pOri;
            this.newP = posAxiom;
        }

        @Override
        public String getPresentationName() {
            return "Rotate Layer";
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            updatePosAx(sll, newP);
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            updatePosAx(sll, oriP);
        }
    }

    public static class PosAxiom {

        Point p;
        String axiom;

        public PosAxiom(Point p, String axiom) {
            this.p = p;
            this.axiom = axiom;
        }

        public Point getP() {
            return p;
        }

        public void setP(Point p) {
            this.p = p;
        }

        public String getAxiom() {
            return axiom;
        }

        public void setAxiom(String axiom) {
            this.axiom = axiom;
        }
    }
    /**
     * Original position & axiom of each slw
     */
    private HashMap<SubLayoutLayer, PosAxiom> originalPos = new HashMap<>();

    private Point anchor;

    public SelectedLayerRotateProvider(LayoutScene s) {
        this.scene = s;
    }

    private String oriSLAxiom;

    @Override
    public void rotateStarted(Widget wek) {
        //make sure originals are clear
        originalPos.clear();
        //save initial position and axiom of each selected layer
        for (Object o : scene.getSelectedObjects()) {
            Widget w = scene.findWidget(o);
            if (w != null && w instanceof SubLayoutWidget) {
                SubLayoutWidget slw = (SubLayoutWidget) w;
                SubLayout sl = slw.getSublayout();
                SubLayoutLayer sll = (SubLayoutLayer) o;
                originalPos.put(sll, new PosAxiom(sll.getLocation(), sl.getAxiom()));
            }
        }
    }

    /**
     * Anchor point adalah titik tengah dari rectangle yang memuat seluruh
     * selected slw
     *
     * @param w
     * @param p
     * @return
     */
    @Override
    public Point getAnchorPointOnScene(Widget w, Point p) {
        Widget candidate = null;
        Set objects = scene.getSelectedObjects();
        Iterator it = objects.iterator();
        while (candidate == null && it.hasNext()) {
            //see whether it.next is a SLL
            Object o = it.next();
            if (o instanceof SubLayoutLayer) {
                candidate = scene.findWidget(o);
            }
        }
        if (candidate == null) {
            //out of while loop, but candidate still == null
            return p;
        } else {
            SubLayoutLayer sl = (SubLayoutLayer) scene.findObject(candidate);
            oriSLAxiom = sl.getSublayout().getAxiom();
            Rectangle r = scene.getSelectedRectangle();
            anchor = new Point(r.width / 2 + r.x, r.height / 2 + r.y);
            return anchor;
        }
    }

    public static void updatePosAx(SubLayoutLayer sll, PosAxiom pa) {
        sll.setLocation(pa.getP());
        sll.getSublayout().setRawAxiom(pa.getAxiom());
        try {
            sll.getSublayout().getRenderer().generate();
        } catch (ParseRuleException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            sll.getSublayout().getRenderer().render();
        }
    }

    @Override
    public void applyRotation(Widget w, double d) {
        originalPos.entrySet().stream().forEach((entry) -> {
            Point2D.Double rotated = GeomUtil.rotate_point(
                    entry.getValue().getP().x,
                    entry.getValue().getP().y,
                    Math.toRadians(d),
                    anchor.x,
                    anchor.y);
            entry.getKey().setX((int) Math.floor(rotated.x));
            entry.getKey().setY((int) Math.floor(rotated.y));
            try {
                entry.getKey().getSublayout().setRawAxiom(getModifiedAxiom(entry.getValue().getAxiom(), d));
                entry.getKey().getSublayout().getRenderer().generate();
            } catch (ParseRuleException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                entry.getKey().getSublayout().getRenderer().render();
            }
        });
    }

    @Override
    public void rotateFinished(Widget w) {
        //create undoable edit
        Widget candidate = null;
        Set objects = scene.getSelectedObjects();
        Iterator it = objects.iterator();
        while (candidate == null && it.hasNext()) {
            //see whether it.next is a SLL
            Object o = it.next();
            if (o instanceof SubLayoutLayer) {
                candidate = scene.findWidget(o);
            }
        }
        if (candidate != null) {
            SubLayoutLayer sl = (SubLayoutLayer) scene.findObject(candidate);
            if (!oriSLAxiom.equals(sl.getSublayout().getAxiom())) {
                if (edit == null || edit.isInProgress() == false) {
                    edit = new CompoundEdit();
                }
                originalPos.entrySet().stream().forEach((entry) -> {
                    PosAxiom pOri = entry.getValue();
                    SubLayoutLayer sll = entry.getKey();
                    SubLayoutRotationUndoableEdit smallEdit = new SubLayoutRotationUndoableEdit(sll, pOri, new PosAxiom(sll.getLocation(), sll.getSublayout().getAxiom()));
                    edit.addEdit(smallEdit);
                });
                //done adding small edits
                edit.end();
                GlobalUndoManager.getManager().undoableEditHappened(new UndoableEditEvent(scene, edit));
//
            }
        }
    }

    private String getModifiedAxiom(String axiom, double d) throws ParseRuleException {
        String res = axiom;
        DecimalFormat fourDigit = new DecimalFormat("###.##", DotDecimalFormat.getSymbols());
        String formattedDegree = fourDigit.format(d);
        res = "+(" + formattedDegree + ")" + res;
        return LSCompacter.simplifyPrefix(res, '+');
    }

}
