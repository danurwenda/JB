/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.visual.widgets;

import com.jbatik.modules.layout.api.LayoutLibrary;
import com.jbatik.canvas.util.GeomUtil;
import com.jbatik.modules.layout.visual.SelectionWrapperWidget;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.WeakListeners;

/**
 * Most of the code on this class is imitating the behaviors of ImageWidget
 *
 * @author RAPID02
 */
public class SquareWidget extends Widget {

    final static BasicStroke stroke = new BasicStroke(2.0f);
    final static Color[] Colors = {
        new Color(0.0f, 0.5f, 0.0f),
        new Color(0.0f, 0.5f, 0.5f),
        new Color(0.0f, 1.0f, 0.0f),
        new Color(0.0f, 1.0f, 1.0f),
        new Color(0.0f, 0.0f, 1.0f),
        new Color(0.5f, 0.0f, 0.5f),
        new Color(1.0f, 0.0f, 0.0f),
        new Color(0.5f, 0.5f, 0.0f),
        new Color(1.0f, 0.0f, 1.0f),
        new Color(1.0f, 1.0f, 0.0f),
        new Color(0.5f, 0.5f, 0.5f),
        new Color(1.0f, 1.0f, 1.0f),};
    private LayoutLibrary image;
    private double sideLength;
    private double resizingSideLength;
    private Point2D.Double resizingLocation;
    private Point2D.Double direction;
    private int colorIndex;
    private Color color;
    private double theta;//rotation angle in radian
    private boolean gambarBorder = false;
    ChangeListener libraryChangeListener;
    ChangeListener lastListener;

    @Override
    protected void notifyStateChanged(ObjectState previousState, ObjectState state) {
        if (state.isSelected()) {
            gambarBorder = state.isHovered();
            repaint();
        }
    }

    public SquareWidget(Scene scene, float sideLength, int colorIndex) {
        super(scene);

        this.mirror = false;
        this.sideLength = sideLength;
        this.colorIndex = colorIndex;
        this.color = Colors[colorIndex % 12];
        createActions(SelectionWrapperWidget.TRANSFORM_TOOL).addAction(scene.createWidgetHoverAction());
        this.libraryChangeListener = new LibraryChangeListener();
        this.hideWhenMissing = false;// by default border is drawn
    }

    public SquareWidget(Scene scene, float currentWidth) {
        this(scene, currentWidth, 1);
    }

    @Override
    public boolean isHitAt(Point p) {
        if (!isVisible()) {
            return false;
        } else {
            AffineTransform squareTransform = new AffineTransform();
            double transx = sideLength / 2;
            double transy = sideLength / 2;
            if (mirror) {
                transx *= (direction.x - direction.y);
                transy *= (direction.y + direction.x);
            } else {
                transx *= (direction.x + direction.y);
                transy *= (direction.y - direction.x);
            }
            squareTransform.translate(transx, transy);
            squareTransform.rotate(
                    Math.toRadians(mirror ? -theta : theta)
                    + (mirror ? getAngularDirection() + Math.PI / 2 : getAngularDirection()));
            Rectangle2D box = new Rectangle2D.Double(0, 0, sideLength, sideLength);
            return squareTransform.createTransformedShape(box).contains(p);
        }
    }

