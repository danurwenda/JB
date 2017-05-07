/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.corak.action;

import com.jbatik.modules.interfaces.Scene3DObserverCookie;
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
//        id = "com.jbatik.modules.corak.action.AxisLinesAction"
//)
//@ActionRegistration(
//        displayName = "#CTL_AxisLinesAction", lazy = false
//)
//@ActionReference(path = "Menu/View", position = 650)
@Messages("CTL_AxisLinesAction=Show Axis Lines")
public class AxisLinesAction extends AbstractAction implements ActionListener, Presenter.Menu {
    
    private Lookup.Result<Scene3DObserverCookie> result;
    private Scene3DObserverCookie context;
    JCheckBoxMenuItem checkbox;
    
    public AxisLinesAction(){
        
        setEnabled(context != null);
        Lookup lkp = Utilities.actionsGlobalContext();
        
        result = lkp.lookupResult(Scene3DObserverCookie.class);
        result.addLookupListener(new LookupListener() {
            
            @Override
            public void resultChanged(LookupEvent ev) {
                Collection<? extends Scene3DObserverCookie> c = result.allInstances();
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
        context.setAxisLines(!context.getAxisLinesEnabled());
        getMenuPresenter().setSelected(context.getAxisLinesEnabled());
    }
    
    @Override
    public JMenuItem getMenuPresenter() {
        if (checkbox == null) {
            checkbox = new JCheckBoxMenuItem(NbBundle.getMessage(AxisLinesAction.class, "CTL_AxisLinesAction"));
            checkbox.addActionListener(this);
        }
        return checkbox;
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        getMenuPresenter().setEnabled(enabled);
        if (enabled) {
            getMenuPresenter().setSelected(context.getAxisLinesEnabled());
        }
        
    }
    
}
