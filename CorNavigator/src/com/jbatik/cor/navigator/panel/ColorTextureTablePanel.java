/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.cor.navigator.panel;

import com.jbatik.cor.navigator.table.editor.FileTextureEditor;
import com.jbatik.cor.navigator.table.ColorTextureTableModel;
import com.jbatik.cor.navigator.table.editor.ColorCellEditor;
import com.jbatik.cor.navigator.table.renderer.ColorCellRenderer;
import com.jbatik.cor.navigator.table.renderer.ImageCellRenderer;
import com.jbatik.cor.navigator.table.editor.LabelCellEditor;
import com.jbatik.cor.navigator.table.renderer.LabelCellRenderer;
import com.jbatik.cor.navigator.PicturePresenter;
import com.jbatik.cor.navigator.table.editor.RemoveCellEditor;
import com.jbatik.cor.navigator.table.renderer.RemoveCellRenderer;
import com.jbatik.cor.navigator.TableRowTransferHandler;
import com.jbatik.filetype.cor.CorakDataObject;
import com.jbatik.modules.corak.CorakLSystem;
import com.jbatik.modules.corak.CorakUtil;
import com.jbatik.modules.corak.io.CorakSavable;
import com.jbatik.modules.corak.io.CorakSerializer;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.undo.UndoManager;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author RAPID02
 */
public class ColorTextureTablePanel extends javax.swing.JPanel {

    Color colors;
    int sum_colors;
    FileObject textureFolder;

    private static final int DATA_OBJECT_MODE = 888;
    private static final int CORAK_MODE = 889;
    private int sourceMode;
    private CorakDataObject sourceDO;
    private CorakLSystem sourceCor;
    private FileTextureEditor fileTextureEditor= new FileTextureEditor();

    public void setTextureFolder(FileObject textureFolder) {
        this.textureFolder = textureFolder;
        fileTextureEditor.setFolder(textureFolder);
    }
    private ColorTextureTableModel colorTableModel = new ColorTextureTableModel();

