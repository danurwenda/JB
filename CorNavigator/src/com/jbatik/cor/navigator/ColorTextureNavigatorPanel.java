/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.cor.navigator;

import com.jbatik.cor.navigator.panel.ColorTextureTablePanel;
import com.jbatik.filetype.cor.CorakDataObject;
import com.jbatik.modules.corak.CorakLSystem;
import com.jbatik.modules.corak.node.CorakFileUtil;
import java.awt.EventQueue;
import java.util.Collection;
import java.util.WeakHashMap;
import javax.swing.JComponent;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.awt.UndoRedo;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author RAPID02
 */
@NavigatorPanel.Registration(mimeType = "text/cor+xml", displayName = "Colors/Textures")
public class ColorTextureNavigatorPanel extends CorakNavigatorPanel {

    private ColorTextureTablePanel panelUI;
    private Lookup.Result<CorakLSystem> corakResult;
    private LookupListener corakOnLookupListener;
    private UndoRedo.Manager activeUndoRedo;
    private CorakLSystem currentCorak;
    private final WeakHashMap<CorakDataObject, UndoRedo.Manager> undoredomap = new WeakHashMap<>();

    private void setCorakMode(boolean usecorak) {
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (getComponent() != null) {
                    if (currentDataObject == null) {
                        return;
                    }
                    
                    Project p = FileOwnerQuery.getOwner(currentDataObject.getPrimaryFile());
                    panelUI.setTextureFolder(CorakFileUtil.getTexturesFolder(p, false));

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

    //parse ini dilakukan saat file .cor nya berubah (dataobject tetap)
    //, atau selected dataobject berubah
    @Override
    protected void parseInformation(CorakDataObject dobj) {
        if (corakResult != null) {
            corakResult.removeLookupListener(getCorakListener());
        }
        //update corakResult
        corakResult = currentDataObject.getLookup().lookupResult(CorakLSystem.class);
        //check current corak for the first time
        Collection<? extends CorakLSystem> coraks = corakResult.allInstances();
        //harusnya ga ada, atau satu
        if (!coraks.isEmpty()) {
            currentCorak = coraks.iterator().next();
        } else {
            currentCorak = null;
        }
        corakResult.addLookupListener(getCorakListener());

        //jika ya, berarti perubahan yang terjadi di sini (add, edit, remove color)
        //tidak langsung ditulis ke file, tapi diterapkan ke cor yang ada di lookup tsb
        if (currentCorak == null) {
            //tidak ada active editor, sumber diambil dari + perubahan langsung ditulis ke
            //currentDataObject.getPrimaryFile
            setCorakMode(false);
        } else {
            setCorakMode(true);
        }
    }

    private LookupListener getCorakListener() {
        if (corakOnLookupListener == null) {
            corakOnLookupListener = (LookupEvent ev) -> {
                /**
                 * Listens to changes of availability of corak on DataObject's
                 * lookup
                 */
                Collection<? extends CorakLSystem> coraks = ((Lookup.Result) ev.getSource()).allInstances();
                //harusnya ga ada, atau satu
                //ini akan triggered saat buka/tutup editor
                //artinya di sini sumbernya harus diupdate
                //kalo ada corak, sumber tabel dari corak
                //kalo ga ada, sumber tabel dari dataobject
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
        return "Color/Texture";
    }

    @Override
    public String getDisplayHint() {
        return "Panel untuk edit Color dan Texture";
    }

    @Override
    public JComponent getComponent() {
        if (lastSaveTime == -1) {
            lastSaveTime = System.currentTimeMillis();
        }
        if (panelUI == null) {
            panelUI = new ColorTextureTablePanel();
        }
        return panelUI;
    }

    @Override
    public Lookup getLookup() {
        return null;
    }

}
