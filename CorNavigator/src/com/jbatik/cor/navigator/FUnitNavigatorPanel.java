/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jbatik.cor.navigator;

import com.jbatik.cor.navigator.panel.FUnitTablePanel;
import com.jbatik.filetype.cor.CorakDataObject;
import com.jbatik.modules.corak.CorakLSystem;
import java.awt.EventQueue;
import java.util.Collection;
import java.util.WeakHashMap;
import javax.swing.JComponent;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.awt.UndoRedo;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author risman
 */
@NavigatorPanel.Registration(mimeType = "text/cor+xml", displayName = "FUnit")
public class FUnitNavigatorPanel extends CorakNavigatorPanel {

    private FUnitTablePanel panelUI;
    private Lookup.Result<CorakLSystem> corakResult;
    private LookupListener corakOnLookupListener;
    private UndoRedo.Manager activeUndoRedo;
    private CorakLSystem currentCorak;
    private final WeakHashMap<CorakDataObject, UndoRedo.Manager> undoredomap = new WeakHashMap<>();

    //use corak from main editor
    private void setCorakMode(boolean usecorak) {
        
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (getComponent() != null) {
                    if (currentDataObject == null) {
                        return;
                    }
                    if (usecorak) {
                        panelUI.setTableSource(currentDataObject, currentCorak);
                    } else {
                        panelUI.setTableSource(currentDataObject);
                    }

                    //ambil undoredo yang berkesesuaian buat dataobject tersebut
                    activeUndoRedo = getUndoRedoForDO(currentDataObject);
                    panelUI.setUndoManager(activeUndoRedo);
                }
            }
        });
    }

    private UndoRedo.Manager getUndoRedoForDO(CorakDataObject currentDataObject) {
        UndoRedo.Manager undoredo = undoredomap.get(currentDataObject);
        if (undoredo == null) {
            undoredo = new UndoRedo.Manager();
            undoredomap.put(currentDataObject, undoredo);
        }
        return undoredo;
    }

    @Override
    protected void parseInformation(CorakDataObject dobj) {
        if (corakResult != null) {
            corakResult.removeLookupListener(getCorakListener());
        }
        //update corakResult
        corakResult = currentDataObject.getLookup().lookupResult(CorakLSystem.class);
        //check current corak for the first time
        Collection<? extends CorakLSystem> coraks = corakResult.allInstances();
        if (!coraks.isEmpty()) {
            currentCorak = coraks.iterator().next();
        } else {
            currentCorak = null;
        }
        corakResult.addLookupListener(getCorakListener());

        if (currentCorak == null) {
            setCorakMode(false);
        } else {
            setCorakMode(true);
        }
    }

    private LookupListener getCorakListener() {
        if (corakOnLookupListener == null) {
            corakOnLookupListener = (LookupEvent ev) -> {
 
                Collection<? extends CorakLSystem> coraks = ((Lookup.Result) ev.getSource()).allInstances();
 
                if (!coraks.isEmpty()) {
                    currentCorak = coraks.iterator().next();
                    setCorakMode(true);

                } else {
                    currentCorak = null;
                    setCorakMode(false);
                }
            };
        }
        return corakOnLookupListener;
    }

    @Override
    public UndoRedo getUndoRedo() {
        return activeUndoRedo;
    }

    @Override
    public String getDisplayName() {
        return "FUnit";
    }

    @Override
    public String getDisplayHint() {
        return "FUnit Navigator Panel";
    }

    @Override
    public JComponent getComponent() {
        if (lastSaveTime == -1) {
            lastSaveTime = System.currentTimeMillis();
        }
        if (panelUI == null) {
            panelUI = new FUnitTablePanel();
        }
        return panelUI;
    }

    @Override
    public Lookup getLookup() {
        return null;
    }
    
    
}
