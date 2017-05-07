/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout;

import com.jbatik.modules.layout.layering.LayoutLayer;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.Exceptions;

/**
 * A complete structure of a Layout, consists of the structural information
 * about the paper where it lays on, and the layers it contains
 *
 * @author Dimas Y. Danurwenda <dimas@danurwenda.com>
 */
public class LayoutLSystem {

    public LayoutLSystem(LayoutDocument p, List<LayoutLayer> sls) {
        this.paper = p;
        this.layers = sls;
    }

    /**
     * Copy cons
     *
     * @param lls
     */
    public LayoutLSystem(LayoutLSystem lls) {
        LayoutDocument oriDoc = lls.paper;
        this.paper = new LayoutDocument(
                oriDoc.getWidth(),
                oriDoc.getHeight(),
                oriDoc.getDPI(),
                oriDoc.getUnit().toString(),
                oriDoc.getBackground()
        );
        this.layers = new ArrayList<>();
        for (LayoutLayer l : lls.getLayers()) {
            LayoutLayer clone;
            try {
                clone = l.clone();
                layers.add(clone);
            } catch (CloneNotSupportedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public List<LayoutLayer> getLayers() {
        return layers;
    }

    public void setLayers(List<LayoutLayer> sublayouts) {
        this.layers = sublayouts;
    }
    private List<LayoutLayer> layers;
    private LayoutDocument paper;

    public LayoutDocument getDocument() {
        return paper;
    }

    public void setPaper(LayoutDocument paper) {
        this.paper = paper;
    }
}
