/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.satellite;

import com.jbatik.modules.layout.visual.LayoutScene;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.SwingUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;

/**
 * A class that listens to changes to the set of opened TopComponents to
 * show/hide the satellite view window when a TopComponent that supports the
 * layout is activated/deactivated.
 *
 * @author RAPID02
 */
public final class SatelliteViewSwitch implements Runnable, LookupListener {

    static final String PROP_LAYERS_CONTENTS = "component_layers_contents"; //NOI18N
    private static SatelliteViewSwitch theInstance;
    private PropertyChangeListener registryListener;
    private PropertyChangeSupport propertySupport;
    private Lookup.Result lookupRes;
    private LayoutScene currentLayers;
    private Object currentToken;
    private static final RequestProcessor RP = new RequestProcessor(SatelliteViewSwitch.class); //NOI18N

    public LayoutScene getCurrentLayers() {
        return currentLayers;
    }

    private SatelliteViewSwitch() {

        propertySupport = new PropertyChangeSupport(this);
    }

    public synchronized static SatelliteViewSwitch getDefault() {
        if (null == theInstance) {
            theInstance = new SatelliteViewSwitch();
        }
        return theInstance;
    }

    public void startListening() {
        if (!isLayersWindowEnabled()) {
            return;
        }
        synchronized (theInstance) {
            if (null == registryListener) {
                registryListener = createRegistryListener();
                TopComponent.getRegistry().addPropertyChangeListener(registryListener);
                switchLookupListener();
                run();
            }
        }
    }

    public void stopListening() {
        synchronized (theInstance) {
            if (null != registryListener) {
                TopComponent.getRegistry().removePropertyChangeListener(registryListener);
                registryListener = null;
                currentLayers = null;
            }
        }
    }

