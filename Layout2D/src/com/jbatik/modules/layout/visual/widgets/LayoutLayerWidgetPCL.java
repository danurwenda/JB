/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.visual.widgets;

import com.jbatik.modules.layout.api.LibMappable;
import com.jbatik.modules.layout.layering.LayoutLayer;
import com.jbatik.modules.layout.visual.LayoutScene;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.api.visual.widget.Widget;

/**
 * A helper PCL for widget listening to LayoutLayer.
 *
 * @author RAPID02
 */
public class LayoutLayerWidgetPCL implements PropertyChangeListener {

    private final Widget widget;

    public LayoutLayerWidgetPCL(Widget widget) {
        this.widget = widget;
    }

    /**
     * If you override this method, please handle the undoable & savable.
     *
     * @param evt
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String type = evt.getPropertyName();
        switch (type) {
            case LayoutLayer.LAYER_LOCKED_PROP:
                boolean locked = (boolean) evt.getNewValue();
                widget.setEnabled(!locked);
                trySave();
                break;
            case LayoutLayer.LAYER_MAPPABLE_PROP:
                if (widget instanceof LibMappable) {
                    LibMappable libMappable = (LibMappable) widget;
                    libMappable.setMappable((boolean) evt.getNewValue());
                }
                trySave();
                break;
            case LayoutLayer.LAYER_VISIBILITY_PROP:
                widget.setVisible((boolean) evt.getNewValue());
                widget.getScene().validate();
            case LayoutLayer.LAYER_NAME_PROP:
                trySave();
        }
    }

    private void trySave() {
        if (widget.getScene() instanceof LayoutScene) {
            LayoutScene layoutScene = (LayoutScene) widget.getScene();
            layoutScene.addSavable();
        }
    }

}
