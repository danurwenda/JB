/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.core.project;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import javax.swing.ImageIcon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectFactory2;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Dimas Y. Danurwenda <dimas@danurwenda.com>
 */
@ServiceProvider(service = ProjectFactory.class)
public class JBatikProjectFactory implements ProjectFactory2 {

    public static final String PROJECT_DIR = "jbatikproject";
    public static final String PROJECT_PROPFILE = "project.properties";

    /**
     * This method needs to be very fast—it should determine whether or not a
     * directory is a project as quickly as possible, because it will be called
     * once for each directory shown in the file chooser when the user selects
     * File > Open Project.
     *
     * This simple test for the presence of a subdirectory called
     * "jbatikproject" is all we need to determine that something is not one of
     * our projects.
     *
     * @param projectDirectory
     * @return
     */
    @Override
    public boolean isProject(FileObject projectDirectory) {
        return projectDirectory.getFileObject(PROJECT_DIR) != null;
    }

    /**
     * The code that actually loads a project, given a directory. The project
     * system handles caching of projects, so all that's needed here is to
     * create a new project
     *
     * @param projectDirectory
     * @param state
     * @return
     * @throws IOException
     */
    @Override
    public Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException {
        return isProject(projectDirectory) ? new JBatikProject(projectDirectory, state) : null;
    }

    /**
     * This is what will write out any unsaved changes to disk when a project is
     * closed, or when the application shuts down
     *
     * @param project
     * @throws IOException
     * @throws ClassCastException
     */
    @Override
    public void saveProject(Project project) throws IOException, ClassCastException {
        FileObject projectRoot = project.getProjectDirectory();
        if (projectRoot.getFileObject(PROJECT_DIR) == null) {
            throw new IOException("Project dir " + projectRoot.getPath() + " deleted,"
                    + " cannot save project");
        }

        //Find the properties file pvproject/project.properties,
        //creating it if necessary
        String propsPath = PROJECT_DIR + "/" + PROJECT_PROPFILE;
        FileObject propertiesFile = projectRoot.getFileObject(propsPath);
        if (propertiesFile == null) {
            //Recreate the properties file if needed
            propertiesFile = projectRoot.createData(propsPath);
        }

        Properties properties = project.getLookup().lookup(Properties.class);
        File f = FileUtil.toFile(propertiesFile);
        properties.store(new FileOutputStream(f), "JBatik Project Properties");
    }

    @Override
    public ProjectManager.Result isProject2(FileObject projectDirectory) {
        if (isProject(projectDirectory)) {
            return new ProjectManager.Result(new ImageIcon(
                    ImageUtilities.loadImage(JBatikProject.JBATIK_PROJECT_ICON_PATH)));
        }
        return null;
    }

}
