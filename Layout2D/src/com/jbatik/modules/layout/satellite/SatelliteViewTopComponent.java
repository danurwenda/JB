/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.satellite;

import com.jbatik.modules.layout.visual.LayoutScene;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JScrollPane;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author RAPID02
 */
@TopComponent.Description(
        preferredID = "SatelliteViewTopComponent",
        iconBase = "com/jbatik/modules/layout/explorer/palette.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(position = 12, mode = "properties", openAtStartup = false)
@NbBundle.Messages({
    "CTL_LayersTopComponent=Satellite View",
    "HINT_LayersTopComponent=Satellite View"
})
public class SatelliteViewTopComponent extends TopComponent implements PropertyChangeListener {

    private static SatelliteViewTopComponent instance;

    /**
     * Gets default instance. Don't use directly, it reserved for '.settings'
     * file only, i.e. deserialization routines, otherwise you can get
     * non-deserialized instance.
     */
    public static synchronized SatelliteViewTopComponent getDefault() {
        if (instance == null) {
            instance = new SatelliteViewTopComponent();
        }
        return instance;
    }

    static void showWindow() {
        WindowManager wm = WindowManager.getDefault();
        TopComponent palette = wm.findTopComponent("SatelliteViewTopComponent"); // NOI18N
        if (null == palette) {
            Logger.getLogger(SatelliteViewSwitch.class.getName()).log(Level.INFO, "Cannot find Layers component."); // NOI18N
            //for unit-testing
            palette = getDefault();
        }
        if (!palette.isOpened()) {
            palette.open();
        }
    }

    static void hideWindow() {
        TopComponent palette = WindowManager.getDefault().findTopComponent("SatelliteViewTopComponent"); // NOI18N
        if (palette != null && palette.isOpened()) {
            palette.close();
        }
    }

    public SatelliteViewTopComponent() {
        setName(Bundle.CTL_LayersTopComponent());
        setToolTipText(Bundle.HINT_LayersTopComponent());

        setLayout(new BorderLayout());
        pane = new JScrollPane();
        add(pane, BorderLayout.CENTER);
        setPreferredSize(new Dimension(505, 88));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (SatelliteViewSwitch.PROP_LAYERS_CONTENTS.equals(evt.getPropertyName())) {
            setLayoutScene((LayoutScene) evt.getNewValue());
        }
    }

    @Override
    protected void componentClosed() {
        SatelliteViewSwitch switcher = SatelliteViewSwitch.getDefault();

        switcher.removePropertyChangeListener(this);

        LayoutScene pc = switcher.getCurrentLayers();
        SatelliteViewVisibility.setVisible(pc, false);
    }

    @Override
    protected void componentOpened() {
        SatelliteViewSwitch switcher = SatelliteViewSwitch.getDefault();

        switcher.addPropertyChangeListener(this);

        LayoutScene pc = switcher.getCurrentLayers();
        setLayoutScene(pc);
        if (Utils.isOpenedByUser(this)) {
            //only change the flag when the Palette window was opened from ShowPaletteAction
            //i.e. user clicked the menu item or used keyboard shortcut - ignore window system load & restore
            SatelliteViewVisibility.setVisible(pc, true);
        }
    }

    JScrollPane pane;

    private void setLayoutScene(LayoutScene s) {
        if (s != null) {
            pane.setViewportView(s.createSatelliteView());
        }
    }

}
