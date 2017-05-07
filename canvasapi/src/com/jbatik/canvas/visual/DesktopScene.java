/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.canvas.visual;

import com.jbatik.canvas.actions.StrategicZoom;
import com.jbatik.canvas.util.GeomUtil;
import com.jbatik.canvas.util.SceneUtil;
import com.jbatik.canvas.visual.actions.DesktopZoomAction;
import com.jbatik.core.api.FitView;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JComponent;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.lookup.InstanceContent;

/**
 * Lumayan
 *
 * @author RAPID02
 */
public abstract class DesktopScene extends ObjectScene
        implements FitView {

    public DesktopScene() {
        //desktop, lowest level of layer
        this.desktopLayer = new DesktopLayer(this);
        addChild(desktopLayer);
        
        //slot for paper
        this.paperLayer = new LayerWidget(this);
        addChild(paperLayer);

        //zoom
        //always enabled disregard the active tool
        zoomAction = new DesktopZoomAction(2, desktopLayer) {

            @Override
            public Rectangle getRectangle() {
                return getWorkingRectangle(true);
            }
        };
        getActions().addAction(zoomAction);

        //add strategic zoom
        ic.add(toPageZoom);
        ic.add(new ToWidthZoom(this));
        ic.add(new ToHeightZoom(this));
        ic.add(new ToFitZoom(this));
        
    }
    //============================LAYERS================================

    /**
     * a layer that represents the desk surface where the paper is laid. This
     * layer is required when displaying getCanvasWidget() in a specific ZoomLevel.
     * For example, when using "To Height" ZoomLevel, the vertical scrollbar is
     * placed in the middle instead of not shown at all. This layer contains
     * "dummy widget"s that represent the corners of the desktop.
     */
    protected DesktopLayer desktopLayer;

    /**
     * The working paper
     */
    protected LayerWidget paperLayer;
    protected abstract CanvasWidget getCanvasWidget();
    //============================LOOKUP===================================

    /**
     * A general utility content
     */
    protected InstanceContent ic = new InstanceContent();


    //================STRATEGIC ZOOM ==========================================
    private ToPageZoom toPageZoom = new ToPageZoom(this);
    /**
     * Zoom.
     */
    private final WidgetAction zoomAction;

    public final void toPage() {
        toPageZoom.zoom();
    }

    /**
     * Mempertahankan center dari current visible rect. Trus jadikan zoom
     * factornya sebesar d dengan centernya tidak bergeser. Bisa jadi yang
     * digeser adalah desktop corners.
     *
     * @param d zoom factor
     */
    public final void setCenteredZoomFactor(double d) {
        JComponent view = getView();
        if (view != null) {
            Rectangle viewBounds = view.getVisibleRect();
            SceneUtil.properZoom(this, d, convertViewToScene(GeomUtil.center(viewBounds)),
                    desktopLayer.getTopLeft(),
                    desktopLayer.getBottomRight(),
                    getWorkingRectangle(true));
        } else {
            setZoomFactor(d);
        }
    }

    @Override
    public void optimalView() {
        toPage();
    }

    protected Rectangle getWorkingRectangle(boolean withPaper) {
        Rectangle working = null;
        if (withPaper) {
            working = getCanvasWidget().getPreferredBounds();
        }
        return working;
    }

    protected Rectangle getSelectedRectangle(boolean visibleOnly) {
        Rectangle working = null;
        for (Object o : getSelectedObjects()) {
            Widget w = findWidget(o);
            if (w != null) {
                if (!visibleOnly || (visibleOnly && w.isVisible())) {
                    Point wLoc = w.getPreferredLocation();
                    Rectangle wRect = w.getBounds();
                    if (wRect != null) {
                        wRect.translate(wLoc.x, wLoc.y);
                        if (working == null) {
                            working = wRect;
                        }
                        working.add(wRect);
                    }
                }
            }
        }
        return working;
    }

    /**
     * When set to false, will hide all visible widgets except those widgets
     * representing real objects above the paper.
     * @param show set to true to show workplace
     */
    public void setWorkplaceVisible(boolean show) {
        getCanvasWidget().setVisible(show);
    }

    public Rectangle getPaperBounds() {
        return getCanvasWidget().getPreferredBounds();
    }


    //tofitzoom hanya di add ke lookup jika ada SHOWN sublayoutwidget
    //ini semacam toPage terhadap widgets rectangle dengan buffer sebesar
    //to height/to width
    private class ToFitZoom extends StrategicZoom.ViewDependent {

        private final double FIT_GAP_RATIO = 0.02;

        public ToFitZoom(Scene scene) {
            super(scene);
        }

        @Override
        public String getDisplayName() {
            return "To Fit";
        }

        @Override
        public void doZoom(Rectangle rekt) {
            Rectangle w;
            if (getSelectedObjects().isEmpty()) {
                //fit all visible object
                w = getWorkingRectangle(false);
            } else {
                //fit selected object only
                w = getSelectedRectangle(true);
            }
            if (w != null) {
                w = getWorkingRectangle(true);
                double pw = w.getWidth();
                //      -paper.height
                double ph = w.getHeight();
                //      -Rectangle that contains paper and all Widget on mainLayer
                //      -container.width
                double rw = rekt.width;
                //      -container.height
                double rh = rekt.height;
                //  menghasilkan 
                //      -proper zoom level 
                //      -proper location of visible rectangle
                //      -proper location of two corners of the desktopLayer 

                double z;
                //step 1 : find zoom level
                if (rw * ph > pw * rh) {
                    //bounded by vertical space
                    //satisfies rh(1-2g)=z*ph
                    z = rh * (1 - 2 * FIT_GAP_RATIO) / ph;
                } else {
                    z = rw * (1 - 2 * FIT_GAP_RATIO) / pw;
                }
                SceneUtil.properZoom(getScene(), z, w, desktopLayer.getTopLeft(), desktopLayer.getBottomRight(), getWorkingRectangle(true));
            }
        }
    }

    private class ToPageZoom extends StrategicZoom.ViewDependent {

        private final double BUFFER_GAP_RATIO = 0.1;

        public ToPageZoom(Scene scene) {
            super(scene);
        }

        @Override
        public String getDisplayName() {
            return "To Page";
        }

        @Override
        public void doZoom(Rectangle rekt) {
            // di sini, terjamin bahwa rw rh are known
            /**
             * TO_PAGE mode itu menempatkan kertas di tengah2 layar dengan
             * tingkat zoom tertentu sedemikian sehingga ada gap yang enak
             * dilihat antara edge of paper dengan edge of visible rekt
             */
            // parameter yang dibutuhkan adalah 
            //      -paper.width
            double pw = getCanvasWidget().getWidth();
            //      -paper.height
            double ph = getCanvasWidget().getHeight();
            //      -Rectangle that contains paper and all Widget on mainLayer
            //      -container.width
            double rw = rekt.width;
            //      -container.height
            double rh = rekt.height;
            //  menghasilkan 
            //      -proper zoom level 
            //      -proper location of visible rectangle
            //      -proper location of two corners of the desktopLayer 
            double z;
            //step 1 : find zoom level
            if (rw * ph > pw * rh) {
                //bounded by vertical space
                //satisfies rh(1-2g)=z*ph
                z = rh * (1 - 2 * BUFFER_GAP_RATIO) / ph;
            } else {
                z = rw * (1 - 2 * BUFFER_GAP_RATIO) / pw;
            }
            SceneUtil.properZoom(getScene(), z, getCanvasWidget().getPreferredBounds(), desktopLayer.getTopLeft(), desktopLayer.getBottomRight(), getWorkingRectangle(true));
        }
    }

    private class ToWidthZoom extends StrategicZoom.ViewDependent {

        private final double WIDTH_GAP_RATIO = 0.02;

        public ToWidthZoom(Scene scene) {
            super(scene);
        }

        @Override
        public String getDisplayName() {
            return "To Width";
        }

        @Override
        public void doZoom(Rectangle rekt) {

            double pw = getCanvasWidget().getWidth();
            double rw = rekt.width;

            double z = rw * (1 - 2 * WIDTH_GAP_RATIO) / pw;
            //step 1 : find zoom level
            //zoom level always satisfy rw(1-2g)=pw.z
            SceneUtil.properZoom(getScene(), z, getCanvasWidget().getPreferredBounds(), desktopLayer.getTopLeft(), desktopLayer.getBottomRight(), getWorkingRectangle(true));
        }
    }

    private class ToHeightZoom extends StrategicZoom.ViewDependent {

        private final double HEIGHT_GAP_RATIO = 0.02;

        public ToHeightZoom(Scene scene) {
            super(scene);
        }

        @Override
        public String getDisplayName() {
            return "To Height";
        }

        @Override
        public void doZoom(Rectangle r) {
            double ph = getCanvasWidget().getHeight();
            double rh = r.height;

            double z = rh * (1 - 2 * HEIGHT_GAP_RATIO) / ph;
            //step 1 : find zoom level
            //zoom level always satisfy rw(1-2g)=pw.z
            SceneUtil.properZoom(getScene(), z, getCanvasWidget().getPreferredBounds(), desktopLayer.getTopLeft(), desktopLayer.getBottomRight(), getWorkingRectangle(true));
        }
    }

    public class FixCenterZoom extends StrategicZoom {

        private final double zoomMultiplier;

        public FixCenterZoom(double z) {
            zoomMultiplier = z;
        }

        @Override
        public String getDisplayName() {
            return String.valueOf((int) Math.floor(zoomMultiplier * 100)) + "%";
        }

        @Override
        public void zoom() {
            setCenteredZoomFactor(zoomMultiplier);
        }
    }
}
