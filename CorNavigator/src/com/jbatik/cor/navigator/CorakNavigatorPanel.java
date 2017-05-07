/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.cor.navigator;

import com.jbatik.filetype.cor.CorakDataObject;
import java.awt.EventQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;
import org.netbeans.spi.navigator.NavigatorPanelWithUndo;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

@Messages({
    "ERR_DataObject=Files of Cor"
})
/**
 *
 * @author RAPID02
 */
public abstract class CorakNavigatorPanel implements NavigatorPanelWithUndo {

    /**
     * current context to work on
     */
    private Lookup.Result currentContext;
    /**
     * listener to context changes
     */
    private LookupListener contextListener;
    /**
     * Listens for changes on image file.
     */
    private FileChangeListener fileChangeListener;
    protected long lastSaveTime = -1;
    protected CorakDataObject currentDataObject;
    private static final RequestProcessor WORKER = new RequestProcessor(CorakNavigatorPanel.class.getName());

    @Override
    public void panelActivated(Lookup context) {
        currentContext = context.lookupResult(CorakDataObject.class);
        currentContext.addLookupListener(getContextListener());
        // get actual data and recompute content
        Collection data = currentContext.allInstances();
        currentDataObject = getDataObject(data);
        if (currentDataObject == null) {
            return;
        }
        if (fileChangeListener == null) {
            fileChangeListener = new CorFileChangeAdapter();
        }
        currentDataObject.getPrimaryFile().addFileChangeListener(fileChangeListener);
        setNewContent(currentDataObject);
    }

    @Override
    public void panelDeactivated() {
        currentContext.removeLookupListener(getContextListener());
        currentContext = null;
        if (currentDataObject != null) {
            currentDataObject.getPrimaryFile().removeFileChangeListener(fileChangeListener);
        }
        currentDataObject = null;
    }

    /**
     * Accessor for listener to context
     */
    private LookupListener getContextListener() {
        if (contextListener == null) {
            /**
             * Listens to changes of context and triggers proper action
             */
            contextListener = (LookupEvent ev) -> {
                Collection data = currentContext.allInstances();
//                if (data.isEmpty()) {
//                    System.err.println("bahkan datanya null");
//                } else {
//                    System.err.println("datanya ada sih");
//                }
                currentDataObject = getDataObject(data);
                if (currentDataObject == null) {
//                    System.err.println("CDO NYA NULL");
                    return;
                }
                setNewContent(currentDataObject);
            };
        }
        return contextListener;
    }

    private void setNewContent(final CorakDataObject dataObject) {
        if (dataObject == null) {
            return;
        }

        WORKER.post(() -> {
            parseInformation(dataObject);
        });

    }

    protected abstract void parseInformation(final CorakDataObject dobj);

    private CorakDataObject getDataObject(Collection data) {
        CorakDataObject dataObject = null;
        Iterator it = data.iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof CorakDataObject) {
                dataObject = (CorakDataObject) o;
                break;
            }
        }
        return dataObject;
    }

    private class CorFileChangeAdapter extends FileChangeAdapter {

        @Override
        public void fileChanged(final FileEvent fe) {
            if (fe.getTime() > lastSaveTime) {
                lastSaveTime = System.currentTimeMillis();

                // Refresh content
                EventQueue.invokeLater(() -> {
                    try {
                        currentDataObject = (CorakDataObject) DataObject.find(fe.getFile());
                        if (currentDataObject == null) {
                            return;
                        }
                        setNewContent(currentDataObject);
                    } catch (DataObjectNotFoundException ex) {
                        Logger.getLogger(CorakNavigatorPanel.class.getName()).info(Bundle.ERR_DataObject());
                    }
                });
            }
        }
    }

}
