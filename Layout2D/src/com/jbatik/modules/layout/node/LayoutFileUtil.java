/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.node;

import com.jbatik.core.project.JBatikProject;
import com.jbatik.modules.layout.api.DefaultImageLibrary;
import com.jbatik.modules.layout.api.LayoutLibrary;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author RAPID02
 */
public class LayoutFileUtil {

    public static final String LAYOUTS_DIR = "lays"; //NOI18N
    public static final String LIBRARIES_DIR = "libs"; //NOI18N

    /**
     * Given a JBatikProject project, returns the FileObject of the directory
     * containing layout files
     *
     * @param project the JBatikProject in question
     * @param create if set to true, then it will try to create the directory in
     * case the directory is missing
     * @return the FileObject representing the layout directory
     */
    public static final FileObject getLayoutsFolder(Project project, boolean create) {
        FileObject projectDir = project.getProjectDirectory();
        FileObject result
                = projectDir.getFileObject(LAYOUTS_DIR);

        if (result == null && create) {
            try {
                result = projectDir.createFolder(LAYOUTS_DIR);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        return result;
    }

    /**
     * Given a JBatikProject project, returns the FileObject of the directory
     * containing library image files
     *
     * @param project the JBatikProject in question
     * @param create if set to true, then it will try to create the directory in
     * case the directory is missing
     * @return the FileObject representing the library directory
     */
    public static final FileObject getLibrariesFolder(Project project, boolean create) {
        FileObject projectDir = project.getProjectDirectory();
        FileObject result
                = projectDir.getFileObject(LIBRARIES_DIR);

        if (result == null && create) {
            FileObject zip = FileUtil.getConfigFile("Templates/JBatik/libs.zip");
            try (
                    InputStream in = zip.getInputStream();
                    ZipInputStream zipStream = new ZipInputStream(in)) {
                result = projectDir.createFolder(LIBRARIES_DIR);
                //add few cor files from the zip in packages
                //extract the zip
                ZipEntry entry;
                while ((entry = zipStream.getNextEntry()) != null) {

                    // Once we get the entry from the stream, the stream is
                    // positioned read to read the raw data, and we keep
                    // reading until read returns 0 or less.                        
                    //create data at destination folder
                    try (OutputStream output = result.createAndOpen(entry.getName())) {
                        FileUtil.copy(zipStream, output);
                    }
                }
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        return result;
    }

    /**
     * Return image (possibly cached) from libraries directory of a given
     * projectPath and image name.
     *
     * @param projectPath
     * @param imageName
     * @return
     */
    public static LayoutLibrary getLibraryImage(String projectPath, String imageName) {
        if (projectPath == null || imageName == null) {
            return null;
        }
        //projectPath is in the form of C:\Users\RAPID02\Desktop\Project 3.4
        File projectDir = new File(projectPath);
        FileObject projectDirFO = FileUtil.toFileObject(projectDir);
        JBatikProject proj = (JBatikProject) FileOwnerQuery.getOwner(projectDirFO);

        //trying to get the library caching
        LibraryHandler h = proj.getLookup().lookup(LibraryHandler.class);
        if (h != null) {
            //handle was successfully injected and still exist
            return h.getLibrary(imageName);
        } else {
            System.err.println("not even close");
            FileObject libDirFO = LayoutFileUtil.getLibrariesFolder(proj, true);
            //to get the full image
            LayoutLibrary image = null;
            FileObject libFO = libDirFO.getFileObject(imageName);
            if (libFO != null) {
                return new DefaultImageLibrary(libFO);
            } else {
                return new DefaultImageLibrary(libDirFO,imageName);
            }
        }
    }
}
