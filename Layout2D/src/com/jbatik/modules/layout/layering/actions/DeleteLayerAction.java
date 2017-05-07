/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.layering.actions;

import com.jbatik.modules.layout.explorer.LayerController;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;

/**
 * Action for deleting a LayoutLayer from tree structure. Actually it will only
 * invoke org.openide.actions.DeleteAction; the real deletion process is handled
 * by DeleteActionInterceptor.java, which in turn will call
 * LayoutLayerNode.delete
 *
 * @author RAPID02
 */
@ActionID(
        category = "LayoutExplorer",
        id = "com.jbatik.modules.layout.explorer.actions.DeleteLayerAction"
)
@ActionRegistration(
        displayName = "#CTL_DeleteLayerAction", lazy = false
)
@ActionReferences({
    @ActionReference(path = "LayoutLayer/Action", position = 300)
})
@NbBundle.Messages("CTL_DeleteLayerAction=Delete Layer")

public class DeleteLayerAction extends AbstractAction implements PropertyChangeListener, LookupListener, ContextAwareAction {

    private Action delegate;
    private Lookup context;
    Lookup.Result<LayerController> lcResult;

    public DeleteLayerAction() {
        this(Utilities.actionsGlobalContext());
    }

    private DeleteLayerAction(Lookup context) {
        putValue(Action.NAME, Bundle.CTL_DeleteLayerAction());
        this.context = context;
    }

    void init() {

        if (lcResult != null) {
            return;
        }
        assert SwingUtilities.isEventDispatchThread() : "this shall be called just from AWT thread";

        //The thing we want to listen for the presence or absence of
        //on the global selection
        lcResult = context.lookupResult(LayerController.class);
        lcResult.addLookupListener(this);
        resultChanged(null);
    }

    @Override
    public boolean isEnabled() {
        init();
        if (delegate == null) {
            return false;
        }
        return delegate.isEnabled();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        init();
        delegate.actionPerformed(e);
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        Collection lcs = lcResult.allInstances();
        if (lcs.isEmpty()) {
            setEnabled(false);
        } else {
            //assuming that there'll be only one layercontroller in context
            LayerController lc = (LayerController) lcs.iterator().next();
            if (delegate != null) {
                delegate.removePropertyChangeListener(this);
            }
            ExplorerManager em = lc.getExplorerManager();
            delegate = ExplorerUtils.actionDelete(em, false);
            delegate.addPropertyChangeListener(this);
            setEnabled(delegate.isEnabled());
        }
    }

    @Override
    public Action createContextAwareInstance(Lookup context) {
        return new DeleteLayerAction(context);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("enabled")) {
            //only interested in enabled state
            setEnabled(delegate.isEnabled());
        }
    }
}
