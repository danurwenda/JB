/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.explorer.view;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.datatransfer.MultiTransferObject;

/**
 *
 * @author RAPID02
 */
/**
 * Utilities to handle drag and drop events to/from other applications
 *
 * @author S. Aubrecht
 */
public class ExternalDragAndDrop {

    private ExternalDragAndDrop() {
    }

    /**
     * The default Transferable implementation for multi-object drag and drop
     * operations is ExTransferable.Multi. However it uses a custom DataFlavor
     * which prevents drag and drop of multiple files from the IDE to other
     * applications. This method checks whether the given Multi instance
     * contains objects that support DataFlavor.javaFileListFlavor and adds a
     * separate Transferable instance for them.
     *
     * @param multi Multi transferable
     *
     * @return The original Multi transferable if none of the inner
     * transferables supports javaFileListFlavor. Otherwise it returns a new
     * ExTransferable with the original Multi transferable and an additional
     * Transferable with javaFileListFlavor that aggregates all file objects
     * from the Multi instance.
     *
     */
    public static Transferable maybeAddExternalFileDnd(ExTransferable.Multi multi) {
        Transferable res = multi;
        try {
            MultiTransferObject mto = (MultiTransferObject) multi.getTransferData(ExTransferable.multiFlavor);
            final ArrayList fileList = new ArrayList(mto.getCount());
            for (int i = 0; i < mto.getCount(); i++) {
                if (mto.isDataFlavorSupported(i, DataFlavor.javaFileListFlavor)) {
                    List list = (List) mto.getTransferData(i, DataFlavor.javaFileListFlavor);
                    fileList.addAll(list);
                }
            }
            if (!fileList.isEmpty()) {
                ExTransferable fixed = ExTransferable.create(multi);
                fixed.put(new ExTransferable.Single(DataFlavor.javaFileListFlavor) {
                    protected Object getData() throws IOException, UnsupportedFlavorException {
                        return fileList;
                    }
                });
                res = fixed;
            }
        } catch (UnsupportedFlavorException ex) {
            Logger.getLogger(ExternalDragAndDrop.class.getName()).log(Level.INFO, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ExternalDragAndDrop.class.getName()).log(Level.INFO, null, ex);
        }
        return res;
    }
}
