/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.canvas.visual.actions;

import java.awt.Point;
import java.awt.event.MouseEvent;
import org.netbeans.api.visual.action.SelectProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author Dimas Y. Danurwenda <dimas@danurwenda.com>
 */
public final class NonAimedSelectAction extends WidgetAction.Adapter {

    private boolean invertSelection;
    private SelectProvider provider;
    private boolean trapRightClick = false;

    public NonAimedSelectAction(SelectProvider provider, boolean trapRightClick) {
        this.provider = provider;
        this.trapRightClick = trapRightClick;
    }

    public NonAimedSelectAction(SelectProvider provider) {
        this.provider = provider;
    }

    @Override
    public WidgetAction.State mousePressed(Widget widget, WidgetAction.WidgetMouseEvent event) {

        Point localLocation = event.getPoint();

        if (event.getButton() == MouseEvent.BUTTON1 || event.getButton() == MouseEvent.BUTTON2) {
            invertSelection = (event.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0;

            if (provider.isSelectionAllowed(widget, localLocation, invertSelection)) {

                provider.select(widget, localLocation, invertSelection);
                return WidgetAction.State.CHAIN_ONLY;

            }
        } else if (trapRightClick && event.getButton() == MouseEvent.BUTTON3) {
            provider.select(widget, localLocation, false);
            return WidgetAction.State.CHAIN_ONLY;
        }
        return WidgetAction.State.REJECTED;
    }

    @Override
    public WidgetAction.State mouseReleased(Widget widget, WidgetAction.WidgetMouseEvent event) {
        return State.REJECTED;
    }
}
