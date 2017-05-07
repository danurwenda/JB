/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.cor.navigator.table.editor;

import com.jbatik.core.api.component.ImageFileChooser;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.windows.WindowManager;

/**
 *
 * @author RAPID02
 */
public class FileTextureEditor extends AbstractCellEditor
        implements TableCellEditor {

    File currentFile;
    FileObject textureFolder;
    JButton button;
    JFileChooser fc;
    protected static final String EDIT = "edit";

    public FileTextureEditor() {
        //Set up the editor (from the table's point of view),
        //which is a button.
        //This button brings up the color chooser dialog,
        //which is the editor from the user's point of view.
        button = new JButton();
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorderPainted(false);
        button.setActionCommand(EDIT);
        button.addActionListener((ActionEvent e) -> {
            if (textureFolder != null) {
                if (fc == null) {
                    fc = new ImageFileChooser(FileUtil.toFile(textureFolder));
                }

                //Show it.
                int returnVal = fc.showDialog(WindowManager.getDefault().getMainWindow(), "Choose Texture");

                //Process the results.
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    boolean failed = false;
                    //copy selected file to texture folder, if necessary
                    //pertama2 cek apakah ini ngambil dari texture folder
                    //jika udah ngambil dari texture folder, nothing to do
                    File ori = fc.getSelectedFile();

                    FileObject selected = FileUtil.toFileObject(ori);
                    if (!FileUtil.isParentOf(textureFolder, selected)) {
                        //jika bukan, copy current file ke texturefolder
                        String ext = selected.getExt();
                        String newName = FileUtil.findFreeFileName(textureFolder, selected.getName(), ext);
                        try (InputStream in = selected.getInputStream();
                                OutputStream out = textureFolder.createAndOpen(newName.concat(".").concat(ext));) {
                            FileUtil.copy(in, out);
                            FileObject result = textureFolder.getFileObject(newName, ext);
                            ori = FileUtil.toFile(result);
                        } catch (IOException ex) {
                            failed = true;
                            Exceptions.printStackTrace(ex);

                        }
                    }
                    if (!failed) {
                        currentFile = ori;
                    }
                    fireEditingStopped();
                } else {
                    fireEditingCanceled();
                }

                //Reset the file chooser for the next time it's shown.
                fc.setSelectedFile(null);
            }
        });
        
        
    }

    @Override
    public Object getCellEditorValue() {
        return currentFile;
    }

    @Override
    public Component getTableCellEditorComponent(
            JTable table, Object file,
            boolean isSelected, int row, int column) {
        currentFile = (File) file;
        return button;
    }

    public void setFolder(FileObject textureFolder) {
        this.textureFolder = textureFolder;
    }
}
