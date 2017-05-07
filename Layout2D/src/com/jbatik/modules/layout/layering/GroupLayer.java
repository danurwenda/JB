/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.layering;

import com.jbatik.modules.layout.explorer.node.LayerList;
import java.util.ArrayList;
import java.util.List;

/**
 * A group Layer has children of layers.
 *
 * @author RAPID02
 */
public class GroupLayer extends LayoutLayer {

    private LayerList children;
    public static final String X_CENTER_PROP = "xcenter";
    private int xcenter;

    public static final String Y_CENTER_PROP = "ycenter";
    private int ycenter;

    public int getXcenter() {
        return xcenter;
    }

    public void setXcenter(int xcenter) {
        this.xcenter = xcenter;
    }

    public int getYcenter() {
        return ycenter;
    }

    public void setYcenter(int ycenter) {
        this.ycenter = ycenter;
    }

    public GroupLayer() {
        this(new ArrayList<>(), null);
    }

    public GroupLayer(GroupLayer parent) {
        this(new ArrayList<>(), parent);
    }

    @Override
    public String getType() {
        return "group";
    }

    public GroupLayer(List<LayoutLayer> c, GroupLayer parent) {
        super(parent);
        this.children = new LayerList(c, this);
        locked = false;
        mappable = true;
        visible = true;
        name = "Group";
    }

    public LayerList getModel() {
        return children;
    }

    @Override
    public GroupLayer clone() throws CloneNotSupportedException {
        List<LayoutLayer> copy = new ArrayList<>(children.list().size());
        for (LayoutLayer x : children.list()) {
            copy.add(x.clone());
        }
        GroupLayer cg = (GroupLayer) super.clone();
        cg.children = new LayerList(copy, cg);
        return cg;
    }

    @Override
    public void setMappable(boolean b) {
        setMappable(b, true); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setLocked(boolean b) {
        setLocked(b, true); //To change body of generated methods, choose Tools | Templates.
    }

    public void setMappable(boolean b, boolean propagate) {
        //propagate bottom-up
        if (propagate) {
            for (LayoutLayer x : children.list()) {
                x.setMappable(b);
            }
        }
        super.setMappable(b); //To change body of generated methods, choose Tools | Templates.
    }

    public void setLocked(boolean b, boolean propagate) {
        //lock bottom-up
        if (propagate) {
            for (LayoutLayer x : children.list()) {
                x.setLocked(b);
            }
        }
        super.setLocked(b); //To change body of generated methods, choose Tools | Templates.
    }

    public void setModel(List<LayoutLayer> c) {
        this.children = new LayerList(c, this);
    }

}
