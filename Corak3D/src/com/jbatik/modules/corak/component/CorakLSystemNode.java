/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.corak.component;

import com.jbatik.modules.corak.CorakLSystem;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;

/**
 *
 * @author Dimas Y. Danurwenda
 */
class CorakLSystemNode extends FilterNode {

    CorakLSystemPropertyChangeSupport wrapper;

    public CorakLSystemNode(Node ori, CorakLSystemPropertyChangeSupport w) {
        super(ori);
        this.wrapper = w;
    }

    @Override
    public PropertySet[] getPropertySets() {
        return createSheet().toArray();
    }

    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set set = Sheet.createPropertiesSet();

        try {

            //L-System properties
            Property iter = new PropertySupport.Reflection(wrapper, int.class, CorakLSystem.ITERATION_PROP);
            Property angle = new PropertySupport.Reflection(wrapper, int.class, CorakLSystem.ANGLE_PROP);
            Property length = new PropertySupport.Reflection(wrapper, int.class, CorakLSystem.LENGTH_PROP);
            Property width = new PropertySupport.Reflection(wrapper, int.class, CorakLSystem.WIDTH_PROP);

            set.put(iter);
            set.put(angle);
            set.put(length);
            set.put(width);

        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        }

        sheet.put(set);
        return sheet;
    }

}