    /**
     * SquareWidget may be rendered in rotated position. This function calculate
     * the smallest non-rotated rectangle that contains all part of widget.
     *
     * @return the calculated client area
     */
    @Override
    protected Rectangle calculateClientArea() {
        double l = resizeMode ? resizingSideLength : sideLength;
        Point2D.Double P = new Point2D.Double((direction.x * l) / 2, (direction.y * l) / 2);
        Point2D.Double O = new Point2D.Double();
        Point2D.Double A, A2, B, C, D;
        if (!mirror) {
            A = GeomUtil.rotate_point(O.x, O.y, Math.PI / 2, P.x, P.y);

            A2 = GeomUtil.rotate_point(A.x, A.y, Math.PI, P.x, P.y);
            B = GeomUtil.rotate_point(A2.x, A2.y, Math.toRadians(theta), A.x, A.y);
            C = GeomUtil.rotate_point(A.x, A.y, -Math.PI / 2, B.x, B.y);
            D = GeomUtil.rotate_point(B.x, B.y, Math.PI / 2, A.x, A.y);

        } else {
            A2 = GeomUtil.rotate_point(O.x, O.y, Math.PI / 2, P.x, P.y);

            A = GeomUtil.rotate_point(A2.x, A2.y, Math.PI, P.x, P.y);
            B = GeomUtil.rotate_point(A2.x, A2.y, Math.toRadians(-theta), A.x, A.y);
            C = GeomUtil.rotate_point(A.x, A.y, Math.PI / 2, B.x, B.y);
            D = GeomUtil.rotate_point(B.x, B.y, -Math.PI / 2, A.x, A.y);

        }
        double minx = Math.min(A.x, Math.min(B.x, Math.min(C.x, D.x)));
        double maxx = Math.max(A.x, Math.max(B.x, Math.max(C.x, D.x)));
        double miny = Math.min(A.y, Math.min(B.y, Math.min(C.y, D.y)));
        double maxy = Math.max(A.y, Math.max(B.y, Math.max(C.y, D.y)));
        return GeomUtil.roundRectangle(new Rectangle2D.Double(
                minx,
                miny,
                (maxx - minx),
                (maxy - miny)
        ));
    }

    /**
     * Paints the image widget. If it contains no image, the square will be
     * drawn. Unless it's on export-to-file mode.
     */
    @Override
    protected void paintWidget() {
        Graphics2D g2 = getGraphics();
        Font previousFont = g2.getFont();
        Stroke previousStroke = g2.getStroke();
        AffineTransform previousTransform = g2.getTransform();
        if (resizeMode) {
            //translate graphics
            AffineTransform t = g2.getTransform();
            t.translate(resizingLocation.x, resizingLocation.y);
            g2.setTransform(t);
        }
        double length = resizeMode ? resizingSideLength : sideLength;
        double transx = length / 2;
        double transy = length / 2;
        if (mirror) {
            transx *= (direction.x - direction.y);
            transy *= (direction.y + direction.x);
        } else {
            transx *= (direction.x + direction.y);
            transy *= (direction.y - direction.x);
        }

        g2.translate(transx, transy);
        g2.rotate(
                Math.toRadians(mirror ? -theta : theta)
                + (mirror ? getAngularDirection() + Math.PI / 2 : getAngularDirection()));
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if ((image == null || gambarBorder) && !hideWhenMissing) {
            g2.setPaint(color);
            g2.setStroke(new BasicStroke((float) (1.0 / getScene().getZoomFactor())));
            Rectangle2D box = new Rectangle2D.Double(0, 0, length, length);
            Ellipse2D.Double circle = new Ellipse2D.Double(0, 0, 5, 5);
            g2.draw(box);
            g2.draw(circle);
            //draw color index (number) proportionally with sidelength
            g2.setFont(previousFont.deriveFont((float) (sideLength / 4)));
            g2.drawString(Integer.toString(colorIndex), (float) length / 2, (float) length / 2);
        }
        if (image != null && !(image.isMissing() && hideWhenMissing)) {
            image.drawLibrary(g2, length, mirror);
        }
        g2.setStroke(previousStroke);
        g2.setTransform(previousTransform);
        g2.setFont(previousFont);
    }
    private boolean hideWhenMissing;

    public void setDrawWhenMissing(boolean v) {
        this.hideWhenMissing = !v;
    }

    public void setColorIndex(int index, Color c, LayoutLibrary i) {
        this.colorIndex = index;
        this.color = c;
        this.image = i;

    }

    public int getColorIndex() {
        return colorIndex;
    }

