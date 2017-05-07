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
        category = "Edit",
        id = "com.jbatik.modules.corak.action.ChangeAppearanceAction"
)
@ActionRegistration(
        displayName = "#CTL_ChangeAppearanceAction", lazy = false
)
@ActionReference(path = "Menu/View", position = 676)
@Messages("CTL_ChangeAppearanceAction=Appearance")
public final class ChangeAppearanceAction extends AbstractAction implements ActionListener, Presenter.Menu{

    private Lookup.Result<Appearance3DChangerCookie> result;
    private Appearance3DChangerCookie context;
    
    JRadioButtonMenuItem wireframeButton;
    JRadioButtonMenuItem solidButton;
    JRadioButtonMenuItem textureButton;
    JMenu submenu;

    public ChangeAppearanceAction() {
        setEnabled(context != null);
        Lookup lkp = Utilities.actionsGlobalContext();
        result = lkp.lookupResult(Appearance3DChangerCookie.class);
        result.addLookupListener(new LookupListener() {
            
            @Override
            public void resultChanged(LookupEvent ev) {
                Collection<? extends Appearance3DChangerCookie> c = result.allInstances();
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
        if (ev.getSource() == wireframeButton) {
            context.changeAppearance(Appearance3DChangerCookie.Appearance.WIREFRAME);
            wireframeButton.setSelected(true);
            solidButton.setSelected(false);
            textureButton.setSelected(false);
        } else if (ev.getSource() == solidButton) {
            context.changeAppearance(Appearance3DChangerCookie.Appearance.SOLID);
            solidButton.setSelected(true);
            wireframeButton.setSelected(false);
            textureButton.setSelected(false);
        } else if (ev.getSource() == textureButton) {
            context.changeAppearance(Appearance3DChangerCookie.Appearance.TEXTURE);
            textureButton.setSelected(true);
            wireframeButton.setSelected(false);
            solidButton.setSelected(false);
        }
    }

    @Override
    public JMenu getMenuPresenter() {
        if (submenu == null) {
            submenu = new JMenu(NbBundle.getMessage(ChangeAppearanceAction.class, "CTL_ChangeAppearanceAction"));
            wireframeButton = new JRadioButtonMenuItem("Wireframe");
            wireframeButton.addActionListener(this);
            solidButton = new JRadioButtonMenuItem("Solid");
            solidButton.addActionListener(this);
            textureButton = new JRadioButtonMenuItem("Texture");
            textureButton.addActionListener(this);
            submenu.add(wireframeButton);
            submenu.add(solidButton);
            submenu.add(textureButton);
        }
        if (context != null) {
            if (context.getCurrentAppearance() == Appearance3DChangerCookie.Appearance.WIREFRAME)
                wireframeButton.doClick();
            else if (context.getCurrentAppearance() == Appearance3DChangerCookie.Appearance.SOLID)
                solidButton.doClick();
            else if (context.getCurrentAppearance() == Appearance3DChangerCookie.Appearance.TEXTURE)
                textureButton.doClick();
        } else {
            wireframeButton.setSelected(false);
            solidButton.setSelected(false);
            textureButton.setSelected(false);
        }
        return submenu;
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        getMenuPresenter().setEnabled(enabled);
    }
}
