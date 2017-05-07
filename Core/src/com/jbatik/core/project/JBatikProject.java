/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.core.project;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Properties;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Dimas Y. Danurwenda <dimas@danurwenda.com>
 */
public class JBatikProject implements Project {

    @StaticResource
    public static final String JBATIK_PROJECT_ICON_PATH
            = "com/jbatik/core/project/project-icon.png";//NOI18N
    public static final String REGISTERED_LOOKUP_LOCATION
            = "Projects/com-jbatik-project/Lookup";//NOI18N
    private final FileObject projectDir;
    private final ProjectState state;

    JBatikProject(FileObject projectDirectory, ProjectState state) {
        this.projectDir = projectDirectory;
        this.state = state;
    }

    @Override
    public FileObject getProjectDirectory() {
        return projectDir;
    }

    private Lookup lkp;

    @Override
    public Lookup getLookup() {
        if (lkp == null) {
            lkp = LookupProviderSupport.createCompositeLookup(Lookups.fixed(new Object[]{
                this, //handy to expose a project in its own lookup
                state, //allow outside code to mark the project as needing saving
                new JBatikActionProvider(this), //Provides standard actions like Build and Clean
                loadProperties(), //The project properties
                new Info(), //Project information implementation
                new JBatikProjectLogicalView(this), //Logical view of project implementation
                new JBatikProjectOperations(this),//Implementation on how to delete a JBatik Project
            }),REGISTERED_LOOKUP_LOCATION);
        }
        return lkp;
    }

    private Properties loadProperties() {

        FileObject fob = projectDir.getFileObject(JBatikProjectFactory.PROJECT_DIR
                + "/" + JBatikProjectFactory.PROJECT_PROPFILE);

        Properties properties = new NotifyProperties(state);
        if (fob != null) {
            try {
                properties.load(fob.getInputStream());
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }

        return properties;

    }

    private static class NotifyProperties extends Properties {

        private final ProjectState state;

        NotifyProperties(ProjectState state) {
            this.state = state;
        }

        @Override
        public Object put(Object key, Object val) {

            Object result = super.put(key, val);

            if (((result == null) != (val == null)) || (result != null
                    && val != null && !val.equals(result))) {
                state.markModified();
            }

            return result;

        }

    }

    /**
     * Implementation of project system's ProjectInformation class
     */
    private final class Info implements ProjectInformation {

        @Override
        public Icon getIcon() {
            return new ImageIcon(ImageUtilities.loadImage(
                    JBATIK_PROJECT_ICON_PATH));
        }

        @Override
        public String getName() {
            return getProjectDirectory().getNameExt();
        }

        @Override
        public String getDisplayName() {
            return getName();
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener pcl) {
            //do nothing, won't change
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener pcl) {
            //do nothing, won't change
        }

        @Override
        public Project getProject() {
            return JBatikProject.this;
        }

    }

}
