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
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;
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

@ActionID(
        category = "View",
        id = "com.jbatik.modules.corak.action.ChangeProjectionModeAction"
)
@ActionRegistration(
        displayName = "#CTL_ChangeProjectionModeAction", lazy = false
)
@ActionReference(path = "Menu/View", position = 668)
@Messages("CTL_ChangeProjectionModeAction=Projection Mode")
public class ChangeProjectionModeAction extends AbstractAction implements ActionListener, Presenter.Menu {

    private Lookup.Result<Scene3DObserverCookie> result;
    private Scene3DObserverCookie context;
    
    JRadioButtonMenuItem perspectiveButton;
    JRadioButtonMenuItem parallelButton;
    JMenu submenu;

    public ChangeProjectionModeAction() {
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
        if (ev.getSource() == perspectiveButton) {
            context.setProjectionMode(Scene3DObserverCookie.Projection.PERSPECTIVE);
            perspectiveButton.setSelected(true);
            parallelButton.setSelected(false);
        } else if (ev.getSource() == parallelButton) {
            context.setProjectionMode(Scene3DObserverCookie.Projection.PARALLEL);
            parallelButton.setSelected(true);
            perspectiveButton.setSelected(false);
        }
    }

    @Override
    public JMenu getMenuPresenter() {
        if (submenu == null) {
            submenu = new JMenu(NbBundle.getMessage(ChangeProjectionModeAction.class, "CTL_ChangeProjectionModeAction"));
            perspectiveButton = new JRadioButtonMenuItem("Perspective");
            perspectiveButton.addActionListener(this);
            parallelButton = new JRadioButtonMenuItem("Axonometric");
            parallelButton.addActionListener(this);
            submenu.add(perspectiveButton);
            submenu.add(parallelButton);
        }
        if (context != null) {
            if (context.getProjectionMode() == Scene3DObserverCookie.Projection.PERSPECTIVE)
                perspectiveButton.setSelected(true);
            else if (context.getProjectionMode() == Scene3DObserverCookie.Projection.PARALLEL)
                parallelButton.setSelected(true);
        } else {
            perspectiveButton.setSelected(false);
            parallelButton.setSelected(false);
        }
        return submenu;
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        getMenuPresenter().setEnabled(enabled);
    }
}
