/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.visual.actions;

import com.jbatik.canvas.visual.actions.OnAxisMoveStrategy;
import com.jbatik.modules.layout.layering.SubLayoutLayer;
import com.jbatik.modules.layout.visual.LayoutScene;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.MoveProvider;
import org.netbeans.api.visual.action.MoveStrategy;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Widget;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * Jadi moveselectedlayer ini moveaction tapi diterapkan di scene. Ini seperti
 * move layer nya photoshop, saat auto-select off. Jadi mousePressed nya bisa di
 * mana saja selama di atas Scene, lalu drag gesture akan menggeser SEMUA widget
 * yang selected.
 *
 * @author RAPID02
 */
public final class MoveSelectedLayerAction extends WidgetAction.LockedAdapter {

    private ObjectScene objScene;
    private MoveStrategy strategy;
    private MoveProvider provider;

    private Widget movingWidget = null;
    private Point dragSceneLocation = null;
    private Point originalSceneLocation = null;
    private Point initialMouseLocation = null;

    public MoveSelectedLayerAction(LayoutScene s, boolean axis) {
        this(s, axis ? new OnAxisMoveStrategy() : ActionFactory.createFreeMoveStrategy(), new MultiMoveProvider(s));
    }

    public MoveSelectedLayerAction(ObjectScene s, MoveStrategy strategy, MoveProvider provider) {
        this.objScene = s;
        this.strategy = strategy;
        this.provider = provider;
    }

    @Override
    protected boolean isLocked() {
        return movingWidget != null;
    }

    @Override
    public State mousePressed(Widget widget, WidgetAction.WidgetMouseEvent event) {
        if (isLocked()) {
            return State.createLocked(widget, this);
        }
        if (event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() == 1) {
            //cek apakah ada selected object
            Widget candidate = null;
            Set sel = objScene.getSelectedObjects();
            Iterator it = sel.iterator();
            while (candidate == null && it.hasNext()) {
                Object o = it.next();
                if (o instanceof SubLayoutLayer) {
                    candidate = objScene.findWidget(o);
                }
            }
            if (candidate == null) {
                //popup macam potosop
                NotifyDescriptor d
                        = new NotifyDescriptor.Message("Could not use the move tool because no layers are selected.", NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            } else {
                //cek apakah ada yang locked dari selected object
                boolean locked = false;
                for (Object o : objScene.getSelectedObjects()) {
                    Widget maybeDisabled = objScene.findWidget(o);
                    if (!maybeDisabled.isEnabled()) {
                        //popup macam potosop
                        NotifyDescriptor d
                                = new NotifyDescriptor.Message("Could not complete your request because the layer is locked.", NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(d);
                        locked = true;
                        break;
                    }
                }
                if (!locked) {
                    movingWidget = candidate;
                    initialMouseLocation = event.getPoint();
                    originalSceneLocation = provider.getOriginalLocation(movingWidget);
                    if (originalSceneLocation == null) {
                        originalSceneLocation = new Point();
                    }
                    dragSceneLocation = event.getPoint();
                    provider.movementStarted(movingWidget);
                    return State.createLocked(movingWidget, this);
                }
            }
        }
        return State.REJECTED;
    }

    @Override
    public State mouseReleased(Widget widget, WidgetAction.WidgetMouseEvent event) {
        boolean state;
        if (initialMouseLocation != null && initialMouseLocation.equals(event.getPoint())) {
            //tanpa digeser langsung released
            state = true;
        } else {
            state = move(widget, event.getPoint());
        }
        if (state) {
            movingWidget = null;
            dragSceneLocation = null;
            originalSceneLocation = null;
            initialMouseLocation = null;
            provider.movementFinished(widget);
        }
        return state ? State.CONSUMED : State.REJECTED;
    }

    @Override
    public State mouseDragged(Widget widget, WidgetAction.WidgetMouseEvent event) {
        return move(widget, event.getPoint()) ? State.createLocked(widget, this) : State.REJECTED;
    }

    private boolean move(Widget widget, Point newLocation) {
        if (movingWidget != widget) {
            return false;
        }
        initialMouseLocation = null;
        newLocation = widget.convertLocalToScene(newLocation);
        Point location = new Point(originalSceneLocation.x + newLocation.x - dragSceneLocation.x, originalSceneLocation.y + newLocation.y - dragSceneLocation.y);
        provider.setNewLocation(widget, strategy.locationSuggested(widget, originalSceneLocation, location));
        return true;
    }
}
