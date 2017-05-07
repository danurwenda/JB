/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.visual.actions;

import com.jbatik.modules.layout.visual.LayoutScene;
import java.awt.Rectangle;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.api.visual.action.RectangularSelectProvider;
import org.netbeans.api.visual.widget.Widget;

/**
 * UNUSED
 *
 * @author Dimas Y. Danurwenda <dimas@danurwenda.com>
 */
public class LayoutSceneRectangularSelectProvider implements RectangularSelectProvider {

    private LayoutScene scene;

    public LayoutSceneRectangularSelectProvider(LayoutScene scene) {
        this.scene = scene;
    }

    /**
     * Will clear selection if selection is happened outside any SquareWidget
     *
     * @param sceneSelection
     */
    @Override
    public void performSelection(Rectangle sceneSelection) {
        System.err.println("dafuq");
        int w = sceneSelection.width;
        int h = sceneSelection.height;
        Rectangle rect = new Rectangle(w >= 0 ? 0 : w, h >= 0 ? 0 : h, w >= 0 ? w : -w, h >= 0 ? h : -h);
        rect.translate(sceneSelection.x, sceneSelection.y);
        HashSet<Object> set = new HashSet<Object>();
        Set<?> objects = scene.getObjects();
        for (Object object : objects) {
            Widget widget = scene.findWidget(object);
            if (widget == null) {
                continue;
            }
            Rectangle widgetRect = widget.convertLocalToScene(widget.getBounds());
            if (rect.contains(widgetRect)) {
                set.add(object);
            }
        }
        Iterator<Object> iterator = set.iterator();
        if (!iterator.hasNext()) {
//            scene.suggestSelectedLayer(null);
        }
        scene.setFocusedObject(iterator.hasNext() ? iterator.next() : null);
        scene.userSelectionSuggested(set, false);
    }

}
