/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.visual.actions;

import com.jbatik.core.format.DotDecimalFormat;
import com.jbatik.lsystem.parser.exceptions.ParseRuleException;
import com.jbatik.lsystem.util.LSCompacter;
import com.jbatik.modules.layout.layering.SubLayout;
import com.jbatik.modules.layout.visual.widgets.SquareWidget;
import com.jbatik.modules.layout.visual.widgets.SubLayoutWidget;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import org.netbeans.api.visual.action.ResizeControlPointResolver;
import org.netbeans.api.visual.action.ResizeProvider;
import org.netbeans.api.visual.action.ResizeStrategy;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * Resize action. Resizing will always maintain aspect ratio. Using modifier,
 * the center of dilation will be the center point of Widget, instead of the
 * opposite control point (default).
 *
 * @author Dimas Y. Danurwenda
 */
public class ResizeSelectedLayerAction extends WidgetAction.LockedAdapter {

    private ResizeStrategy strategy;
    private ResizeControlPointResolver resolver;

    private Widget resizingWidget = null;
    private ResizeProvider.ControlPoint controlPoint;
    private Rectangle originalSceneRectangle = null;
    private Rectangle suggestedBounds = null;
    private Insets insets;
    private Point dragSceneLocation = null;

    public ResizeSelectedLayerAction(ResizeStrategy strategy, ResizeControlPointResolver resolver) {
        this.strategy = strategy;
        this.resolver = resolver;
    }

    @Override
    protected boolean isLocked() {
        return resizingWidget != null;
    }

    @Override
    public WidgetAction.State mousePressed(Widget widget, WidgetAction.WidgetMouseEvent event) {
        if (isLocked()) {
            return WidgetAction.State.createLocked(widget, this);
        }
        if (event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() == 1) {
            insets = widget.getBorder().getInsets();
            controlPoint = resolver.resolveControlPoint(widget, event.getPoint());
            if (controlPoint != null) {
                resizingWidget = widget;
                originalSceneRectangle = null;
                if (widget.isPreferredBoundsSet()) {
                    originalSceneRectangle = widget.getPreferredBounds();
                }
                if (originalSceneRectangle == null) {
                    originalSceneRectangle = widget.getBounds();
                }
                if (originalSceneRectangle == null) {
                    originalSceneRectangle = widget.getPreferredBounds();
                }
                dragSceneLocation = widget.convertLocalToScene(event.getPoint());
                return WidgetAction.State.createLocked(widget, this);
            }
        }
        return WidgetAction.State.REJECTED;
    }

    @Override
    public WidgetAction.State mouseReleased(Widget widget, WidgetAction.WidgetMouseEvent event) {
        boolean state = resize(widget, event.getPoint());
        if (state) {
            resizingWidget = null;
            updateSubLayout(widget, originalSceneRectangle, suggestedBounds, controlPoint);
            widget.setPreferredBounds(null);
        }
        return state ? WidgetAction.State.CONSUMED : WidgetAction.State.REJECTED;
    }

    @Override
    public WidgetAction.State mouseDragged(Widget widget, WidgetAction.WidgetMouseEvent event) {
        return resize(widget, event.getPoint()) ? WidgetAction.State.createLocked(widget, this) : WidgetAction.State.REJECTED;
    }

    private boolean resize(Widget widget, Point newLocation) {
        if (resizingWidget != widget) {
            return false;
        }

        newLocation = widget.convertLocalToScene(newLocation);
        int dx = newLocation.x - dragSceneLocation.x;
        int dy = newLocation.y - dragSceneLocation.y;
        int minx = insets.left + insets.right;
        int miny = insets.top + insets.bottom;

        Rectangle rectangle = new Rectangle(originalSceneRectangle);
        switch (controlPoint) {
            case BOTTOM_CENTER:
                resizeToBottom(miny, rectangle, dy);
                break;
            case BOTTOM_LEFT:
                resizeToLeft(minx, rectangle, dx);
                resizeToBottom(miny, rectangle, dy);
                break;
            case BOTTOM_RIGHT:
                resizeToRight(minx, rectangle, dx);
                resizeToBottom(miny, rectangle, dy);
                break;
            case CENTER_LEFT:
                resizeToLeft(minx, rectangle, dx);
                break;
            case CENTER_RIGHT:
                resizeToRight(minx, rectangle, dx);
                break;
            case TOP_CENTER:
                resizeToTop(miny, rectangle, dy);
                break;
            case TOP_LEFT:
                resizeToLeft(minx, rectangle, dx);
                resizeToTop(miny, rectangle, dy);
                break;
            case TOP_RIGHT:
                resizeToRight(minx, rectangle, dx);
                resizeToTop(miny, rectangle, dy);
                break;
        }
        suggestedBounds = strategy.boundsSuggested(widget, originalSceneRectangle, rectangle, controlPoint);
        resizeSquares(originalSceneRectangle, suggestedBounds, controlPoint, widget);
        widget.setPreferredBounds(suggestedBounds);
        return true;
    }

