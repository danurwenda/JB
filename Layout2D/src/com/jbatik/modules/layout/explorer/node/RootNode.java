/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.explorer.node;

import com.jbatik.core.api.GlobalUndoManager;
import com.jbatik.modules.layout.layering.LayoutLayer;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;
import javax.swing.Action;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.UndoableEdit;
import org.openide.actions.PasteAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.Exceptions;
import org.openide.util.datatransfer.PasteType;

/**
 *
 * @author RAPID02
 */
public class RootNode extends AbstractNode {

    private final LayerList childrenModel;

    public RootNode(final LayerList model) {
        super(Children.create(new LayerNodeFactory(model), true));
        this.childrenModel = model;
        getCookieSet().add(new UndoableIndexSupport(this, childrenModel));
        setName("All layers");
    }

    /**
     * Can this node be copied?
     *
     * @return <code>true</code>
     */
    @Override
    public boolean canCopy() {
        return false;
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[]{PasteAction.get(PasteAction.class)};
    }

    @Override
    public PasteType getDropType(final Transferable t, int arg1, int arg2) {
        if (t.isDataFlavorSupported(LayoutLayerFlavor.LAYOUT_LAYER_FLAVOR)) {
            //first lets check whether it's a parent node being pasted into one of its descendants.
            //CANNOT PASTE INTO ITSELF OR DESCENDANTS
            Node leNode;
            final Node moveCut = NodeTransfer.node(t, NodeTransfer.DND_MOVE | NodeTransfer.CLIPBOARD_CUT);
            if (moveCut != null) {
                //mau cut
                leNode = moveCut;
            } else {
                //mau copy
                leNode = NodeTransfer.node(t, NodeTransfer.DND_COPY | NodeTransfer.CLIPBOARD_COPY);
            }
            if (leNode == null) {
                //bukan cut, bukan pula copy
                return null;
            }

            //cek tipe
            if (!(leNode instanceof LayoutLayerNode)) {
                return null;
            }
            LayoutLayerNode layoutLayerNode = (LayoutLayerNode) leNode;
            return new PasteType() {
                @Override
                public Transferable paste() throws IOException {
                    try {
                        LayoutLayer ori = (LayoutLayer) t.getTransferData(LayoutLayerFlavor.LAYOUT_LAYER_FLAVOR);
                        if (moveCut != null) {
                            //it's a cut-paste
                            UndoableEdit edit = new ChangeParentNodeUndoableEdit(ori, layoutLayerNode.model, childrenModel);
                            GlobalUndoManager.getManager().undoableEditHappened(
                                    new UndoableEditEvent(ori, edit));
                            layoutLayerNode.destroy();
                            //switch parent to null because it's RootNode
                            ori.setParent(null);
                            childrenModel.add(ori);
                        } else {
                            //its a copy-paste
                            LayoutLayer clone = ori.clone();
                            UndoableEdit edit = new AddLayerUndoableEdit(clone, childrenModel);
                            GlobalUndoManager.getManager().undoableEditHappened(
                                    new UndoableEditEvent(ori, edit));
                            clone.setParent(null);
                            childrenModel.add(clone);
                        }
                    } catch (UnsupportedFlavorException | CloneNotSupportedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    return null;
                }
            };

        }
        return null;

    }

    @Override
    protected void createPasteTypes(Transferable t, List<PasteType> s) {
        super.createPasteTypes(t, s);
        PasteType p = getDropType(t, 0, 0);
        if (p != null) {
            s.add(p);
        }
    }

    public String findFreeName(String name) {
        if (checkFreeName(name)) {
            return name;
        }

        for (int i = 1;; i++) {
            String destName = name + "_" + i; // NOI18N

            if (checkFreeName(destName)) {
                return destName;
            }
        }
    }

    private boolean checkFreeName(String name) {
        //cek apakah ada children dengan nama itu
        return getChildren().findChild(name)==null;
    }
}
