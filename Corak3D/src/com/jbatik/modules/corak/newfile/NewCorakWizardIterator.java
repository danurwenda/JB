/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.corak.newfile;

import com.jbatik.modules.corak.node.CorakFileUtil;
import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

@NbBundle.Messages("CorakWizardIterator_dName=Corak File")
public final class NewCorakWizardIterator implements WizardDescriptor.InstantiatingIterator<WizardDescriptor> {

    private static final String CORAK_TEMPLATE_PATH = "Templates/JBatik/CorakTemplate.cor"; //NO18N
    private int index;
    private final Project project;
    private WizardDescriptor wizard;
    private List<WizardDescriptor.Panel<WizardDescriptor>> panels;

    public NewCorakWizardIterator(Project project) {
        this.project = project;
    }

    private List<WizardDescriptor.Panel<WizardDescriptor>> getPanels() {
        if (panels == null) {
            panels = new ArrayList<>();
            panels.add(new NewCorakWizardPanel1(project, wizard));
            String[] steps = new String[panels.size()];
            for (int i = 0; i < panels.size(); i++) {
                Component c = panels.get(i).getComponent();
                if (steps[i] == null) {
                    // Default step name to component name of panel. Mainly
                    // useful for getting the name of the target chooser to
                    // appear in the list of steps.
                    steps[i] = c.getName();
                }
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                    jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
                }
            }
        }
        return panels;
    }

    @Override
    public Set<?> instantiate() throws IOException {
        //Get the new file name:
        String targetName = (String) wizard.getProperty(NewCorakWizardPanel1.WIZARD_KEY_TARGET_NAME);

        //Specify the target directory
        FileObject corsDirFO = CorakFileUtil.getCoraksFolder(project, false);
        DataFolder corsDirDF = DataFolder.findFolder(corsDirFO);

        //Specify the template defined somewhere in XML layer
        FileObject template = FileUtil.getConfigFile(CORAK_TEMPLATE_PATH);
        DataObject dTemplate = DataObject.find(template);

        //Create new DataObject
        DataObject obj = dTemplate.createFromTemplate(corsDirDF, targetName);

        //Obtain a FileObject:
        FileObject createdFile = obj.getPrimaryFile();

        //Create the new file:
        return Collections.singleton(createdFile);
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        panels = null;
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return getPanels().get(index);
    }

    @Override
    public String name() {
        return index + 1 + ". from " + getPanels().size();
    }

    @Override
    public boolean hasNext() {
        return index < getPanels().size() - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }
}
