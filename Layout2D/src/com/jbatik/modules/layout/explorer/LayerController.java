/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.explorer;

import com.jbatik.core.api.GlobalUndoManager;
import com.jbatik.modules.layout.explorer.node.AddLayerUndoableEdit;
import com.jbatik.modules.layout.explorer.node.LayerList;
import com.jbatik.modules.layout.explorer.node.LayoutLayerNode;
import com.jbatik.modules.layout.explorer.node.RootNode;
import com.jbatik.modules.layout.layering.GroupLayer;
import com.jbatik.modules.layout.layering.LayoutLayer;
import com.jbatik.modules.layout.layering.SubLayoutLayer;
import com.jbatik.modules.layout.visual.widgets.RootLayerWidget;
import java.util.List;
import javax.swing.event.UndoableEditEvent;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.LayerWidget;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;

/**
 * If an instance of this class is in the <code>Lookup</code> of any
 * <code>TopComponent</code> or <code>MultiViewElement</code> then the layer
 * explorer window will open and display the firstLevelModel structure.
 *
 * It also contains an ExplorerManager to save the state of the nodes
 * representing the firstLevelModel. Those states include opened/closed,
 * selected/not etc.
 *
 * @author RAPID02
 */
public class LayerController {

    final LayerList firstLevelModel;
    ObjectScene scene;

    public LayerController(ExplorerManager em, List<LayoutLayer> ls, ObjectScene s) {
        this.firstLevelModel = new LayerList(ls, null);
        this.manager = em;
        manager.setRootContext(getRootNode());
        this.scene = s;
    }
    ExplorerManager manager;

    public ExplorerManager getExplorerManager() {
        return manager;
    }

    private Node rootNode;

    final Node getRootNode() {
        if (rootNode == null) {
            rootNode = createRootNode();
        }
        return rootNode;
    }

    /**
     * RootNode that has all level1 layers as children
     *
     * @return
     */
    private Node createRootNode() {
        return new RootNode(firstLevelModel);
    }

    private LayerWidget rootWidget;

    public LayerWidget getRootWidget() {
        if (rootWidget == null) {
            rootWidget = createRootWidget();
        }
        return rootWidget;
    }

    private LayerWidget createRootWidget() {
        return new RootLayerWidget(scene, firstLevelModel);
    }

    /**
     * Add new group layer as a sibling of the currently selected layer. If no
     * layer is selected, new layer will be placed topmost. New layer will be
     * placed right above the topmost layer in selected layers.
     */
    public void addNewGroup() {
        addNewLayer(1);
    }

    /**
     * Add new sublayout layer as a sibling of the currently selected layer. If
     * no layer is selected, new layer will be placed topmost. New layer will be
     * placed right above the topmost layer in selected layers.
     */
    public void addNewLayer() {
        addNewLayer(2);
    }

    private void addNewLayer(int c) {
        Node[] sel = manager.getSelectedNodes();
        //if nothing is selected or root node is selected
        if ((sel.length == 0) || (sel[0] instanceof RootNode)) {
            //create default layer without parent
            LayoutLayer newLayer = (c == 1) ? new GroupLayer() : new SubLayoutLayer();
            //handle undo
            AddLayerUndoableEdit edit = new AddLayerUndoableEdit(newLayer, firstLevelModel);
            GlobalUndoManager.getManager().undoableEditHappened(
                    new UndoableEditEvent(firstLevelModel, edit));
            //add the layer
            firstLevelModel.addFirst(newLayer);
        } else {
            LayoutLayerNode n = (LayoutLayerNode) sel[0];
            LayerList model = n.getModel();
            GroupLayer parent = n.getLayer().getParent();
            LayoutLayer newLayer = (c == 1) ? new GroupLayer(parent) : new SubLayoutLayer(parent);
            //put it right before first selected node
            int i = model.list().indexOf(n.getLayer());
            AddLayerUndoableEdit edit = new AddLayerUndoableEdit(newLayer, model, i);
            GlobalUndoManager.getManager().undoableEditHappened(
                    new UndoableEditEvent(model, edit));
            model.add(i, newLayer);
        }
    }
}
