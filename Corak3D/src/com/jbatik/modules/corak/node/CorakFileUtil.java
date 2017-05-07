/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.corak.node;

import com.jbatik.modules.corak.CorakLSystem;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author RAPID02
 */
public class CorakFileUtil {

    public static final String CORAKS_DIR = "cors"; //NOI18N
    public static final String TEXTURES_DIR = "textures"; //NOI18N

    public static final FileObject getCoraksFolder(Project project, boolean create) {
        FileObject projectDir = project.getProjectDirectory();
        FileObject result = projectDir.getFileObject(CORAKS_DIR);

        if (result == null && create) {
            FileObject zip = FileUtil.getConfigFile("Templates/JBatik/cors.zip");
            try (
                    InputStream in = zip.getInputStream();
                    ZipInputStream zipStream = new ZipInputStream(in)) {
                result = projectDir.createFolder(CORAKS_DIR);
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

    public static final FileObject getDefaultTexture(FileObject textures, boolean create) {
        FileObject result
                = textures.getFileObject(CorakLSystem.DEFAULT_TEXTURE);

        if (result == null && create) {
            try (
                    InputStream in = FileUtil.getConfigFile("Templates/JBatik/blank.png").getInputStream();
                    OutputStream out = textures.createAndOpen("blank.png")) {
                FileUtil.copy(in, out);
                result= textures.getFileObject(CorakLSystem.DEFAULT_TEXTURE);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return result;
    }

    public static final FileObject getTexturesFolder(Project project, boolean create) {
        FileObject projectDir = project.getProjectDirectory();
        FileObject result
                = projectDir.getFileObject(TEXTURES_DIR);

        if (result == null && create) {
            FileObject zip = FileUtil.getConfigFile("Templates/JBatik/textures.zip");
            try (
                    InputStream in = zip.getInputStream();
                    ZipInputStream zipStream = new ZipInputStream(in)) {
                result = projectDir.createFolder(TEXTURES_DIR);
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
}
