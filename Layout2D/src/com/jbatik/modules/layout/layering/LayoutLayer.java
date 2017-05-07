/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.layering;

import com.jbatik.modules.layout.explorer.node.GroupLayerNode;
import com.jbatik.modules.layout.explorer.node.RootNode;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.openide.nodes.Node;

/**
 * Intinya ini nyontek layernya Photoshop
 *
 * @author RAPID02
 */
public abstract class LayoutLayer implements Cloneable {

    public LayoutLayer(GroupLayer parent) {
        this.parent = parent;
    }

    /**
     * the node delegate for this layer. This attribute is added to LayoutLayer
     * to enable selecting from Scene. hardly need an effective function from
     * layer->node to get rid of this node here
     */
    private transient Node nodeDelegate;

    public void setNodeDelegate(Node n) {
        nodeDelegate = n;
    }

    public Node getNodeDelegate() {
        return nodeDelegate;
    }

    /**
     * This attribute is added to distinguish if Layer's visibility is
     * structural or inherited from its parent
     */
    private GroupLayer parent;

    public void setParent(GroupLayer parent) {
        this.parent = parent;
    }

    public GroupLayer getParent() {
        return parent;
    }

    @Override
    public LayoutLayer clone() throws CloneNotSupportedException {
        LayoutLayer clone = (LayoutLayer) super.clone();
        //ugly typechecking
        Node parno = getNodeDelegate().getParentNode();
        if (parno instanceof RootNode) {
            RootNode root = (RootNode) parno;
            clone.name = root.findFreeName(name);
        } else {
            GroupLayerNode root = (GroupLayerNode) parno;
            clone.name = root.findFreeName(name);
        }
//        }
        //how about pcs?
        //those who listen to the original layer might
        //not listening to the clone as well e.g. widget
        clone.pcs = new PropertyChangeSupport(clone);
        //flush the node, let the factory regenerate it
        clone.nodeDelegate = null;
        return clone;
    }

    protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(listener);
    }
    protected String name;
    public static final String LAYER_NAME_PROP = "name";

    /**
     * Layer pasti punya nama
     *
     * @return nama layer
     */
    public String getName() {
        return name;
    }

    public void setName(String n) {
        String oldName = name;
        name = n;
        pcs.firePropertyChange(LAYER_NAME_PROP, oldName, n);
    }

    /**
     * Layer ini tipe nya macam2, sementara ini kita implement dua tipe :
     * SubLayout dan Group
     *
     * @return
     */
    public abstract String getType();

    /**
     * Sebuah layer bisa diubah2 show/hide nya. Kalo di photoshop yang simbolnya
     * mata
     */
    protected boolean visible;
    public static final String LAYER_VISIBILITY_PROP = "visible";

    public boolean isVisible() {
        return visible;
    }

    /**
     * Return true if this layer is visible on Scene
     *
     * @return
     */
    public boolean isVisibleOnScene() {
        if (parent == null) {
            return visible;
        } else if (visible) {
            return parent.visible;
        } else {
            return false;
        }
    }

    public void setVisible(boolean b) {
        boolean oldVisible = visible;
        visible = b;
        pcs.firePropertyChange(LAYER_VISIBILITY_PROP, oldVisible, b);
    }

    //LOCKING
    /**
     * Sebuah layer bisa dilock formulanya dan posisinya. Intinya kalau isLocked
     * returns false, kotak2 layoutnya ga bisa diubah. Hanya isi dari kotak yang
     * bisa diubah.
     */
    protected boolean locked;
    public static final String LAYER_LOCKED_PROP = "locked";

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean b) {
        boolean oldMod = locked;
        locked = b;
        pcs.firePropertyChange(LAYER_LOCKED_PROP, oldMod, b);
    }

    /**
     * Sebuah SubLayoutLayer bisa dilock image mapping nya. Jika suatu
     * GroupLayer diset mappable = false, ini berarti semua SubLayoutLayer di
     * bawah GroupLayer tersebut jadi ga bisa dimap.
     */
    protected boolean mappable;
    public static final String LAYER_MAPPABLE_PROP = "mappable";

    public boolean isMappable() {
        return mappable;
    }

    public void setMappable(boolean b) {
        boolean oldMap = mappable;
        mappable = b;
        pcs.firePropertyChange(LAYER_MAPPABLE_PROP, oldMap, b);
    }
}
