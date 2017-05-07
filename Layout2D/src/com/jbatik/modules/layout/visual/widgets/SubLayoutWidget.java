/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.visual.widgets;

import com.jbatik.modules.layout.api.LibMappable;
import com.jbatik.lsystem.VisualLSystemRenderer;
import com.jbatik.lsystem.parser.exceptions.ParseRuleException;
import com.jbatik.modules.layout.api.LayoutLibrary;
import com.jbatik.modules.layout.layering.SubLayout;
import com.jbatik.modules.layout.drawer.Canting2D;
import com.jbatik.modules.layout.layering.LayoutLayer;
import com.jbatik.modules.layout.layering.SubLayoutLayer;
import com.jbatik.modules.layout.layering.SubLayoutLayer.ImageMap;
import com.jbatik.modules.layout.node.LayoutFileUtil;
import com.jbatik.modules.layout.visual.LayoutScene;
import com.jbatik.canvas.visual.actions.RotateSquareMouseAction;
import com.jbatik.modules.layout.visual.SelectionWrapperWidget;
import com.jbatik.modules.layout.visual.actions.MultiSquareRotateProvider;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.Widget;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * A widget that represents a SubLayout. A SubLayoutWidget consists of one or
 * more SquareWidget.
 *
 * @author RAPID02
 */
