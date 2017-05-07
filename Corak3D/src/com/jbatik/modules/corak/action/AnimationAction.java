/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.corak.action;

import static com.jbatik.modules.corak.action.Bundle.CTL_AnimationAction;
import com.jbatik.modules.interfaces.AnimatibleCookie;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

@ActionID(
        category = "View",
        id = "com.jbatik.modules.corak.action.AnimationAction"
)
@ActionRegistration(
        displayName = "#CTL_AnimationAction", lazy = false
)
@ActionReference(path = "Menu/View", position = 669)
@Messages("CTL_AnimationAction=Animation")
public class AnimationAction extends AbstractAction implements ActionListener, Presenter.Menu  {

    private Lookup.Result<AnimatibleCookie> result;
    private AnimatibleCookie context;
    
    JCheckBoxMenuItem rotateXButton;
    JCheckBoxMenuItem rotateYButton;
    JCheckBoxMenuItem rotateZButton;
    JMenuItem stopButton;
    JMenu submenu;

    public AnimationAction() {
        setEnabled(context != null);
        Lookup lkp = Utilities.actionsGlobalContext();
        result = lkp.lookupResult(AnimatibleCookie.class);
        result.addLookupListener(new LookupListener() {
            
            @Override
            public void resultChanged(LookupEvent ev) {
                Collection<? extends AnimatibleCookie> c = result.allInstances();
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
        if (ev.getSource() == rotateXButton) {
            context.toggleXAnimation();
            rotateXButton.setSelected(context.isAnimatingX());
        } else if (ev.getSource() == rotateYButton) {
            context.toggleYAnimation();
            rotateYButton.setSelected(context.isAnimatingY());
        } else if (ev.getSource() == rotateZButton) {
            context.toggleZAnimation();
            rotateZButton.setSelected(context.isAnimatingZ());
        } else if (ev.getSource() == stopButton) {
            context.stopAnimation();
            rotateXButton.setSelected(false);
            rotateYButton.setSelected(false);
            rotateZButton.setSelected(false);
        }
    }

    @Override
    public JMenu getMenuPresenter() {
        if (submenu == null) {
            submenu = new JMenu(CTL_AnimationAction());
            rotateXButton = new JCheckBoxMenuItem("rotation X");
            rotateXButton.addActionListener(this);
            rotateYButton = new JCheckBoxMenuItem("rotation Y");
            rotateYButton.addActionListener(this);
            rotateZButton = new JCheckBoxMenuItem("rotation Z");
            rotateZButton.addActionListener(this);
            stopButton = new JMenuItem("stop animation");
            stopButton.addActionListener(this);
            submenu.add(rotateXButton);
            submenu.add(rotateYButton);
            submenu.add(rotateZButton);
            submenu.add(stopButton);
        }
        if (context != null) {
            rotateXButton.setSelected(context.isAnimatingX());
            rotateYButton.setSelected(context.isAnimatingY());
            rotateZButton.setSelected(context.isAnimatingZ());
        } else {
            rotateXButton.setSelected(false);
            rotateYButton.setSelected(false);
            rotateZButton.setSelected(false);
        }
        return submenu;
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        getMenuPresenter().setEnabled(enabled);
    }
}
