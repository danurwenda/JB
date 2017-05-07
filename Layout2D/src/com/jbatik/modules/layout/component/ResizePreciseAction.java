/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.component;

import com.jbatik.core.format.DotDecimalFormat;
import com.jbatik.lsystem.parser.exceptions.ParseRuleException;
import com.jbatik.lsystem.util.LSCompacter;
import com.jbatik.modules.layout.layering.SubLayout;
import com.jbatik.modules.layout.visual.LayoutScene;
import com.jbatik.modules.layout.visual.widgets.SubLayoutWidget;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.undo.CompoundEdit;
import org.netbeans.api.visual.widget.Widget;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Resize selected layers by, or set the angle modifier on axiom to, a specified
 * degree
 *
 * @author danur
 */
class ResizePreciseAction extends AbstractAction {

    // PENDING change to icons provided by Dusan
    private static final String ICON_BASE = "com/jbatik/modules/layout/component/resources/resizep.png"; // NOI18N

    private LayoutScene scene;

    public ResizePreciseAction(LayoutScene scene) {
        this.scene = scene;
        putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, true));
        putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(ResizePreciseAction.class, "CTL_ResizePreciseAction")); // NOI18N
    }
    private CompoundEdit edit;

    @Override
    public void actionPerformed(ActionEvent e) {
        Rectangle ori = scene.getSelectedRectangle();
        ResizePrecisePanel box = new ResizePrecisePanel(ori.width, ori.height);
        Object result
                = DialogDisplayer.getDefault().notify(
                        new DialogDescriptor(
                                box, "Resize Layers")
                );

        if (result != NotifyDescriptor.OK_OPTION) {
            return;
        }
        int newW = box.getNewWidth();
        int newH = box.getNewHeight();
        double ratio = (newW * 1.0d / ori.width + newH * 1.0d / ori.height) / 2;
        int anchor = box.getAnchor();
        Rectangle selectedRekt = scene.getSelectedRectangle();
        Point fixedPoint = getAnchorPoint(selectedRekt, anchor);
        for (Object sll : scene.getSelectedObjects()) {
            Widget w = scene.findWidget(sll);
            if (w instanceof SubLayoutWidget && w.isEnabled()) {
                SubLayout sl = w.getLookup().lookup(SubLayout.class);
                if (sl != null) {
                    //find delta from fixedpoint
                    Point wLocOnScene = w.getParentWidget().convertLocalToScene(w.getPreferredLocation());
                    double dx = wLocOnScene.getX() - fixedPoint.getX();
                    double dy = wLocOnScene.getY() - fixedPoint.getY();
                    Point endLocOnScene = new Point((int) (fixedPoint.x + ratio * dx), (int) (fixedPoint.y + ratio * dy));
                    Point endLocOnParent = w.getParentWidget().convertSceneToLocal(endLocOnScene);
                    w.setPreferredLocation(endLocOnParent);
                    sl.setLocation(endLocOnParent);
                    try {
                        sl.setRawAxiom(getModifiedAxiom(sl.getAxiom(), ratio));
                        sl.getRenderer().generate();
                    } catch (ParseRuleException ex) {
                        DialogDisplayer.getDefault().notify(
                                new NotifyDescriptor.Message(ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE)
                        );
                    } finally {
                        sl.getRenderer().render();
                    }
                }
            }
        }
    }

    private static String getModifiedAxiom(String axiom, double ratio) throws ParseRuleException {
        String res = axiom;
        DecimalFormat fourDigit = new DecimalFormat("###.####", DotDecimalFormat.getSymbols());
        String formattedRatio = fourDigit.format(ratio);
        res = "\"(" + formattedRatio + ")" + res;
        res = "?(" + formattedRatio + ")" + res;
        return LSCompacter.simplifyPrefix(res, '"', '?');
    }

    private Point getAnchorPoint(Rectangle r, int anchor) {
        switch (anchor) {
            case 1:
                return r.getLocation();
            case 3:
                return new Point(r.x + r.width, r.y);
            case 9:
                return new Point(r.x + r.width, r.y + r.height);
            case 7:
                return new Point(r.x, r.y + r.height);
            case 8:
                return new Point(r.x + r.width / 2, r.y + r.height);
            case 4:
                return new Point(r.x, r.y + r.height / 2);
            case 6:
                return new Point(r.x + r.width, r.y + r.height / 2);
            case 2:
                return new Point(r.x + r.width / 2, r.y);
            default:
                return new Point(r.x + r.width / 2, r.y + r.height / 2);
        }
    }
}
