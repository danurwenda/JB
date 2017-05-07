/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.explorer.node;

import com.jbatik.core.api.GlobalUndoManager;
import static com.jbatik.modules.layout.explorer.node.Bundle.*;
import com.jbatik.modules.layout.layering.SubLayout;
import com.jbatik.modules.layout.layering.SubLayoutLayer;
import java.io.IOException;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.UndoableEdit;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Dimas Y. Danurwenda
 */
public class SubLayoutLayerNode extends LayoutLayerNode {

    SubLayoutLayerNode(InstanceContent c, LayerList model) {
        super(Children.LEAF, c, model);
    }

    /**
     * TODO : Icon of SubLayoutLayerNode is essentially a thumbnail version of
     * the sublayout's appearance on blank, transparent canvas. Max size of the
     * icon is 32x32 px.
     *
     * Ini sebenarnya kalau kainnya udah besar, thumbnail ini ga guna sih, ga
     * keliatan juga gambarnya apa.
     *
     * See Photoshop for references.
     *
     * @param type
     * @return
     */
//    @Override
//    public Image getIcon(int type) {
//        return super.getIcon(type); //To change body of generated methods, choose Tools | Templates.
//    }
    @NbBundle.Messages({
        "layerdialog.title=Confirmation",
        "# {0} - layer name",
        "layerdialog.message=Delete the layer \"{0}\"?"
    })
    @Override
    public void doDelete(boolean withWarning) throws IOException {
        boolean cancelled = false;
        UndoableEdit del = new DeleteSimpleLayoutLayerUndoableEdit(getLayer(), model);
        if (withWarning) {
            Object result = DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Confirmation(
                            layerdialog_message(getDisplayName()),
                            layerdialog_title(),
                            NotifyDescriptor.YES_NO_OPTION,
                            NotifyDescriptor.INFORMATION_MESSAGE
                    )
            );
            if (NotifyDescriptor.YES_OPTION != result) {
                cancelled = true;
            }
        }
        if (!cancelled) {
            destroy();
            GlobalUndoManager.getManager().undoableEditHappened(
                    new UndoableEditEvent(model, del)
            );
        }
    }

    @Override
    protected Sheet createSheet() {
        Sheet s = super.createSheet();

        s.put(createSublayoutSet());
        return s;

    }

    @NbBundle.Messages({
        "CTL_SublayoutProperties=Sublayout",
        "HINT_SublayoutProperties=The properties of selected sublayout(s)",})
    private Sheet.Set createSublayoutSet() {
        Sheet.Set set = new Sheet.Set();
        set.setName(CTL_SublayoutProperties());
        set.setDisplayName(CTL_SublayoutProperties());
        set.setShortDescription(HINT_SublayoutProperties());
        //add properties from sublayout
        SubLayoutLayer sll = getLookup().lookup(SubLayoutLayer.class);
        try {
            //position
            Property x = new PropertySupport.Reflection(sll, int.class, SubLayout.X_PROP);
            Property y = new PropertySupport.Reflection(sll, int.class, SubLayout.Y_PROP);
            set.put(x);
            set.put(y);
            //L-System properties
            Property iter = new PropertySupport.Reflection(sll, int.class, SubLayout.ITERATION_PROP);
            Property angle = new PropertySupport.Reflection(sll, int.class, SubLayout.ANGLE_PROP);
            angle.setDisplayName("( A ) ANGLE");
            Property length = new PropertySupport.Reflection(sll, int.class, SubLayout.LENGTH_PROP);
            length.setDisplayName("( E ) LENGTH");
            Property width = new PropertySupport.Reflection(sll, int.class, SubLayout.WIDTH_PROP);
            width.setDisplayName("( W ) WIDTH");
            //special
            Property sqrot = new PropertySupport.Reflection(sll, float.class, SubLayout.SQROT_PROP);
            //multiplier
            Property angleMultiplier = new PropertySupport.Reflection(sll, float.class, SubLayout.ANGLE_MULT_PROP);
            angleMultiplier.setDisplayName("( ; ) INCREMENT ANGLE");
            angleMultiplier.setShortDescription("Angle Multiplier");
            Property lengthMultiplier = new PropertySupport.Reflection(sll, float.class, SubLayout.LENGTH_MULT_PROP);
            lengthMultiplier.setDisplayName("( \" ) INCREMENT LENGTH");
            lengthMultiplier.setShortDescription("Length Multiplier");
            Property widthMultiplier = new PropertySupport.Reflection(sll, float.class, SubLayout.WIDTH_MULT_PROP);
            widthMultiplier.setDisplayName("( ? ) INCREMENT WIDTH");
            widthMultiplier.setShortDescription("Width Multiplier");
            set.put(iter);
            set.put(angle);
            set.put(sqrot);
            set.put(length);
            set.put(width);
            set.put(angleMultiplier);
            set.put(lengthMultiplier);
            set.put(widthMultiplier);
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        }
        return set;
    }

}
