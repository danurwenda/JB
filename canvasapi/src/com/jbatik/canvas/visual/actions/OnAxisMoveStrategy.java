/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.canvas.visual.actions;

import java.awt.Point;
import org.netbeans.api.visual.action.MoveStrategy;
import org.netbeans.api.visual.widget.Widget;

/**
 * Move on x or y axis, depends on which delta is larger.
 *
 * @author Dimas Y. Danurwenda
 */
public class OnAxisMoveStrategy implements MoveStrategy {

    @Override
    public Point locationSuggested(Widget widget, Point originalLocation, Point suggestedLocation) {
        int deltaW = Math.abs(originalLocation.x - suggestedLocation.x);
        int deltaH = Math.abs(originalLocation.y - suggestedLocation.y);
        if (deltaW >= deltaH) { // moving mostly horizontally
            return new Point(suggestedLocation.x, originalLocation.y);
        } else { // moving mostly vertically
            return new Point(originalLocation.x, suggestedLocation.y);
        }
    }

}