    //redraw all square widgets in selected widget
    private void resizeSquares(Rectangle originalSceneRectangle, Rectangle sug, ResizeProvider.ControlPoint controlPoint, Widget widget) {
        double ratio = sug.getHeight() / originalSceneRectangle.getHeight();
        Point fixedPoint = widget.convertLocalToScene(resolveFixedPoint(originalSceneRectangle, controlPoint));
        Scene s = widget.getScene();
        if (s instanceof ObjectScene) {
            ObjectScene objectScene = (ObjectScene) s;

            for (Object sll : objectScene.getSelectedObjects()) {
                Widget w = objectScene.findWidget(sll);
                if (w instanceof SubLayoutWidget && w.isEnabled()) {
                    for (Widget c : w.getChildren()) {
                        if (c instanceof SquareWidget) {
                            SquareWidget sw = (SquareWidget) c;
                            sw.setResizeMode(true);
                            //adjust sidelength
                            sw.setResizingSideLength(sw.getSideLength() * ratio);
                            //translate the preferloc
                            Point ploc = w.convertLocalToScene(sw.getPreferredLocation());
                            int dx = ploc.x - fixedPoint.x;
                            int dy = ploc.y - fixedPoint.y;
                            double dxr = ratio * dx + fixedPoint.x;
                            double dyr = ratio * dy + fixedPoint.y;
                            sw.setResizingLocationDelta(dxr - ploc.x, dyr - ploc.y);
                        }
                    }
                }
            }
        }
    }

    private static Point resolveFixedPoint(Rectangle r, ResizeProvider.ControlPoint p) {
        switch (p) {
            case BOTTOM_RIGHT:
                return r.getLocation();
            case BOTTOM_LEFT:
                return new Point(r.x + r.width, r.y);
            case TOP_LEFT:
                return new Point(r.x + r.width, r.y + r.height);
            case TOP_RIGHT:
                return new Point(r.x, r.y + r.height);
            case TOP_CENTER:
                return new Point(r.x + r.width / 2, r.y + r.height);
            case CENTER_RIGHT:
                return new Point(r.x, r.y + r.height / 2);
            case CENTER_LEFT:
                return new Point(r.x + r.width, r.y + r.height / 2);
            case BOTTOM_CENTER:
                return new Point(r.x + r.width / 2, r.y);
        }
        return null;
    }

    private static void resizeToTop(int miny, Rectangle rectangle, int dy) {
        if (rectangle.height - dy < miny) {
            dy = rectangle.height - miny;
        }
        rectangle.y += dy;
        rectangle.height -= dy;
    }

    private static void resizeToBottom(int miny, Rectangle rectangle, int dy) {
        if (rectangle.height + dy < miny) {
            dy = miny - rectangle.height;
        }
        rectangle.height += dy;
    }

    private static void resizeToLeft(int minx, Rectangle rectangle, int dx) {
        if (rectangle.width - dx < minx) {
            dx = rectangle.width - minx;
        }
        rectangle.x += dx;
        rectangle.width -= dx;
    }

    private static void resizeToRight(int minx, Rectangle rectangle, int dx) {
        if (rectangle.width + dx < minx) {
            dx = minx - rectangle.width;
        }
        rectangle.width += dx;
    }

    private void updateSubLayout(Widget widget, Rectangle originalSceneRectangle, Rectangle sug, ResizeProvider.ControlPoint controlPoint) {
        double ratio = sug.getHeight() / originalSceneRectangle.getHeight();
        Point fixedPoint = widget.convertLocalToScene(resolveFixedPoint(originalSceneRectangle, controlPoint));
        Scene s = widget.getScene();
        if (s instanceof ObjectScene) {
            ObjectScene scene = (ObjectScene) s;
            for (Object sll : scene.getSelectedObjects()) {
                Widget w = scene.findWidget(sll);
                if (w instanceof SubLayoutWidget && w.isEnabled()) {
                    SubLayout sl = w.getLookup().lookup(SubLayout.class);
                    if (sl != null) {
                        //find delta from fixedpoint
                        Point wLocOnScene = w.getParentWidget().convertLocalToScene(w.getPreferredLocation());
                        double dx = wLocOnScene.getX() - fixedPoint.getX();
                        double dy = wLocOnScene.getY() - fixedPoint.getY();
                        Point endLocOnScene = new Point((int) (fixedPoint.x + ratio * dx), (int) (fixedPoint.y + ratio * dy));
                        Point endLocOnParent = w.getParentWidget().convertSceneToLocal(endLocOnScene);
                        w.setPreferredLocation(endLocOnParent);
                        sl.setLocation(endLocOnParent);
                        try {
                            sl.setRawAxiom(getModifiedAxiom(sl.getAxiom(), ratio));
                            sl.getRenderer().generate();
                        } catch (ParseRuleException ex) {
                            DialogDisplayer.getDefault().notify(
                                    new NotifyDescriptor.Message(ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE)
                            );
                        } finally {
                            sl.getRenderer().render();
                        }
                    }
                }
            }
        }
    }

    private static String getModifiedAxiom(String axiom, double ratio) throws ParseRuleException {
        String res = axiom;
        DecimalFormat fourDigit = new DecimalFormat("###.####", DotDecimalFormat.getSymbols());
        String formattedRatio = fourDigit.format(ratio);
        res = "\"(" + formattedRatio + ")" + res;
        res = "?(" + formattedRatio + ")" + res;
        return LSCompacter.simplifyPrefix(res, '"', '?');
    }

}
