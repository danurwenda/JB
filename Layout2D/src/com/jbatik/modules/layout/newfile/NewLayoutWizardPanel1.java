/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.newfile;

import java.text.ParseException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;

public class NewLayoutWizardPanel1 implements ChangeListener,
        WizardDescriptor.Panel<WizardDescriptor> {

    public static String WIZARD_KEY_TARGET_NAME = "NAME";//NOI18N
    public static String WIDTH = "WIDTH";//NOI18N
    public static String HEIGHT = "HEIGHT";//NOI18N
    public static String DPI = "DPI";//NOI18N
    public static String UNIT = "UNIT";//NOI18N
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private NewLayoutVisualPanel1 component;
    private Project project;
    private WizardDescriptor wizard;

    public NewLayoutWizardPanel1(Project project, WizardDescriptor wizard) {
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
    public NewLayoutVisualPanel1 getComponent() {
        if (component == null) {
            component = new NewLayoutVisualPanel1(this);
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
    public void addChangeListener(ChangeListener l) {
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
        wiz.putProperty(WIZARD_KEY_TARGET_NAME, getNewLayoutName());
        wiz.putProperty(WIDTH, getNewLayoutWidth());
        wiz.putProperty(HEIGHT, getNewLayoutHeight());
        wiz.putProperty(DPI, getNewLayoutDPI());
        wiz.putProperty(UNIT, getNewLayoutUnit());
    }

    private String getNewLayoutUnit() {
        return getComponent().getNewLayoutUnit();
    }

    private String getNewLayoutName() {
        return getComponent().getNewLayoutName();
    }

    private double getNewLayoutWidth() {
        try {
            return getComponent().getNewLayoutWidth();
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        return 0;
    }

    private double getNewLayoutHeight() {
        try {
            return getComponent().getNewLayoutHeight();
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        return 0;
    }

    private Long getNewLayoutDPI() {
        try {
            return getComponent().getNewLayoutDPI();
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        return 0l;
    }

}
