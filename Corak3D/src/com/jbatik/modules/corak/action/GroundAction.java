/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.corak.action;

import com.jbatik.modules.interfaces.Archi3DObserverCookie;
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
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

//@ActionID(
//        category = "View",
//        id = "com.jbatik.modules.corak.action.GroundAction"
//)
//@ActionRegistration(
//        displayName = "#CTL_GroundAction", lazy = false
//)
//@ActionReference(path = "Menu/View", position = 652)
@Messages("CTL_GroundAction=Show Ground")
public class GroundAction extends AbstractAction implements ActionListener, Presenter.Menu {
    
    private Lookup.Result<Archi3DObserverCookie> result;
    private Archi3DObserverCookie context;
    JCheckBoxMenuItem checkbox;
    
    public GroundAction(){
        
        setEnabled(context != null);
        Lookup lkp = Utilities.actionsGlobalContext();
        
        result = lkp.lookupResult(Archi3DObserverCookie.class);
        result.addLookupListener(new LookupListener() {
            
            @Override
            public void resultChanged(LookupEvent ev) {
                Collection<? extends Archi3DObserverCookie> c = result.allInstances();
                if (!c.isEmpty()) {
                    context = c.iterator().next();
                } else {
                    context = null;
                }
                setEnabled(context != null);
            }
            
        });
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        context.setGround(!context.getGroundEnabled());
        getMenuPresenter().setSelected(context.getGroundEnabled());
    }
    
    @Override
    public JMenuItem getMenuPresenter() {
        if (checkbox == null) {
            checkbox = new JCheckBoxMenuItem(NbBundle.getMessage(GroundAction.class, "CTL_GroundAction"));
            checkbox.addActionListener(this);
        }
        return checkbox;
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        getMenuPresenter().setEnabled(enabled);
        if (enabled) {
            getMenuPresenter().setSelected(context.getGroundEnabled());
        }
        
    }
    
}
