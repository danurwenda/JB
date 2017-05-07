/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.cor.navigator.panel;

import com.jbatik.cor.navigator.PicturePresenter;
import com.jbatik.cor.navigator.table.FUnitTableModel;
import com.jbatik.cor.navigator.table.editor.FUnitNameEditor;
import com.jbatik.cor.navigator.table.editor.FUnitPreviewEditor;
import com.jbatik.cor.navigator.table.editor.FUnitRemoveCellEditor;
import com.jbatik.cor.navigator.table.editor.LabelCellEditor;
import com.jbatik.cor.navigator.table.renderer.FUnitNameRenderer;
import com.jbatik.cor.navigator.table.renderer.FUnitPreviewRenderer;
import com.jbatik.cor.navigator.table.renderer.LabelCellRenderer;
import com.jbatik.cor.navigator.table.renderer.RemoveCellRenderer;
import com.jbatik.filetype.cor.CorakDataObject;
import com.jbatik.lsystem.turtle.Surface;
import com.jbatik.lsystem.turtle.Tube;
import com.jbatik.modules.corak.CorakLSystem;
import com.jbatik.modules.corak.io.CorakSavable;
import com.jbatik.modules.corak.io.CorakSerializer;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;
import javax.swing.DropMode;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.undo.UndoManager;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author risman
 */
public class FUnitTablePanel extends JPanel {
    
    private static final int DATA_OBJECT_MODE = 888;
    private static final int CORAK_MODE = 889;
    private int sourceMode;
    private CorakDataObject sourceDO;
    private CorakLSystem sourceCor;

    private TableModelListener currentTableListener;
    
    private JScrollPane scrollpane;
    private JTable funitTable;
    private JButton addButton;
    private FUnitTableModel funitTableModel = new FUnitTableModel();

    public FUnitTablePanel() {
        initComponents();
        funitTable.getTableHeader().setReorderingAllowed(false);
        
        ListSelectionModel listMod = funitTable.getSelectionModel();
        listMod.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listMod.addListSelectionListener(funitTable);
        
        funitTable.getColumnModel().getColumn(0).setCellEditor(new LabelCellEditor(15));
        funitTable.getColumnModel().getColumn(0).setCellRenderer(new LabelCellRenderer(JLabel.CENTER));
        
        funitTable.getColumnModel().getColumn(1).setCellEditor(new FUnitNameEditor());
        funitTable.getColumnModel().getColumn(1).setCellRenderer(new FUnitNameRenderer());
        
        funitTable.getColumnModel().getColumn(2).setCellEditor(new FUnitPreviewEditor());
        funitTable.getColumnModel().getColumn(2).setCellRenderer(new FUnitPreviewRenderer());
        
        funitTable.getColumnModel().getColumn(3).setCellEditor(new FUnitRemoveCellEditor(funitTable, funitTableModel));
        funitTable.getColumnModel().getColumn(3).setCellRenderer(new RemoveCellRenderer());
        
        
        funitTable.setRowHeight(80);
        funitTable.setCellSelectionEnabled(true);
        funitTable.setSurrendersFocusOnKeystroke(true);
        
        PicturePresenter.setPicture_Add(addButton);
        addButton.addActionListener((ActionEvent e) -> {
            Surface newFUnit = new Tube();
            funitTableModel.addRow(new Object[]{
                null,
                newFUnit,
                newFUnit,
                null},true);//2nd parameter means this action is undoable
        });
        setTableListener(new FUnitTableModelListener());
    }