    /**
     * Creates new form ColorPanel
     */
    public ColorTextureTablePanel() {
        initComponents();
        colorTextureTable.getTableHeader().setReorderingAllowed(false);
        //set table as ListSelectionListener buat drag n drop
        ListSelectionModel listMod = colorTextureTable.getSelectionModel();
        listMod.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listMod.addListSelectionListener(colorTextureTable);
        //set transferhandler
        colorTextureTable.setTransferHandler(new TableRowTransferHandler(colorTextureTable));

        colorTextureTable.getColumnModel().getColumn(0).setCellEditor(new LabelCellEditor(15));
        colorTextureTable.getColumnModel().getColumn(0).setCellRenderer(new LabelCellRenderer(JLabel.CENTER));
        colorTextureTable.getColumnModel().getColumn(0).setHeaderRenderer(new LabelCellRenderer(JLabel.CENTER));
        colorTextureTable.getColumnModel().getColumn(0).setResizable(false);
        colorTextureTable.getColumnModel().getColumn(0).setPreferredWidth(10);
        colorTextureTable.getColumnModel().getColumn(0).setMinWidth(30);
        
        //set editor untuk kolom color, ini kenapa sih ga bisa pakai defaultrenderer?      
//        colorTextureTable.setDefaultEditor(Color.class, new ColorCellEditor());
        
        colorTextureTable.getColumnModel().getColumn(1).setCellEditor(new ColorCellEditor());
        colorTextureTable.getColumnModel().getColumn(1).setCellRenderer(new ColorCellRenderer());
        colorTextureTable.getColumnModel().getColumn(1).setHeaderRenderer(new LabelCellRenderer(JLabel.CENTER));
        colorTextureTable.getColumnModel().getColumn(1).setResizable(false);
        colorTextureTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        colorTextureTable.getColumnModel().getColumn(1).setMinWidth(50);
//        //set editor untuk kolom file
        colorTextureTable.getColumnModel().getColumn(2).setCellEditor(fileTextureEditor);
        colorTextureTable.getColumnModel().getColumn(2).setCellRenderer(new ImageCellRenderer());
        colorTextureTable.getColumnModel().getColumn(2).setHeaderRenderer(new LabelCellRenderer(JLabel.CENTER));
        colorTextureTable.getColumnModel().getColumn(2).setResizable(false);
        colorTextureTable.getColumnModel().getColumn(2).setPreferredWidth(70);
        colorTextureTable.getColumnModel().getColumn(2).setMinWidth(50);

        colorTextureTable.getColumnModel().getColumn(3).setCellEditor(new RemoveCellEditor(colorTextureTable, colorTableModel));
        colorTextureTable.getColumnModel().getColumn(3).setCellRenderer(new RemoveCellRenderer());
        colorTextureTable.getColumnModel().getColumn(3).setHeaderRenderer(new LabelCellRenderer(JLabel.CENTER));
        colorTextureTable.getColumnModel().getColumn(3).setResizable(false);
        colorTextureTable.getColumnModel().getColumn(3).setPreferredWidth(10);
        colorTextureTable.getColumnModel().getColumn(3).setMinWidth(15);
        //set height biar agak bagus dilihat texture nya
        colorTextureTable.setRowHeight(20);
        //biar bisa di drag
        colorTextureTable.setCellSelectionEnabled(true);
        colorTextureTable.setSurrendersFocusOnKeystroke(true);
        
        PicturePresenter.setPicture_Add(addButton);
        addButton.addActionListener((ActionEvent e) -> {
            //add to tablemodel
            colorTableModel.addRow(new Object[]{
                null,
                //random color, feedback from user
                new Color((float)Math.random(),(float)Math.random(),(float)Math.random()),
                new File(FileUtil.toFile(textureFolder), CorakLSystem.DEFAULT_TEXTURE),
                null},true);//2nd parameter means this action is undoable
        });
        setTableListener(new ColorTableModelListener());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPaneColor = new javax.swing.JScrollPane();
        colorTextureTable = new javax.swing.JTable();
        addButton = new javax.swing.JButton();

        colorTextureTable.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        colorTextureTable.setModel(colorTableModel);
        colorTextureTable.setDragEnabled(true);
        colorTextureTable.setDropMode(javax.swing.DropMode.ON);
        jScrollPaneColor.setViewportView(colorTextureTable);

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(ColorTextureTablePanel.class, "ColorTextureTablePanel.addButton.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPaneColor, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(addButton)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPaneColor, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addButton)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JTable colorTextureTable;
    private javax.swing.JScrollPane jScrollPaneColor;
    // End of variables declaration//GEN-END:variables

    public void setTableSource(CorakDataObject currentDataObject, CorakLSystem cor) {
        this.sourceDO = currentDataObject;//tetap perlu buat taruh savable
        this.sourceCor = cor;
        this.sourceMode = CORAK_MODE;
        //sebelum kita acak2 si table nya dengan data dari source yang baru,
        //kita cabut dulu tablemodellistenernya
        if (currentTableListener != null) {
            colorTableModel.removeTableModelListener(currentTableListener);
        }
        //dan undoredo nya
        colorTableModel.setUndoEnabled(false);
        //flush table model
        colorTableModel.setRowCount(0);
        //ambil list warnanya & texture nya
        List<Color> cs = cor.getColors();
        List<String> ts = cor.getTextures();

        //ambil folder texture nya, harusnya sih sudah ada, makanya getTextureFolder param 2 nya false                
        Iterator<Color> colorIt = cs.iterator();
        Iterator<String> textIt = ts.iterator();
        File f;
        while (colorIt.hasNext() && textIt.hasNext()) {
            //create the (may be non existent) file
            f = new File(FileUtil.toFile(textureFolder), textIt.next());
            //add to tablemodel
            colorTableModel.addRow(new Object[]{null, colorIt.next(), f, null});
        }
        //pasang lagi tablemodellistenernya
        if (currentTableListener != null) {
            colorTableModel.addTableModelListener(currentTableListener);
        }
        //dan undomanager nya
        colorTableModel.setUndoEnabled(true);
    }

    public void setTableSource(CorakDataObject obj) {
        this.sourceDO = obj;
        this.sourceMode = DATA_OBJECT_MODE;
        //sebelum kita acak2 si table nya dengan data dari source yang baru,
        //kita cabut dulu tablemodellistenernya
        if (currentTableListener != null) {
            colorTableModel.removeTableModelListener(currentTableListener);
        }
        //dan undoredo nya
        colorTableModel.setUndoEnabled(false);

        //do acak2
        CorakLSystem cor = CorakSerializer.deserialize(obj);
        if (cor != null) {
            CorakUtil.syncColorsAndTextures(cor);
            //flush table model
            colorTableModel.setRowCount(0);
            //ambil list warnanya & texture nya
            List<Color> cs = cor.getColors();
            List<String> ts = cor.getTextures();

            //ambil folder texture nya, harusnya sih sudah ada, makanya getTextureFolder param 2 nya false                
            Iterator<Color> colorIt = cs.iterator();
            Iterator<String> textIt = ts.iterator();
            File f;
            while (colorIt.hasNext() && textIt.hasNext()) {
                //create the (may be non existent) file
                f = new File(FileUtil.toFile(textureFolder), textIt.next());
                //add to tablemodel
                colorTableModel.addRow(new Object[]{null, colorIt.next(), f, null});
            }
        } else {
            System.err.println("kok corak nya null? gagal parsing donk ini");
        }
        //pasang lagi tablemodellistenernya
        if (currentTableListener != null) {
            colorTableModel.addTableModelListener(currentTableListener);
        }
        //dan undomanager nya
        colorTableModel.setUndoEnabled(true);
    }

    private TableModelListener currentTableListener;

    public final void setTableListener(TableModelListener m) {
        if (currentTableListener != null) {
            colorTableModel.removeTableModelListener(currentTableListener);
        }
        currentTableListener = m;
        colorTableModel.addTableModelListener(currentTableListener);
    }

    public void setUndoManager(UndoManager activeUndoRedo) {
        colorTableModel.setUndoManager(activeUndoRedo);
    }

    /**
     * TODO : FileChangeListener on Texture Folder
     */
    private class TextureFolderChangeListener implements FileChangeListener {

        @Override
        public void fileFolderCreated(FileEvent fe) {
        }

        /**
         * Bisa jadi tadinya missing texture trus ditambahin ke folder.
         *
         * @param fe
         */
        @Override
        public void fileDataCreated(FileEvent fe) {
            FileObject created = fe.getFile();

        }

        @Override
        public void fileChanged(FileEvent fe) {
            //do something
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            //do something
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }
    }

    /**
     * TableModelListener untuk melakukan hal berikut setiap ada perubahan di
     * table:
     *
     * - update value di data source
     *
     * - put savable ke dataobject
     */
    public class ColorTableModelListener implements TableModelListener {

        @Override
        public void tableChanged(TableModelEvent tme) {
            if (sourceMode == DATA_OBJECT_MODE) {
                //write to file directly (without savable)
                CorakLSystem cor = CorakSerializer.deserialize(sourceDO);

                cor.setColor(colorTableModel.extractColors());
                cor.setTextures(colorTableModel.extractTextures());
                CorakSerializer.serialize(cor, FileUtil.toFile(sourceDO.getPrimaryFile()));
            } else {
                //update value on corresponding CorakLSystem
                sourceCor.setColor(colorTableModel.extractColors());
                sourceCor.setTextures(colorTableModel.extractTextures());
                //put savable to data object, if not yet marked as modified
                if (!sourceDO.isModified()) {
                    sourceDO.getLookup().lookup(InstanceContent.class).add(new CorakSavable(sourceCor, sourceDO));
                    sourceDO.setModified(true);
                }
            }

        }

    }
}
