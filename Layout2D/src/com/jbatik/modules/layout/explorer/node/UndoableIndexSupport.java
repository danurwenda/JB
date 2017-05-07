/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.explorer.node;

import com.jbatik.core.api.GlobalUndoManager;
import java.util.Arrays;
import javax.swing.event.UndoableEditEvent;
import org.openide.nodes.Index;
import org.openide.nodes.Node;

/**
 *
 * @author Dimas Y. Danurwenda
 */
public class UndoableIndexSupport extends Index.Support {

    Node n;
    LayerList childrenModel;

    public UndoableIndexSupport(Node n, LayerList childrenModel) {
        this.n = n;
        this.childrenModel = childrenModel;
    }

    @Override
    public Node[] getNodes() {
        return n.getChildren().getNodes(true);
    }

    @Override
    public int getNodesCount() {
        return getNodes().length;
    }

    @Override
    public void reorder(int[] perm) {
        System.err.println("reorder");
        ReorderIndexUndoableEdit edit = new ReorderIndexUndoableEdit(perm, childrenModel);
        GlobalUndoManager.getManager().undoableEditHappened(new UndoableEditEvent(n, edit));
        childrenModel.reorder(perm);
    }

}
