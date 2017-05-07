/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.visual;

import com.jbatik.canvas.visual.CanvasWidget;
import com.jbatik.canvas.visual.DesktopScene;
import com.jbatik.core.api.ProjectPathDependant;
import com.jbatik.modules.layout.LayoutDocument;
import com.jbatik.modules.layout.LayoutLSystem;
import com.jbatik.modules.layout.explorer.LayerController;
import com.jbatik.modules.layout.explorer.LayerFactory;
import com.jbatik.modules.layout.visual.widgets.SubLayoutWidget;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.visual.widget.Widget;
import org.openide.loaders.DataObject;

/**
 *
 * @author Dimas Y. Danurwenda
 */
class ImmutableLayoutScene extends DesktopScene implements ProjectPathDependant {

    String projectPath;
    private final PaperWidget paperWidget;

    ImmutableLayoutScene(DataObject obj, LayoutLSystem lSystem) {
        super();
        this.projectPath = FileOwnerQuery.getOwner(obj.getPrimaryFile()).getProjectDirectory().getPath();
        LayoutLSystem lSclone = new LayoutLSystem(lSystem);
        LayoutDocument ld = lSclone.getDocument();

        //paper layer
        this.paperWidget = new PaperWidget(this, ld);
        paperWidget.drawShadow = false;
        paperLayer.addChild(paperWidget);

        LayerController layerController = LayerFactory.createLayer(this, lSclone.getLayers());
        //main layer, where sublayout widgets will be added
        addChild(layerController.getRootWidget());

        for (Object o : getObjects()) {
            Widget w = findWidget(o);
            if (w instanceof SubLayoutWidget) {
                SubLayoutWidget slw = (SubLayoutWidget) w;
                slw.showEmptySquares(false);
            }
        }
    }

    @Override
    protected CanvasWidget getCanvasWidget() {
        return paperWidget;
    }

    @Override
    public String getProjectPath() {
        return projectPath;
    }

}