    private void initComponents() {
        scrollpane = new JScrollPane();
        funitTable = new JTable();
        addButton = new JButton();
        
        funitTable.setFont(new Font("Tahoma", 0, 15)); //NOI18N
        funitTable.setModel(funitTableModel);
        funitTable.setDragEnabled(true);
        funitTable.setDropMode(DropMode.ON_OR_INSERT_ROWS);
        
        scrollpane.setViewportView(funitTable);
        
        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(ColorTextureTablePanel.class, "ColorTextureTablePanel.addButton.text"));
        
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(scrollpane, GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(addButton)
                    .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(scrollpane, GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addButton)
                .addContainerGap())
        );
    }
    
    public void setTableSource(CorakDataObject currentDataObject, CorakLSystem cor) {
        this.sourceDO = currentDataObject;//tetap perlu buat taruh savable
        this.sourceCor = cor;
        this.sourceMode = CORAK_MODE;
        //sebelum kita acak2 si table nya dengan data dari source yang baru,
        //kita cabut dulu tablemodellistenernya
        if (currentTableListener != null) {
            funitTableModel.removeTableModelListener(currentTableListener);
        }
        //dan undoredo nya
        funitTableModel.setUndoEnabled(false);
        //flush table model
        funitTableModel.setRowCount(0);
        //ambil list FUnitnya
        List<Surface> fs = cor.getSurfaces();
              
        Iterator<Surface> surfaceIt = fs.iterator();
        while (surfaceIt.hasNext()) {
            //add to tablemodel
            Surface srf = surfaceIt.next();
            funitTableModel.addRow(new Object[]{null, srf, srf, null});
        }
        //pasang lagi tablemodellistenernya
        if (currentTableListener != null) {
            funitTableModel.addTableModelListener(currentTableListener);
        }
        //dan undomanager nya
        funitTableModel.setUndoEnabled(true);
    }
    
    public void setTableSource(CorakDataObject obj) {
        this.sourceDO = obj;
        this.sourceMode = DATA_OBJECT_MODE;
        //sebelum kita acak2 si table nya dengan data dari source yang baru,
        //kita cabut dulu tablemodellistenernya
        if (currentTableListener != null) {
            funitTableModel.removeTableModelListener(currentTableListener);
        }
        //dan undoredo nya
        funitTableModel.setUndoEnabled(false);

        //do acak2
        CorakLSystem cor = CorakSerializer.deserialize(obj);
        if (cor != null) {
            //flush table model
            funitTableModel.setRowCount(0);
            //ambil list FUnitnya
            List<Surface> fs = cor.getSurfaces();
          
            Iterator<Surface> surfaceIt = fs.iterator();
            while (surfaceIt.hasNext()) {
                Surface srf = surfaceIt.next();
                funitTableModel.addRow(new Object[]{null, srf, srf, null});
            }
        } else {
            //
        }
        //pasang lagi tablemodellistenernya
        if (currentTableListener != null) {
            funitTableModel.addTableModelListener(currentTableListener);
        }
        //dan undomanager nya
        funitTableModel.setUndoEnabled(true);
    }

    public final void setTableListener(TableModelListener m) {
        if (currentTableListener != null) {
            funitTableModel.removeTableModelListener(currentTableListener);
        }
        currentTableListener = m;
        funitTableModel.addTableModelListener(currentTableListener);
    }
    
    public void setUndoManager(UndoManager activeUndoRedo) {
        funitTableModel.setUndoManager(activeUndoRedo);
    }
    
    public class FUnitTableModelListener implements TableModelListener {

        @Override
        public void tableChanged(TableModelEvent tme) {
            if (sourceMode == DATA_OBJECT_MODE) {
                //write to file directly (without savable)
                CorakLSystem cor = CorakSerializer.deserialize(sourceDO);

                cor.setSurfaces(funitTableModel.extractSurfaces());
                CorakSerializer.serialize(cor, FileUtil.toFile(sourceDO.getPrimaryFile()));
            } else {
                //update value on corresponding CorakLSystem
                try {
                    sourceCor.setSurfaces(funitTableModel.extractSurfaces());
                    //put savable to data object, if not yet marked as modified
                    if (!sourceDO.isModified()) {
                        sourceDO.getLookup().lookup(InstanceContent.class).add(new CorakSavable(sourceCor, sourceDO));
                        sourceDO.setModified(true);
                    }  
                } catch (NullPointerException e) { //null if table not ready yet
                    //
                }
                
            }

        }

    }
}
