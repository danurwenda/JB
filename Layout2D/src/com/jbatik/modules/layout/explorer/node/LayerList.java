/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.explorer.node;

import com.jbatik.modules.layout.layering.GroupLayer;
import com.jbatik.modules.layout.layering.LayoutLayer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;

/**
 * Handle two list of LayoutLayer, one from LSystem structure, and one that
 * wrapped inside a LinkedList. I wrap the list as a LinkedList simply because
 * I'm lazy enough to implement addFirst. Actually it's a list with a
 * ChangeListener.
 *
 * @author RAPID02
 */
public class LayerList {

    private final GroupLayer owner;

    public GroupLayer getOwner() {
        return owner;
    }
    private List<LayoutLayer> originalList;
    private final LinkedList<LayoutLayer> linkedLayers;
    private final ChangeSupport cs = new ChangeSupport(this);

    public LayerList(List<LayoutLayer> ori, GroupLayer owner) {
        this.owner = owner;
        this.originalList = ori;
        this.linkedLayers = new LinkedList(ori);
        this.addChangeListener((ChangeEvent e) -> {
            //sync between ori and linkedlist
            originalList.clear();
            originalList.addAll(linkedLayers);
        });
    }

    public void reorder(int[] perm) {
        LayoutLayer[] reordered = new LayoutLayer[linkedLayers.size()];
        for (int i = 0; i < perm.length; i++) {
            int j = perm[i];
            LayoutLayer c = linkedLayers.get(i);
            reordered[j] = c;
        }
        linkedLayers.clear();
        linkedLayers.addAll(Arrays.asList(reordered));
        cs.fireChange();
    }

    public List<? extends LayoutLayer> list() {
        return linkedLayers;
    }

    /**
     * Semacam addLast gitu.
     *
     * @param c teradd.
     */
    public void add(LayoutLayer c) {
        linkedLayers.add(c);
        cs.fireChange();
    }

    public void add(int i, LayoutLayer l) {
        linkedLayers.add(i, l);
        cs.fireChange();
    }

    public void addFirst(LayoutLayer groupLayer) {
        linkedLayers.addFirst(groupLayer);
        cs.fireChange();
    }

    public void remove(LayoutLayer c) {
        linkedLayers.remove(c);
        cs.fireChange();
    }

    public final void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }

}
