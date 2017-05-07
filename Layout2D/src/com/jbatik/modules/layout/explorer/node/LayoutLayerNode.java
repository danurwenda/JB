/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.explorer.node;

import com.jbatik.core.api.GlobalUndoManager;
import com.jbatik.modules.layout.layering.LayoutLayer;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Action;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import org.openide.actions.CopyAction;
import org.openide.actions.MoveDownAction;
import org.openide.actions.MoveUpAction;
import org.openide.actions.RenameAction;
import org.openide.awt.Actions;
import org.openide.explorer.view.CheckableNode;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * Detachable-droppable node, representing a LayoutLayer.
 *
 * @author Dimas Y. Danurwenda
 */
public abstract class LayoutLayerNode extends AbstractNode
        implements CheckableNode {

    protected final LayerList model;

    public LayerList getModel() {
        return model;
    }

    public final LayoutLayer getLayer() {
        return getLookup().lookup(LayoutLayer.class);
    }
//    private LayoutLayer layer;
    protected InstanceContent ic;

    protected LayoutLayerNode(Children children, InstanceContent content, LayerList model) {
        super(children, new AbstractLookup(content));
        this.model = model;
        content.add(this);
        this.ic = content;
        getLayer().addPropertyChangeListener((PropertyChangeEvent evt) -> {
            //refresh sheet
            setSheet(createSheet());
        });
    }

    @Override
    public boolean canRename() {
        return true;
    }
    //mark the flag as triggerred from undo/redo. 
    //TODO : any better solution?
    private boolean undoing = false;

    @Override
    public void setName(String s) {
        if (!undoing) {
            ChangeLayerNameUndoableEdit edit = new ChangeLayerNameUndoableEdit(this.getName(), s);
            GlobalUndoManager.getManager().undoableEditHappened(new UndoableEditEvent(this, edit));
        }
        getLayer().setName(s);
        super.setName(s); //To change body of generated methods, choose Tools | Templates.
    }

    class ChangeLayerNameUndoableEdit extends AbstractUndoableEdit {

        String o;
        String n;

        public ChangeLayerNameUndoableEdit(String name, String s) {
            this.o = name;
            this.n = s;
        }

        @Override
        public String getPresentationName() {
            return "Rename Layer";
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            undoing = true;
            setName(n);
            undoing = false;
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            undoing = true;
            setName(o);
            undoing = false;
        }

    }

    @Override
    public String getName() {
        return getLayer().getName();
    }

    /**
     * Adding Layer's Properties into node.
     * http://www.antonioshome.net/kitchen/swingnbrcp/swingnbrcp-ui2.php
     *
     * @return a Sheet containing Layer's properties
     */
    @Override
    protected Sheet createSheet() {
        // Create a set of properties
        Sheet.Set set = Sheet.createPropertiesSet();

        //adding properties
        //locked : if true, then it's just like a fully locked layer in pshop; can do nothing
        //mappable : if false, then the color index <-> image mapping is locked. locked = true implies mappable = false
        LayoutLayer layer = getLayer();
        try {

            Property nameProp = new PropertySupport.Name(this);
            Property mapProp = new PropertySupport.Reflection(layer, boolean.class, LayoutLayer.LAYER_MAPPABLE_PROP);
            Property modProp = new PropertySupport.Reflection(layer, boolean.class, LayoutLayer.LAYER_LOCKED_PROP);

            set.put(nameProp);
            set.put(mapProp);
            set.put(modProp);

        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        }
        // Create an empty sheet
        Sheet sheet = Sheet.createDefault();
        // Add the set of properties to the sheet
        sheet.put(set);
        return sheet;
    }

    @Override
    public boolean canCut() {
        return true;
    }

    @Override
    public boolean canDestroy() {
        return !getLayer().isLocked();
    }

    /**
     * Does NOT remove the node from parent, refresh children etc. Only remove
     * its underlying model on its list model. This change will then be listened
     * by those listener, one of them is LayerNodeFactory, which will refresh
     * the node.
     *
     * @throws IOException
     */
    @Override
    public void destroy() throws IOException {
        model.remove(getLayer());
    }

    @Override
    public Transferable clipboardCut() throws IOException {
        Transferable deflt = super.clipboardCut();
        ExTransferable added = ExTransferable.create(deflt);
        added.put(new ExTransferable.Single(LayoutLayerFlavor.LAYOUT_LAYER_FLAVOR) {
            @Override
            protected LayoutLayer getData() {
                return getLayer();
            }
        });
        return added;
    }

    @Override
    public Transferable clipboardCopy() throws IOException {
        Transferable deflt = super.clipboardCopy();
        ExTransferable added = ExTransferable.create(deflt);
        added.put(new ExTransferable.Single(LayoutLayerFlavor.LAYOUT_LAYER_FLAVOR) {
            @Override
            protected LayoutLayer getData() {
                return getLayer();
            }
        });
        return added;
    }

    protected Action[] actions;

    @Override
    public Action[] getActions(boolean context) {
        if (actions == null) {
            //action terkait layer
            actions = new Action[]{
                Actions.forID("Edit", "com.jbatik.modules.layout.layering.actions.ResetAction"),
                RenameAction.get(RenameAction.class),
                Actions.forID("Edit", "com.jbatik.modules.layout.layering.actions.CloneAction"),
                CopyAction.get(CopyAction.class),
                //invoking delete from context menu may trigger repainting race
                //                DeleteAction.get(DeleteAction.class),
                Actions.forID("Edit", "com.jbatik.modules.layout.layering.actions.ToFrontAction"),
                MoveUpAction.get(MoveUpAction.class),
                MoveDownAction.get(MoveDownAction.class),
                Actions.forID("Edit", "com.jbatik.modules.layout.layering.actions.ToBackAction")
            };
        }
        return actions; //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Checking whether this node is equal to or descendant of given node
     *
     * @param n
     * @return
     */
    public boolean isDescendantOf(final Node n) {
        if (this == n) {
            return true;
        } else {
            Node p = getParentNode();
            if (p instanceof LayoutLayerNode) {
                LayoutLayerNode parent = (LayoutLayerNode) p;
                return parent.isDescendantOf(n);
            } else {
                return false;
            }
        }
    }

    /**
     * Sometimes different implementation may have different behavior when being
     * deleted. For example group layer may ask whether to remove the group and
     * its contents or delete the group only.
     *
     * @param withWarning true if a popup/confirmation is shown to user before
     * deletion
     */
    public abstract void doDelete(boolean withWarning) throws IOException;

    public void delete() throws IOException {
        doDelete(true);
    }

    //CHECKABLE NODE STATES
    protected boolean checkEnabled = true;//by default, it's enabled

    @Override
    public boolean isCheckable() {
        return true;//always show the checkbox
    }

    @Override
    public boolean isCheckEnabled() {
        return checkEnabled;
    }

    /**
     * Using tri-state, a layer node is visible if it is visible and all its
     * ancestor is visible. Half-visible is it is visible but any of its
     * ancestor is not visible. Not visible if it is not visible.
     *
     * @return
     */
    @Override
    public Boolean isSelected() {
        if (!getLayer().isVisible()) {
            return false;
        }
        Node p = getParentNode();
        if (p instanceof LayoutLayerNode) {
            LayoutLayerNode parent = (LayoutLayerNode) p;
            Boolean parentSelected = parent.isSelected();
            if (Boolean.TRUE.equals(parentSelected)) {
                return true;
            }
        } else {
            //top-level layer
            return true;
        }
        return null;
    }

    /**
     * Di photosop, handle setSelected dibagi jadi 3 kasus.
     *
     * Jika isSelected == null, maka setSelected(any) mengakibatkan state
     * berubah jadi true, dan semua ancestor layer dijadikan true.
     *
     * Jika isSelected == false, maka setSelected(true) mengakibatkan state
     * berubah jadi true, dan semua ancestor layer dijadikan true.
     *
     * Kasus lainnya tidak perlu handling khusus.
     *
     * @param selected
     */
    @Override
    public void setSelected(Boolean selected) {
        if ( //special handling case 1
                (isSelected() == null)
                || //special handling case 2
                (Boolean.FALSE.equals(isSelected()) && Boolean.TRUE.equals(selected))) {
            CompoundEdit ce = new CompoundEdit();
            ce.addEdit(new LayerVisibilityUndoableEdit(this, true));
            getLayer().setVisible(true);
            Node parent = getParentNode();
            while (parent != null && parent instanceof LayoutLayerNode) {
                LayoutLayerNode layoutLayerNode = (LayoutLayerNode) parent;
                ce.addEdit(new LayerVisibilityUndoableEdit(layoutLayerNode, true));
                layoutLayerNode.getLayer().setVisible(true);
                layoutLayerNode.propagatedFireIconChange(false);
                parent = parent.getParentNode();
            }
            //done adding small edits
            ce.end();
            GlobalUndoManager.getManager().undoableEditHappened(new UndoableEditEvent(parent, ce));
            propagatedFireIconChange(true);
        } else {
            //change the 'visible' property, general case
            GlobalUndoManager.getManager().undoableEditHappened(new UndoableEditEvent(getLayer(),
                    new LayerVisibilityUndoableEdit(this, selected)
            ));
            getLayer().setVisible(selected);
        }
        //change the visibility of its descendant
        propagatedFireIconChange(false);
    }

    protected final void propagatedFireIconChange(boolean up) {
        fireIconChange();
        if (up) {
            Node p = getParentNode();
            if (p != null & p instanceof LayoutLayerNode) {
                ((LayoutLayerNode) p).propagatedFireIconChange(up);
            }
        } else {
            Node[] nodes = getChildren().getNodes();
            for (Node node : nodes) {
                LayoutLayerNode cNode = (LayoutLayerNode) node;
                cNode.propagatedFireIconChange(up);
            }
        }
    }

    /**
     * Filtering given nodes into a set of SubLayoutLayer.
     *
     * @param nodes
     * @return a set containing all SubLayoutLayers that is represented by nodes
     * in the specified nodes and their descendant.
     */
    public static Set<LayoutLayer> nodesToSet(Node[] nodes) {
        Set s = new HashSet();
        for (Node n : nodes) {
            if (n instanceof SubLayoutLayerNode) {
                SubLayoutLayerNode subLayoutLayerNode = (SubLayoutLayerNode) n;
                s.add(subLayoutLayerNode.getLayer());
            } else if (n instanceof GroupLayerNode) {
                GroupLayerNode groupLayerNode = (GroupLayerNode) n;
                s.add(groupLayerNode.getLayer());
                Node[] children = groupLayerNode.getChildren().getNodes();
                s.addAll(nodesToSet(children));//recursive
            }
        }
        return s;
    }
}
