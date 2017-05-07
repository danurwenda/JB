/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.explorer.node;

import com.jbatik.modules.layout.layering.GroupLayer;
import com.jbatik.modules.layout.layering.LayoutLayer;
import com.jbatik.modules.layout.layering.SubLayoutLayer;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author RAPID02
 */
class LayerNodeFactory extends ChildFactory.Detachable<LayoutLayer> implements ChangeListener {

    private final LayerList model;

    public LayerNodeFactory(LayerList model) {
        this.model = model;
    }

    @Override
    protected boolean createKeys(List<LayoutLayer> list) {
        list.addAll(model.list());
        return true;
    }

//    protected Node createNodeForKey2(LayoutLayer layer) {
//        //use the existing node, if exist
//        Node n = layer.getNodeDelegate();
//        if (n == null) {
//            InstanceContent content = new InstanceContent();
//            if (layer instanceof GroupLayer) {
//                GroupLayer groupLayer = (GroupLayer) layer;
//                content.add(groupLayer);
//                n = new GroupLayerNode(groupLayer.getModel(), content, model);
//                groupLayer.setNodeDelegate(n);
//            } else if (layer instanceof SubLayoutLayer) {
//                SubLayoutLayer subLayoutLayer = (SubLayoutLayer) layer;
//                content.add(subLayoutLayer);
//                n = new SubLayoutLayerNode(content, model);
//                subLayoutLayer.setNodeDelegate(n);
//            }
//        }
//        return n;
//    }

    @Override
    protected Node createNodeForKey(LayoutLayer layer) {
        Node n = null;
        InstanceContent content = new InstanceContent();
        if (layer instanceof GroupLayer) {
            GroupLayer groupLayer = (GroupLayer) layer;
            content.add(groupLayer);
            n = new GroupLayerNode(groupLayer.getModel(), content, model);
            groupLayer.setNodeDelegate(n);
        } else if (layer instanceof SubLayoutLayer) {
            SubLayoutLayer subLayoutLayer = (SubLayoutLayer) layer;
            content.add(subLayoutLayer);
            n = new SubLayoutLayerNode(content, model);
            subLayoutLayer.setNodeDelegate(n);
        }
        return n;
    }

    @Override
    protected void addNotify() {
        model.addChangeListener(this);
    }

    @Override
    protected void removeNotify() {
        model.removeChangeListener(this);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        refresh(false);
    }

}
