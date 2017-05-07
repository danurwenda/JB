/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.canvas.visual.actions;

import java.awt.Insets;
import java.awt.Rectangle;
import org.netbeans.api.visual.action.ResizeProvider;
import org.netbeans.api.visual.action.ResizeStrategy;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author Dimas Y. Danurwenda <dimas@danurwenda.com>
 */
public class ResizeWithRatioStrategy implements ResizeStrategy {

    @Override
    public Rectangle boundsSuggested(final Widget widget,
            final Rectangle originalBounds,
            final Rectangle suggestedBounds,
            final ResizeProvider.ControlPoint controlPoint) {
        int updateX = 0;//0 : unchanged, 1 : full dx, 2 : half dx
        int updateY = 0;//analog
        switch (controlPoint) {
            //these first three cases won't change the coordinate of suggested rectangle
            case TOP_CENTER:
            case BOTTOM_CENTER:
                updateY = 1;
                updateX = 2;
                break;
            case BOTTOM_LEFT:
                updateX = 1;
                break;

            case CENTER_LEFT:
            case CENTER_RIGHT:
                updateY = 2;
                updateX = 1;
                break;

            case TOP_LEFT:
                updateX = 1;
                updateY = 1;
                break;
            case TOP_RIGHT:
                updateY = 1;
                break;
        }
        final Rectangle result = new Rectangle(suggestedBounds);
        // We could compute aspectRatio from originalBounds,
        // but rounding errors would accumulate.
        // but THAT is exactly what we'll do here
        final Insets insets = widget.getBorder().getInsets();
        final int mw = insets.left + insets.right;
        final int mh = insets.bottom + insets.top;
        final int contentWidth = result.width - mw;
        final int contentHeight = result.height - mh;
        final double aspectRatio = originalBounds.getHeight() / originalBounds.getWidth();
        final double deltaW = Math.abs(suggestedBounds.getWidth() - originalBounds.getWidth());
        final double deltaH = Math.abs(suggestedBounds.getHeight() - originalBounds.getHeight());
        if (deltaW >= deltaH) { // moving mostly horizontally
            result.height = (int) (mh + Math.round(contentWidth * aspectRatio));
            if (updateY != 0) {
                //ubah y
                int dy = result.height - suggestedBounds.height;
                if (updateY == 2) {
                    result.y -= dy / 2;
                } else {
                    result.y -= dy;
                }
            }
        } else { // moving mostly vertically
            result.width = (int) (mw + Math.round(contentHeight / aspectRatio));
            if (updateX != 0) {
                //ubah x
                int dx = result.width - suggestedBounds.width;
                if (updateX == 2) {
                    result.x -= dx / 2;
                } else {
                    result.x -= dx;
                }
            }
        }

        return result;
    }
}
