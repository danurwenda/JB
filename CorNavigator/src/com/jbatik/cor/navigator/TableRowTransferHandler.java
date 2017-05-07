/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.cor.navigator;

import java.awt.Cursor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragSource;
import java.io.IOException;
import javax.activation.ActivationDataFlavor;
import javax.activation.DataHandler;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import javax.swing.table.DefaultTableModel;

/**
 * @author Nanta Es <kedipkedip@ymail.com>
 * Handles drag & drop row reordering
 * http://stackoverflow.com/questions/638807/how-do-i-drag-and-drop-a-row-in-a-jtable
 */
public class TableRowTransferHandler extends TransferHandler {

    private final DataFlavor localObjectFlavor = new ActivationDataFlavor(Integer.class, DataFlavor.javaJVMLocalObjectMimeType, "Integer Row Index");
    private JTable table = null;

    public TableRowTransferHandler(JTable table) {
        this.table = table;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        assert (c == table);
        return new DataHandler(table.getSelectedRow(), localObjectFlavor.getMimeType());
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport info) {
        boolean b = info.getComponent() == table && info.isDrop() && info.isDataFlavorSupported(localObjectFlavor);
        table.setCursor(b ? DragSource.DefaultMoveDrop : DragSource.DefaultMoveNoDrop);
        return b;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return TransferHandler.MOVE;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport info) {
        JTable target = (JTable) info.getComponent();
        JTable.DropLocation drop = (JTable.DropLocation) info.getDropLocation();
        int to = drop.getRow();
        int max = table.getModel().getRowCount();
        if (to < 0) {
            to = 0;
        } else if (to >= max) {
            to = max - 1;
        }
        target.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        try {
            Integer from = (Integer) info.getTransferable().getTransferData(localObjectFlavor);
            DefaultTableModel mod = (DefaultTableModel) table.getModel();
            mod.moveRow(from, from, to);
            return true;
        } catch (UnsupportedFlavorException | IOException e) {
        }
        return false;
    }

    /*
     If you drag the selected value outside the component and release 
     the curser will keep the "copy icon". I corrected this by modifying the if statement in exportDone to 
     "act == TransferHandler.MOVE || act == TransferHandler.NONE"
     http://stackoverflow.com/questions/638807/how-do-i-drag-and-drop-a-row-in-a-jtable
     */
    @Override
    protected void exportDone(JComponent c, Transferable t, int act) {
        if (act == TransferHandler.MOVE || act == TransferHandler.NONE) {
            table.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }
}
