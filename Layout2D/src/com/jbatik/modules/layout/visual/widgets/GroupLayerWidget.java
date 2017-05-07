/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.visual.widgets;

import com.jbatik.modules.layout.layering.GroupLayer;
import com.jbatik.modules.layout.layering.LayoutLayer;
import java.beans.PropertyChangeListener;
import org.netbeans.api.visual.model.ObjectScene;
import org.openide.util.WeakListeners;

/**
 * A widget that represents a group layer. A GroupLayerWidget may contains zero
 * or more widget as children. Any child must be an instance of GroupLayerWidget
 * or SubLayoutWidget.
 *
 * @author Dimas Y. Danurwenda
 */
public class GroupLayerWidget extends RootLayerWidget implements LayoutLayerPresenter {

    public GroupLayerWidget(ObjectScene scene, GroupLayer groupLayer) {
        super(scene, groupLayer.getModel());
        this.layer = groupLayer;
        setVisible(groupLayer.isVisible());
        setEnabled(!groupLayer.isLocked());        
        this.pcl = new LayoutLayerWidgetPCL(this);
        layer.addPropertyChangeListener(WeakListeners.propertyChange(pcl, layer));
    }
    LayoutLayer layer;
    PropertyChangeListener pcl;

    @Override
    public LayoutLayer getLayer() {
        return layer;
    }
}
