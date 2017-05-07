/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.corak.newfile;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;

public class NewCorakWizardPanel1 implements ChangeListener,
        WizardDescriptor.Panel<WizardDescriptor> {

    public static String WIZARD_KEY_TARGET_NAME = "NAME";//NOI18N
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private NewCorakVisualPanel1 component;
    private Project project;
    private WizardDescriptor wizard;

    NewCorakWizardPanel1(Project project, WizardDescriptor wizard) {
        this.wizard = wizard;
        this.project = project;
    }

    public Project getProject() {
        return project;
    }

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public NewCorakVisualPanel1 getComponent() {
        if (component == null) {
            component = new NewCorakVisualPanel1(this);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx("help.key.here");
    }

    @Override
    public boolean isValid() {
        return getComponent().valid(wizard);
    }

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    @Override
    public final void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        changeSupport.fireChange();
    }

    @Override
    public void readSettings(WizardDescriptor wiz) {
        // use wiz.getProperty to retrieve previous panel state
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        // use wiz.putProperty to remember current panel state
        wiz.putProperty(NewCorakWizardPanel1.WIZARD_KEY_TARGET_NAME, getNewCorakName());
    }

    private String getNewCorakName() {
        return getComponent().getNewCorakName();
    }

}
