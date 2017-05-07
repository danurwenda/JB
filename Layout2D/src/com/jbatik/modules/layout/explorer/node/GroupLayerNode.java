/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.explorer.node;

import com.jbatik.core.api.GlobalUndoManager;
import static com.jbatik.modules.layout.explorer.node.Bundle.*;
import com.jbatik.modules.layout.layering.GroupLayer;
import com.jbatik.modules.layout.layering.LayoutLayer;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.Action;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.PasteAction;
import org.openide.actions.ReorderAction;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.InstanceContent;

/**
 * Extensi dari LayoutLayerNode yang bisa menerima drop berupa LayoutLayerNode
 *
 * @author RAPID02
 */
public class GroupLayerNode extends LayoutLayerNode {

    private LayerList childrenModel;

    public GroupLayerNode(LayerList children, InstanceContent content, LayerList model) {
        //Note that we passed false as second parameter of Children.create
        //Since it's possible for user to select the group layer on explorer
        //without first expanding it.
        super(Children.create(new LayerNodeFactory(children), false), content, model);
        this.childrenModel = children;
        ic.add(new UndoableIndexSupport(this, childrenModel));
        //hack, to force group layer to generate its children
        getChildren().getNodesCount();
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
            //cek hirarki
            if (isDescendantOf(leNode)) {
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
                            ChangeParentNodeUndoableEdit edit = new ChangeParentNodeUndoableEdit(ori, layoutLayerNode.model, childrenModel);
                            GlobalUndoManager.getManager().undoableEditHappened(
                                    new UndoableEditEvent(ori, edit));
                            layoutLayerNode.destroy();
                            ori.setParent((GroupLayer) getLayer());
                            childrenModel.add(ori);
                        } else {
                            //its a copy-paste
                            LayoutLayer clone = ori.clone();
                            AddLayerUndoableEdit edit = new AddLayerUndoableEdit(clone, childrenModel);
                            GlobalUndoManager.getManager().undoableEditHappened(
                                    new UndoableEditEvent(ori, edit));
                            clone.setParent((GroupLayer) getLayer());
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

    /**
     * Khusus buat group layer punya action tambahan yaitu paste dan reorder
     * children.
     *
     * @param context
     * @return
     */
    @Override
    public Action[] getActions(boolean context) {
        if (actions == null) {
            List<Action> acts = new ArrayList<>(8);
            acts.add(ReorderAction.get(ReorderAction.class));
            acts.add(PasteAction.get(PasteAction.class));

            //add super action
            acts.addAll(Arrays.asList(super.getActions(context)));

            this.actions = new Action[acts.size()];
            acts.toArray(this.actions);
        }
        return actions;
    }

    @NbBundle.Messages({
        "groupdialog.title=Confirmation",
        "# {0} - group name",
        "groupdialog.message=Delete the group \"{0}\" and its contents or delete only the group?"
    })
    @Override
    public void doDelete(boolean withWarning) throws IOException {
        boolean cancelled = false;
        boolean compound = false;
        UndoableEdit del = new DeleteSimpleLayoutLayerUndoableEdit(getLayer(), model);
        if (withWarning) {
            //show three options : 
            // doDelete this group as well as its children
            // move all its children to its parent, then doDelete this group
            // cancel

            String initialValue = "Group and Contents";
            String groupOnly = "Group Only";
            Object[] options = new Object[]{initialValue, groupOnly, NotifyDescriptor.CANCEL_OPTION};
            //send options to user
            Object result = DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor(
                            groupdialog_message(getDisplayName()),
                            groupdialog_title(),
                            NotifyDescriptor.YES_NO_CANCEL_OPTION,
                            NotifyDescriptor.WARNING_MESSAGE,
                            options,
                            initialValue
                    )
            );

            //check the result
            if (result == groupOnly) {
                compound = true;
                //move all this.children to this.parent, order by current position
                del = new CompoundEdit();
                while (!childrenModel.list().isEmpty()) {
                    LayoutLayer l = childrenModel.list().get(0);
                    childrenModel.remove(l);
                    int i = model.list().indexOf(getLayer());
                    l.setParent((GroupLayer) getLayer());
                    model.add(i, l);
                    del.addEdit(new MoveNodeOutUndoableEdit(l, childrenModel, model));
                }
            } else if (result != initialValue) {
                //cancelled
                cancelled = true;
            }
        }
        if (!cancelled) {
            if (compound) {
                CompoundEdit ce = (CompoundEdit) del;
                ce.addEdit(new DeleteSimpleLayoutLayerUndoableEdit(getLayer(), model));
                ce.end();
            }
            destroy();
            GlobalUndoManager.getManager().undoableEditHappened(new UndoableEditEvent(model, del));
        }
    }

    @Override
    protected Sheet createSheet() {
        Sheet s = super.createSheet();

        s.put(createGroupSet());
        return s;
    }

    /**
     * Adding properties to group
     * @return 
     */
    @NbBundle.Messages({
        "CTL_GroupProperties=Sublayout",
        "HINT_GroupProperties=The properties of selected sublayout(s)",})
    private Sheet.Set createGroupSet() {
        Sheet.Set set = new Sheet.Set();
        set.setName(CTL_GroupProperties());
        set.setDisplayName(CTL_GroupProperties());
        set.setShortDescription(HINT_GroupProperties());
        GroupLayer gl = (GroupLayer) getLayer();
        try {
            //position
            Property centerX = new PropertySupport.Reflection(gl, int.class, GroupLayer.X_CENTER_PROP);
            Property centerY = new PropertySupport.Reflection(gl, int.class, GroupLayer.Y_CENTER_PROP);
            
            set.put(centerX);
            set.put(centerY);
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        }
        return set;
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
