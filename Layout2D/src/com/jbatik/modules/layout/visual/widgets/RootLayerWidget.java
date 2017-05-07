/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.visual.widgets;

import com.jbatik.core.api.ProjectPathDependant;
import com.jbatik.modules.layout.explorer.node.LayerList;
import com.jbatik.modules.layout.layering.GroupLayer;
import com.jbatik.modules.layout.layering.LayoutLayer;
import com.jbatik.modules.layout.layering.SubLayoutLayer;
import com.jbatik.modules.layout.visual.LayoutScene;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.WeakListeners;

/**
 * A wrapper for mainLayer.
 *
 * @author Dimas Y. Danurwenda
 */
public class RootLayerWidget extends LayerWidget {

    private final LayerList childrenModel;
    private ObjectScene scene;
    ChangeListener cl;

    public RootLayerWidget(ObjectScene s, LayerList model) {
        super(s);
        this.childrenModel = model;
        this.scene = s;
        this.cl = new ChildrenChangeListener();
        childrenModel.addChangeListener(WeakListeners.change(cl, childrenModel));
        //generate children + add object-widget mapping to the objectscene for the first time
        generateChildren();
    }

    @Override
    protected boolean isRepaintRequiredForRevalidating() {
        return true;
    }

    private void generateChildren() {
        List<LayoutLayer> reversed = new ArrayList<>(childrenModel.list());
        Collections.reverse(reversed);

        reversed.stream().forEach((LayoutLayer ll) -> {
            if (ll instanceof GroupLayer) {
                GroupLayer groupLayer = (GroupLayer) ll;
                Widget w = scene.findWidget(groupLayer);
                if (w == null) {
                    w = new GroupLayerWidget(scene, groupLayer);
                    scene.addObject(groupLayer, w);
                } else {
                    w.removeFromParent();
                }
                //add into this
                addChild(w);
            } else if (ll instanceof SubLayoutLayer) {
                SubLayoutLayer subLayoutLayer = (SubLayoutLayer) ll;
                //seek for registered widget for this layer
                Widget w = scene.findWidget(subLayoutLayer);
                if (w == null) {
                    //create the widget
                    WidgetAction libAction = null;
                    if (scene instanceof LayoutScene) {
                        LayoutScene layoutScene = (LayoutScene) scene;
                        libAction = layoutScene.getLibAction();
                    }
                    String projectPath = "";
                    if (scene instanceof ProjectPathDependant) {
                        ProjectPathDependant projectPathDependant = (ProjectPathDependant) scene;
                        projectPath = projectPathDependant.getProjectPath();
                    }
                    w = new SubLayoutWidget(scene, subLayoutLayer, projectPath, libAction);
                    //unlike GroupLayerWidget[?]
                    //we add SubLayoutWidgets into object-widget mapping
                    scene.addObject(subLayoutLayer, w);
                } else {
                    w.removeFromParent();
                }
                //add into this
                addChild(w);
            }
        });

    }

    /**
     * Reflects changes on model into widgets.
     */
    private class ChildrenChangeListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            //listen to the changes in tree layer structure
            //like addition/removal/reordering of layers
            //first, we have to remove all children widgets
            //from object-widget mapping on layoutscene
            for (Widget w : getChildren()) {
                Object o = scene.findObject(w);
                if (o != null) {
                    scene.removeObject(o);
                }
            }
            //then remove the widget(s) from scene
            removeChildren();

            //after that, redraw the children
            generateChildren();
            scene.validate();
            if (scene instanceof LayoutScene) {
                LayoutScene layoutScene = (LayoutScene) scene;
                layoutScene.addSavable();
            }
        }
    }
}