public class SubLayoutWidget extends Widget
        implements VisualLSystemRenderer, LibMappable, LayoutLayerPresenter {

    private SubLayout sublayout;

    public SubLayout getSublayout() {
        return sublayout;
    }

    private Lookup lookup;
    private InstanceContent ic = new InstanceContent();
    private String projectPath;
    //list of SquareWidget grouped by its colorindex
    private Map<Integer, ArrayList<SquareWidget>> colorSquareMap;

    @Override
    protected void notifyStateChanged(ObjectState previousState, ObjectState state) {
        // so that the squares are selected also.
        for (Widget w : getChildren()) {
            w.setState(w.getState().deriveSelected(state.isSelected()));
        }
    }

    LayoutLayer layer;
    PropertyChangeListener pcl;
    String layername;

    public String getLayername() {
        return layername;
    }

    SubLayoutWidget(ObjectScene scene, SubLayoutLayer subLayoutLayer, String projectPath, WidgetAction libAction) {
        super(scene);
        this.colorSquareMap = new HashMap<>();
        this.sublayout = subLayoutLayer.getSublayout();
        this.layername = subLayoutLayer.getName();
        sublayout.setRenderer(this);
        this.projectPath = projectPath;
        ic.add(sublayout);
        setPreferredLocation(subLayoutLayer.getLocation());

        //init state of accepting library
        if (libAction != null) {
            getActions().addAction(libAction);
        }

        createActions(SelectionWrapperWidget.TRANSFORM_TOOL).addAction(new RotateSquareMouseAction(new MultiSquareRotateProvider(scene), false));
        createActions(SelectionWrapperWidget.SHIFTED_TRANSFORM_TOOL).addAction(new RotateSquareMouseAction(new MultiSquareRotateProvider(scene), true));
        lookup = new AbstractLookup(ic);
        this.layer = subLayoutLayer;
        setVisible(subLayoutLayer.isVisible());
        setEnabled(!subLayoutLayer.isLocked());
        setMappable(subLayoutLayer.isMappable());
        this.pcl = new SubLayoutPCL(this);
        layer.addPropertyChangeListener(WeakListeners.propertyChange(pcl, layer));
        generateAndRender();
    }

    private void generateAndRender() {
        try {
            generate();
        } catch (ParseRuleException ex) {
            DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(ex.getMessage() + " di layer " + layername, NotifyDescriptor.ERROR_MESSAGE)
            );
        } finally {
            render();
        }
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    /**
     * A hacky overriding to eliminate the unnecessary inclusion of point (0,0)
     * (in local coordinate system) on returned rectangle
     *
     * @return
     */
    @Override
    protected Rectangle calculateClientArea() {
        if (!getChildren().isEmpty()) {
            Widget child = getChildren().get(0);
            Point location = child.getLocation();
            Rectangle bounds = child.getBounds();
            bounds.translate(location.x, location.y);
            return bounds;
        }
        return super.calculateClientArea();
    }

    @Override
    public boolean isHitAt(Point local) {
        if (!layer.isVisibleOnScene()) {
            return false;
        }
        if (getState().isFocused()) {
            return super.isHitAt(local);
        } else {
            boolean hit = false;
            Point onChildren;
            for (Widget w : getChildren()) {
                onChildren = new Point(local);
                onChildren.translate(-w.getLocation().x, -w.getLocation().y);
                hit = w.isHitAt(onChildren);
                if (hit) {
                    break;
                }
            }
            return hit;
        }
    }

    public SquareWidget hitSquare(Point local) {
        if (!isVisible()) {
            return null;
        }
        SquareWidget hitSquare = null;
        Point onChildren;
        for (Widget w : getChildren()) {
            onChildren = new Point(local);
            onChildren.translate(-w.getLocation().x, -w.getLocation().y);
            if (w.isHitAt(onChildren)) {
                hitSquare = (SquareWidget) w;
                break;
            }
        }
        return hitSquare;
    }

    private void generateSquareWidgets() {
        Canting2D canting = new Canting2D(sublayout);
        canting.render(getScene());
        HashMap<Integer, ArrayList<SquareWidget>> m = canting.getColorSquareMap();
        //reset map
        colorSquareMap.clear();
        colorSquareMap.putAll(m);
        //reset children
        removeChildren();
        m.entrySet().stream().forEach((e) -> {
            e.getValue().stream().forEach((child) -> {
                addChild(child);
            });
        });
        //setimages
        colorSquareMap.entrySet().stream().forEach((e) -> {
            repaintColorIndex(e.getKey());
        });
        getScene().validate();
        notifyStateChanged(null, getState());//propagate the state to the new children
    }

    /**
     * Repaint all SquareWidget children with specified colorIndex. This method
     * should be invoked after any change in SubLayout's Map of colorIndex
     * and/or image name.
     *
     * @param idx
     */
    public void repaintColorIndex(int idx) {
        ArrayList<SquareWidget> list = getSquareWidgetList(idx, false);
        if (list != null && !list.isEmpty()) {
            String img = sublayout.getImageForIndex(idx);
            LayoutLibrary f = LayoutFileUtil.getLibraryImage(projectPath, img);
            list.stream().forEach((w) -> {
                w.setLibrary(
                        img == null
                                ? null
                                : f
                );
            });
        }
    }

    public void addSquareWidget(int idx, SquareWidget child) {
        getSquareWidgetList(idx, true).add(child);
        addChild(child);
    }

    private ArrayList<SquareWidget> getSquareWidgetList(int idx, boolean create) {
        ArrayList<SquareWidget> list = colorSquareMap.get(idx);
        if (list == null && create) {
            list = new ArrayList<>();
            colorSquareMap.put(idx, list);
        }
        return list;
    }

    @Override
    public void generate() throws ParseRuleException {
        sublayout.setAxiom(sublayout.getRawAxiom(), false);
        sublayout.setStringRules(sublayout.getRawDetails(), false);
    }

    @Override
    public void render() {
        generateSquareWidgets();
    }

    public void kedip2(int idx) {
        ArrayList<SquareWidget> list = colorSquareMap.get(idx);
        list.stream().forEach((sw) -> {
            sw.kedip();
        });
    }

    public void showEmptySquares(boolean par0) {
        for (Widget w : getChildren()) {
            SquareWidget sw = (SquareWidget) w;
            sw.setDrawWhenMissing(par0);
        }
    }

    boolean mappable;

    @Override
    public boolean isMappable() {
        return mappable;
    }

    @Override
    public void setMappable(boolean m) {
        this.mappable = m;
    }

    @Override
    public LayoutLayer getLayer() {
        return layer;
    }

    /**
     * Listening on property change on sublayout. Put savable and undoable edit
     * based on the change type.
     */
    private class SubLayoutPCL extends LayoutLayerWidgetPCL {

        public SubLayoutPCL(Widget widget) {
            super(widget);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            super.propertyChange(evt);
            String type = evt.getPropertyName();
            switch (type) {
                case SubLayout.X_PROP:
                case SubLayout.Y_PROP:
                    setPreferredLocation(((SubLayoutLayer) layer).getLocation());
                    getScene().validate();
                    break;
                case SubLayout.ANGLE_PROP:
                case SubLayout.ANGLE_MULT_PROP:
                case SubLayout.ITERATION_PROP:
                case SubLayout.LENGTH_PROP:
                case SubLayout.LENGTH_MULT_PROP:
                case SubLayout.WIDTH_PROP:
                case SubLayout.WIDTH_MULT_PROP:
                case SubLayout.SQROT_PROP:
                    render();
                    break;
                case SubLayoutLayer.INDEXED_MAP:
                    //repaint on that index only
                    ImageMap map = (ImageMap) evt.getNewValue();
                    repaintColorIndex(map.getIdx());
                    break;
                case SubLayoutLayer.IMAGES_MAP:
                    //repaint all
                    colorSquareMap.entrySet().stream().forEach((e) -> {
                        repaintColorIndex(e.getKey());
                    });
                    getScene().validate();
                    break;
                case LayoutLayer.LAYER_NAME_PROP:
                    layername = getLayer().getName();
                    break;
            }
            if (getScene() instanceof LayoutScene) {
                LayoutScene layoutScene = (LayoutScene) getScene();
                layoutScene.addSavable();
            }
        }
    }

}
