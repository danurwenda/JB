/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.explorer;

import com.jbatik.modules.layout.explorer.node.LayoutLayerNode;
import com.jbatik.modules.layout.layering.LayoutLayer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import org.netbeans.api.visual.model.ObjectScene;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;

/**
 * LayerFactory creates LayerController
 *
 * @author RAPID02
 */
public class LayerFactory {

    /**
     * Change of node selection in ExplorerManager should be reflected to
     * selected widgets on scene.
     *
     * In Photoshop, there's a menu to set it vice versa.
     *
     * @param aThis
     * @param layers
     * @return
     */
    public static LayerController createLayer(ObjectScene aThis, List<LayoutLayer> layers) {
        ExplorerManager em = new ExplorerManager();
        SelectedLayerListener l = new SelectedLayerListener(em, aThis);
        em.addPropertyChangeListener(l);
        return new LayerController(em, layers, aThis);
    }

    /**
     * Selection in explorer -> selection in scene
     */
    private static class SelectedLayerListener implements PropertyChangeListener {

        ExplorerManager explorerManager;
        ObjectScene scene;

        public SelectedLayerListener(ExplorerManager explorerManager, ObjectScene scene) {
            this.explorerManager = explorerManager;
            this.scene = scene;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(ExplorerManager.PROP_SELECTED_NODES)) {
                Node[] selNodes = explorerManager.getSelectedNodes();
                scene.setSelectedObjects(LayoutLayerNode.nodesToSet(selNodes));
            }
        }
    }

    /**
     * Do not allow instances of this class.
     */
    private LayerFactory() {
    }
}
