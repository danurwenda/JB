/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.canvas.visual.actions;

import java.awt.Point;
import java.awt.event.KeyEvent;
import org.netbeans.api.visual.action.MoveProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author Dimas Y. Danurwenda <dimas@danurwenda.com>
 */
public class KeyboardMoveAction extends WidgetAction.Adapter {

    private MoveProvider provider;
    private ObjectScene scene;
    boolean consumed;

    public KeyboardMoveAction(ObjectScene s, MoveProvider MOVE_PROVIDER) {
        this.scene = s;
        this.provider = MOVE_PROVIDER;
    }

    @Override
    public WidgetAction.State keyPressed(Widget widget, WidgetAction.WidgetKeyEvent event) {
        if (widget.equals(scene.getFocusedWidget())) {
            Point originalSceneLocation = provider.getOriginalLocation(widget);
            int newY = originalSceneLocation.y;
            int newX = originalSceneLocation.x;
            consumed = false;
            if (event.getKeyCode() == KeyEvent.VK_UP) {
                consumed = true;
                newY = newY - 20;
            } else if (event.getKeyCode() == KeyEvent.VK_DOWN) {
                consumed = true;
                newY = newY + 20;
            } else if (event.getKeyCode() == KeyEvent.VK_RIGHT) {
                consumed = true;
                newX = newX + 20;
            } else if (event.getKeyCode() == KeyEvent.VK_LEFT) {
                consumed = true;
                newX = newX - 20;
            }
            if (consumed) {
                provider.movementStarted(widget);
                provider.setNewLocation(widget, new Point(newX, newY));

                return WidgetAction.State.CONSUMED;
            }
        }
        return WidgetAction.State.REJECTED;
    }

    @Override
    public WidgetAction.State keyReleased(Widget widget, WidgetAction.WidgetKeyEvent event) {
        if (consumed) {
            provider.movementFinished(widget);
            return WidgetAction.State.CONSUMED;
        }
        return WidgetAction.State.REJECTED;
    }
}