    public void setSideLength(double sideLength) {
        this.sideLength = sideLength;
    }

    public void setResizingSideLength(double sideLength) {
        this.resizingSideLength = sideLength;
    }

    public double getSideLength() {
        return sideLength;
    }

    public void setDirection(Point2D.Double point2d) {
        this.direction = point2d;
    }

    public Point2D.Double getDirection() {
        return direction;
    }

    public double getAngularDirection() {
        double atan;
        if (direction.x == 0) {
            if (direction.y == 1) {
                atan = Math.PI;
            } else {
                atan = 0;
            }
        } else {
            atan = Math.atan2(direction.y, direction.x) + Math.PI / 2;
        }
        return atan;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public double getSquareRotationAngle() {
        return theta;
    }

    public void setSquareRotationAngle(double squareRotationAngle) {
        this.theta = squareRotationAngle;
    }

    /**
     * Returns an image.
     *
     * @return the image
     */
    public LayoutLibrary getLibrary() {
        return image;
    }

    /**
     * Sets an image
     *
     * @param image the image
     */
    public void setLibrary(LayoutLibrary image) {
        if (this.image == image) {
            return;
        }
        setImageCore(image);
    }

    private void setImageCore(LayoutLibrary i) {
        if (i == image) {
            return;
        }
        if (image != null && lastListener != null) {
            //remove listener from previous image
            image.removeChangeListener(lastListener);
        }
        image = i;
        if (image != null) {
            lastListener = WeakListeners.change(libraryChangeListener, image);
            image.addChangeListener(lastListener);
        }
        repaint();
    }

    boolean resizeMode = false;

    public void setResizeMode(boolean b) {
        this.resizeMode = b;
    }

    public void setResizingLocationDelta(double dx, double dy) {
        this.resizingLocation = new Point2D.Double(dx, dy);
    }

    /**
     * mark this widget as having the same color index with currently aimed
     * widget in an assigning library action.
     */
    void kedip() {
        Graphics2D g2
                = (Graphics2D) getScene().getView().
                getGraphics();
        Stroke previousStroke = g2.getStroke();
        AffineTransform previousTransform = g2.getTransform();

        Point locOnParent = getPreferredLocation();
        Point ons = getParentWidget().convertLocalToScene(locOnParent);
        Point onv = getScene().convertSceneToView(ons);

        g2.translate(onv.x, onv.y);
        double z = getScene().getZoomFactor();
        g2.scale(z, z);
        double transx = sideLength / 2;
        double transy = sideLength / 2;
        if (mirror) {
            transx *= (direction.x - direction.y);
            transy *= (direction.y + direction.x);
        } else {
            transx *= (direction.x + direction.y);
            transy *= (direction.y - direction.x);
        }

        g2.translate(transx, transy);
        g2.rotate(
                Math.toRadians(mirror ? -theta : theta)
                + (mirror ? getAngularDirection() + Math.PI / 2 : getAngularDirection()));
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(color);
        g2.setStroke(new BasicStroke((float) (5.0 / getScene().getZoomFactor())));
        g2.draw(new Rectangle2D.Double(0, 0, sideLength, sideLength));

        g2.setStroke(previousStroke);
        g2.setTransform(previousTransform);
    }

    /**
     * ingat bahwa y positif arahnya ke bawah
     *
     * @return koordinat (0,0) relative
     */
    public Point getCorner() {
        double x = sideLength / 2;
        double y = sideLength / 2;
        double cx = mirror ? x * (direction.x - direction.y) : x * (direction.x + direction.y);
        double cy = mirror ? y * (direction.y + direction.x) : y * (direction.y - direction.x);

        return new Point((int) cx, (int) cy);
    }
    private boolean mirror;

    public void setMirrored(boolean mirror) {
        this.mirror = mirror;
    }

    private class LibraryChangeListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            repaint();
            getScene().validate();
        }
    }
}
