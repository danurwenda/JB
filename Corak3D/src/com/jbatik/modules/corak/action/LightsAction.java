/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.corak.action;

import com.jbatik.modules.interfaces.Appearance3DChangerCookie;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Bugtracking",
        id = "com.jbatik.modules.corak.action.LightsAction"
)
@ActionRegistration(
        displayName = "#CTL_LightsAction", lazy = false
)
@ActionReference(path = "Menu/View", position = 680, separatorAfter = 699)
@Messages("CTL_LightsAction=Lights")
public final class LightsAction extends AbstractAction implements ActionListener, Presenter.Menu {
    
    private Lookup.Result<Appearance3DChangerCookie> result;
    private Appearance3DChangerCookie context;
    JCheckBoxMenuItem checkbox;
    private final LookupListener listener;
    
    public LightsAction(){
        //init listener
        listener = (LookupEvent ev) -> {
            Collection<? extends Appearance3DChangerCookie> c = result.allInstances();
            if (!c.isEmpty()) {
                context = c.iterator().next();
            } else {
                context = null;
            }
            setEnabled(context != null);
        };
        
        Lookup lkp = Utilities.actionsGlobalContext();
        
        result = lkp.lookupResult(Appearance3DChangerCookie.class);
        result.addLookupListener(listener);
        
        //first time calling
        listener.resultChanged(null);
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        context.setLights(!context.getLightsEnabled());
        getMenuPresenter().setSelected(context.getLightsEnabled());
    }
    
    @Override
    public JMenuItem getMenuPresenter() {
        if (checkbox == null) {
            checkbox = new JCheckBoxMenuItem(Bundle.CTL_LightsAction());
            checkbox.addActionListener(this);
        }
        return checkbox;
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        getMenuPresenter().setEnabled(enabled);
        if (enabled) {
            getMenuPresenter().setSelected(context.getLightsEnabled());
        }        
    }
}
