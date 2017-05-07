/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.filetype.lay;

import java.io.IOException;
import org.netbeans.api.actions.Savable;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.SaveAsCapable;

/**
 * Take care of Save, while the other capabilities will still be handled by
 * default <code>DataEditorSupport</code>.
 *
 * @author RAPID02
 */
class LayoutEditorSupport implements
        //        OpenCookie,
        SaveAsCapable {

    private final LayoutDataObject dataObject;

    public LayoutEditorSupport(LayoutDataObject obj) {
        dataObject = obj;
    }

    /**
     * Note that fileName *might* contains extention already.
     *
     * @param folder
     * @param fileName
     * @throws IOException
     */
    @Override
    public void saveAs(FileObject folder, String fileName) throws IOException {
//        System.err.println("INI CERITANYA SAVE AS");
        DataObject newDob ;

        DataObject currentDob = getDataObject();
        FileObject currentFile = currentDob.getPrimaryFile();
        String newExt = FileUtil.getExtension(fileName);
        if (!newExt.equals(currentFile.getExt())) {
            newExt = currentFile.getExt();
        } else {
            newExt = "";
        }
        String tempExt = "temp";
        //selamatkan file yang sekarang,
        if (currentDob.isModified() && currentDob.getLookup().lookup(Savable.class) != null) {
            String oldName = currentFile.getName();
            String oldExt = currentFile.getExt();
            //tulis ke file temporary fTemp
            String tempName = FileUtil.findFreeFileName(folder, oldName, tempExt);
            FileObject tempFile = FileUtil.copyFile(currentFile, folder, tempName, tempExt);
            //ambil savable dari yang do
            Savable sav = currentDob.getLookup().lookup(Savable.class);
            //savable.save
            sav.save();
            //ambil file yang sekarang, yang sudah ke save, rename sesuai fileName
            FileObject newFile = currentFile.copy(folder, fileName, newExt);
            //assign new data object
            newDob = DataObject.find(newFile);
            if (null != newDob) {
                OpenCookie newOC = newDob.getLookup().lookup(OpenCookie.class);
                CloseCookie oldCC = currentDob.getLookup().lookup(CloseCookie.class);
                if (oldCC != null) {
                    //tutup2 yang lama
                    oldCC.close();
                    //hapus file yang lama, biar namanya available buat tempFile
                    currentFile.delete();
                    //rename fTemp jadi file yang dulu
                    tempFile.copy(folder, oldName, oldExt);
                    //hapus fTemp
                    tempFile.delete();
                }
                if (newOC != null) {
                    //open the new one
                    newOC.open();
                }
            }
        } else {
            //tulis ke fileName
            FileObject newFile = currentFile.copy(folder, fileName, newExt);
            newDob = DataObject.find(newFile);
            if (null != newDob) {
                OpenCookie newOC = newDob.getLookup().lookup(OpenCookie.class);
                CloseCookie oldCC = currentDob.getLookup().lookup(CloseCookie.class);
                if (oldCC != null) {
                    //tutup2 yang lama
                    oldCC.close();
                }
                if (newOC != null) {
                    //open the new one
                    newOC.open();
                }
            }
        }
    }

    private DataObject getDataObject() {
        return dataObject;
    }

    private String getFileNameNoExtension(String fileName) {
        int index = fileName.lastIndexOf("."); // NOI18N

        if (index == -1) {
            return fileName;
        } else {
            return fileName.substring(0, index);
        }
    }

//    /**
//     * Open Multiview related to LayoutDataObject's mime
//     */
//    @Override
//    public void open() {
//        System.err.println("ini ceritanya buka file");
//    }
}