    /**
     * multiview components do not fire events when switching their inner tabs
     * so we have to listen to changes in lookup contents
     */
    private void switchLookupListener() {
        TopComponent active = TopComponent.getRegistry().getActivated();
        if (null != lookupRes) {
            lookupRes.removeLookupListener(SatelliteViewSwitch.this);
            lookupRes = null;
        }
        if (null != active) {
            lookupRes = active.getLookup().lookupResult(LayoutScene.class);
            lookupRes.addLookupListener(SatelliteViewSwitch.this);
            lookupRes.allItems();
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertySupport.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertySupport.removePropertyChangeListener(l);
    }

    /**
     * Create listener for property change on TopComponent.Registry
     *
     * @return
     */
    private PropertyChangeListener createRegistryListener() {
        return (PropertyChangeEvent evt) -> {
            if (TopComponent.Registry.PROP_CURRENT_NODES.equals(evt.getPropertyName())
                    || TopComponent.Registry.PROP_OPENED.equals(evt.getPropertyName())
                    || TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName())) {

                if (TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName())
                        || TopComponent.Registry.PROP_OPENED.equals(evt.getPropertyName())) {
                    //listen to Lookup changes of showing editor windows
                    watchOpenedTCs();
                }

                if (TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName())) {
                    //switch lookup listener for the activated TC
                    switchLookupListener();
                }
                run();
            }
        };
    }

    private Map<TopComponent, Lookup.Result> watchedLkpResults = new WeakHashMap<>(3);

    private void watchOpenedTCs() {
        ArrayList<TopComponent> windowsToWatch = findShowingTCs();
        ArrayList<TopComponent> toAddListeners = new ArrayList<>(windowsToWatch);
        toAddListeners.removeAll(watchedLkpResults.keySet());

        ArrayList<TopComponent> toRemoveListeners = new ArrayList<>(watchedLkpResults.keySet());
        toRemoveListeners.removeAll(windowsToWatch);

        toRemoveListeners.stream().forEach((tc) -> {
            Lookup.Result res = watchedLkpResults.get(tc);
            if (null != res) {
                res.removeLookupListener(this);
                watchedLkpResults.remove(tc);
            }
        });

        toAddListeners.stream().forEach((tc) -> {
            Lookup.Result res = tc.getLookup().lookupResult(LayoutScene.class);
            res.addLookupListener(this);
            res.allItems();
            watchedLkpResults.put(tc, res);
        });
    }

    private ArrayList<TopComponent> findShowingTCs() {
        ArrayList<TopComponent> res = new ArrayList<>(3);
        TopComponent.getRegistry().getOpened().stream()
                .filter((tc) -> (tc.isShowing()))
                .forEach((tc) -> {
                    res.add(tc);
                });
        return res;
    }

    @Override
    public void run() {
        if (!SwingUtilities.isEventDispatchThread()) {
            EventQueue.invokeLater(this);
            return;
        }
        currentToken = new Object();
        TopComponent.Registry registry = TopComponent.getRegistry();
        final TopComponent activeTc = registry.getActivated();
        final Set<TopComponent> opened = new HashSet<>(registry.getOpened());
        final LayoutScene existingLayers = currentLayers;
        final boolean isMaximized = isLayersMaximized();
        final Object token = currentToken;
        RP.post(() -> {
            findNewLayers(existingLayers, activeTc, opened, isMaximized, token);
        });
    }

    private boolean isCurrentLayersAvailable(LayoutScene existingLayers, Set<TopComponent> openedTcs) {
        for (TopComponent tc : openedTcs) {
            //check whether the window with the current layer controller wasn't closed
            LayoutScene layers = getLayersFromTopComponent(tc, false, true);
            if (null != layers && layers == existingLayers) {
                return true;
            }
        }
        return false;
    }

    private void findNewLayers(final LayoutScene existingLayers, TopComponent activeTc,
            Set<TopComponent> openedTcs, boolean layersIsMaximized, final Object token) {
        //first we try to get controller from currently active tc
        LayoutScene layers = getLayersFromTopComponent(activeTc, true, true);

        //but it might be null, so we check another possibilities
        //1. currently the explorer editor is already opened and maximized. That makes
        //it's impossible to listen to another tc
        //
        //2. there are two tc, opened side by side. on of them (TC1) contains a controller
        //but TC2 does not. And we just changed the selection from TC1 to TC2;
        //==>The explorer should stay open
        layersIsMaximized &= isCurrentLayersAvailable(existingLayers, openedTcs);

        ArrayList<LayoutScene> availableLayerss = new ArrayList<>(3);
        //if currently active tc gives us no Layer, we must iterate over all openedTCs
        if (null == layers) {
            for (Iterator i = openedTcs.iterator(); i.hasNext();) {
                TopComponent tc = (TopComponent) i.next();

                layers = getLayersFromTopComponent(tc, true, true);
                if (null != layers) {
                    availableLayerss.add(layers);
                }
            }

            /**
             * at this point we have Layers from all opened TC in
             * availableLayerss, if any. Meanwhile, variable layers will be
             * assigned with the last Layer found
             */
            if (null != existingLayers //last known layer was not null
                    &&//AND
                    (availableLayerss.contains(existingLayers)//case (2) above
                    || //OR
                    layersIsMaximized)) {
                //actually we do nothing here, see remarks X below
                layers = existingLayers;
            } else if (availableLayerss.size() > 0) {
                //something changed
                layers = availableLayerss.get(0);
            }
        }
        //ready for assigning new layer (if any)
        final LayoutScene newLayer = layers;
        //if newly found layer is the old layer and not null
        if (existingLayers == newLayer && null != newLayer) {
            //do nothing; X remarks here.
            return;
        }
        //at this point, we have new Layer (different from existing)
        //OR
        //new Layer is null
        final boolean shouldBeVisible = SatelliteViewVisibility.getVisibility(newLayer);

        EventQueue.invokeLater(() -> {
            if (currentToken == token) {
                showHideLayersTopComponent(newLayer, shouldBeVisible);
            }
        });
    }

    private void showHideLayersTopComponent(LayoutScene newLayers, boolean isNewVisible) {
        LayoutScene oldLayers = currentLayers;
        currentLayers = newLayers;

        if (isNewVisible) {
            SatelliteViewTopComponent.showWindow();
        } else {
            SatelliteViewTopComponent.hideWindow();
        }

        propertySupport.firePropertyChange(PROP_LAYERS_CONTENTS, oldLayers, currentLayers);
    }

    /**
     * Check whether Layer explorer window is the only opened window.
     *
     * @return
     */
    private boolean isLayersMaximized() {
        boolean isMaximized = true;
        TopComponent.Registry registry = TopComponent.getRegistry();
        Set openedTcs = registry.getOpened();
        for (Iterator i = openedTcs.iterator(); i.hasNext();) {
            TopComponent tc = (TopComponent) i.next();

            if (tc.isShowing() && !(tc instanceof SatelliteViewTopComponent)) {
                //other window(s) than the Layers are showing
                isMaximized = false;
                break;
            }
        }
        return isMaximized;
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        run();
    }

    /**
     *
     * @return True to auto-show/hide firstLevelModel window when an editor with
     * firstLevelModel content is activated, false to let the user open
     * firstLevelModel window manually.
     * @since 1.29
     */
    @NbBundle.Messages({"#NOI18N", "Layers.Window.Enabled=true"})
    private static boolean isLayersWindowEnabled() {
        boolean result = true;
        try {
            String resValue = Bundle.Layers_Window_Enabled(); //NOI18N
            result = "true".equals(resValue.toLowerCase()); //NOI18N
        } catch (MissingResourceException mrE) {
            //ignore
        }
        return result;
    }

    LayoutScene getLayersFromTopComponent(TopComponent tc, boolean mustBeShowing, boolean isOpened) {
        if (null == tc || (!tc.isShowing() && mustBeShowing)) {
            return null;
        }
        return tc.getLookup().lookup(LayoutScene.class);
    }

}
