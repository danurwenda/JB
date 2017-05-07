/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.component;

import com.jbatik.canvas.util.GeomUtil;
import com.jbatik.core.api.GlobalUndoManager;
import com.jbatik.lsystem.api.parser.Parser;
import com.jbatik.lsystem.parser.exceptions.ParseRuleException;
import com.jbatik.lsystem.util.LSCompacter;
import com.jbatik.modules.layout.layering.SubLayoutLayer;
import com.jbatik.modules.layout.visual.LayoutScene;
import com.jbatik.modules.layout.visual.actions.SelectedLayerRotateProvider;
import com.jbatik.modules.layout.visual.widgets.SubLayoutWidget;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.CompoundEdit;
import org.netbeans.api.visual.widget.Widget;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Rotate selected layers by, or set the angle modifier on axiom to, a specified
 * degree
 *
 * @author danur
 */
class RotatePreciseAction extends AbstractAction {

    // PENDING change to icons provided by Dusan
    private static final String ICON_BASE = "com/jbatik/modules/layout/component/resources/rotatep.png"; // NOI18N

    private LayoutScene scene;

    public RotatePreciseAction(LayoutScene scene) {
        this.scene = scene;
        putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, true));
        putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(RotatePreciseAction.class, "CTL_RotatePreciseAction")); // NOI18N
    }
    private CompoundEdit edit;

    private Point getAnchorPoint() {
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
            return new Point();
        } else {
            SubLayoutLayer sl = (SubLayoutLayer) scene.findObject(candidate);
            Rectangle r = scene.getSelectedRectangle();
            Point a = new Point(r.width / 2 + r.x, r.height / 2 + r.y);
            return a;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        RotatePrecisePanel box = new RotatePrecisePanel();
        Object result
                = DialogDisplayer.getDefault().notify(
                        new DialogDescriptor(
                                box, "Rotate Layers")
                );

        if (result != NotifyDescriptor.OK_OPTION) {
            return;
        }
        boolean abs = box.isAbsolute();
        int deg = box.getRotatingDegree();
        if (deg != 0 || abs) {
            //ready to move
            //create undoableedit
            if (edit == null || edit.isInProgress() == false) {
                edit = new CompoundEdit();
            }
            Point anchor = getAnchorPoint();
//            if (abs) {
            for (Object o : scene.getSelectedObjects()) {
                Widget w = scene.findWidget(o);
                if (w != null && w instanceof SubLayoutWidget) {
                    SubLayoutLayer sll = (SubLayoutLayer) o;
                    Point oriPoint = sll.getLocation();
                    String oriAxiom = sll.getSublayout().getAxiom();

                    try {
                        String dummy = LSCompacter.simplifyPrefix("+(0)" + oriAxiom, '+');
                        float actualRotDeg = deg;
                        if (abs) {
                            //substract value
                            //parse close bracket
                            int close = Parser.getCloseIdx(dummy, 1, 0);
                            //get the value
                            String sVal = dummy.substring(2, close);
                            //read as numeric
                            float tokenVal = Parser.parseFloat(sVal, 0, 0);
                            actualRotDeg -= tokenVal;
                        }
                        Point2D.Double rotated = GeomUtil.rotate_point(oriPoint.x, oriPoint.y, Math.toRadians(actualRotDeg), anchor.x, anchor.y);
                        //transforming..
                        sll.setX((int) Math.floor(rotated.x));
                        sll.setY((int) Math.floor(rotated.y));

                        sll.getSublayout().setRawAxiom(LSCompacter.simplifyPrefix("+(" + actualRotDeg + ")" + dummy, '+'));
                        sll.getSublayout().getRenderer().generate();
                    } catch (ParseRuleException ex) {
                        Exceptions.printStackTrace(ex);
                    } finally {
                        sll.getSublayout().getRenderer().render();
                    }
                    //adding undoableedit
                    SelectedLayerRotateProvider.SubLayoutRotationUndoableEdit smallEdit = new SelectedLayerRotateProvider.SubLayoutRotationUndoableEdit(
                            sll,
                            new SelectedLayerRotateProvider.PosAxiom(oriPoint, oriAxiom),//original state
                            new SelectedLayerRotateProvider.PosAxiom(sll.getLocation(), sll.getSublayout().getAxiom())//modified state
                    );
                    edit.addEdit(smallEdit);
                }
            }
//done adding small edits
            edit.end();
            GlobalUndoManager.getManager().undoableEditHappened(new UndoableEditEvent(scene, edit));
//            } else {
//
//            }
        }
    }
}
