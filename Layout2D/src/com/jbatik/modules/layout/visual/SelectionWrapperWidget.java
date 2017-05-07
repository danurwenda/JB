 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.visual;

import com.jbatik.canvas.visual.actions.ResizeWithRatioStrategy;
import com.jbatik.modules.layout.layering.SubLayoutLayer;
import com.jbatik.modules.layout.visual.actions.MoveSelectedLayerAction;
import com.jbatik.modules.layout.visual.actions.ResizeSelectedLayerAction;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.api.visual.action.ResizeControlPointResolver;
import org.netbeans.api.visual.action.ResizeProvider;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.model.ObjectSceneEvent;
import org.netbeans.api.visual.model.ObjectSceneEventType;
import org.netbeans.api.visual.model.ObjectSceneListener;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 * Handle all selected widget on scene. Any action being applied to this widget
 * will also applies to all selected widget on scene. Widget may veto the action
 * tho.
 *
 * @author RAPID02
 */
public class SelectionWrapperWidget extends Widget {

    private ObjectScene scene;
    private final TransformBorder TRANSFORM_BORDER = new TransformBorder();
    private final Border WRAPPER_BORDER = new WrapperBorder();
    public static final String TRANSFORM_TOOL = "transform";//resize from opposite + free rotate + rotate square
    public static final String RESIZE_FROM_CENTER_TOOL = "resizeshift";
    public static final String SHIFTED_TRANSFORM_TOOL = "rotatectrl";

    private Listener listener = new Listener();

    public SelectionWrapperWidget(LayoutScene s) {
        super(s);
        this.scene = s;

        //add action
        createActions(TRANSFORM_TOOL).addAction(new ResizeSelectedLayerAction(new ResizeWithRatioStrategy(), TRANSFORM_BORDER.getControlPointResolver()));
        //TODO : create action for left click on control points that pop a dialog asking for precise resizing with ratio
        createActions(TRANSFORM_TOOL).addAction(new MoveSelectedLayerAction(s, false));//TODO : create singleton
        createActions(SHIFTED_TRANSFORM_TOOL).addAction(new MoveSelectedLayerAction(s, true));//TODO : create singleton
        //TODO : create action for left click on control points that pop a dialog asking for precise translation

        scene.addObjectSceneListener(listener, ObjectSceneEventType.OBJECT_SELECTION_CHANGED);
        scene.addSceneListener(listener);

    }

    @Override
    protected Rectangle calculateClientArea() {
        Widget candidate = null;
        Set objects = scene.getSelectedObjects();
        Iterator it = objects.iterator();
        while (candidate == null && it.hasNext()) {
            //see whether it.next is a SLL
            Object o = it.next();
            if (o instanceof SubLayoutLayer) {
                candidate = scene.findWidget(o);
            }
        }
        if (candidate == null) {
            //out of while loop, but candidate still == null
            setBorder(BorderFactory.createEmptyBorder());
            return new Rectangle(0, 0, 0, 0);
        } else {
            //return a rectangle that contains all selected object
            //get the widget of the first selected object
            Widget first = candidate;
            Point fp = first.getPreferredLocation();
            Rectangle f = first.getPreferredBounds();
            f.translate(fp.x, fp.y);
            while (it.hasNext()) {
                Object o = it.next();
                if (o instanceof SubLayoutLayer) {
                    Widget w = scene.findWidget(o);
                    Point p = w.getPreferredLocation();
                    Rectangle r = w.getPreferredBounds();
                    r.translate(p.x, p.y);
                    f.add(r);
                }
            }
            return f;
        }
    }

    void activateTransform() {
        setBorder(TRANSFORM_BORDER);
//        scene.addObjectSceneListener(listener, ObjectSceneEventType.OBJECT_SELECTION_CHANGED);
//        scene.addSceneListener(listener);
        scene.validate();
    }

    void deactivateTransform() {
        setBorder(WRAPPER_BORDER);
//        scene.removeObjectSceneListener(listener, ObjectSceneEventType.OBJECT_SELECTION_CHANGED);
//        scene.removeSceneListener(listener);
        scene.validate();
    }

    /**
     * Border untuk mode transform. Terdiri atas (maximum) 8 titik control.
     * Mungkin bagus juga kalau dikasih garis tepi. Besarnya titik control
     * adalah 8x8 piksel, selalu digambar 8x8 DI LEVEL ZOOM BERAPAPUN. Nah
     * masalahnya kalau level zoom udah di 8x, berarti 8x8 ini sebenarnya adalah
     * 1x1 piksel kan ya.
     *
     * Lihat photoshop buat referensi.
     */
    private class TransformBorder extends WrapperBorder {

        private final int THICKNESS = 8;
        Rectangle2D.Double TL, TC, TR, LC, RC, BL, BC, BR;

        public TransformBorder() {
            TL = new Rectangle2D.Double();
            TC = new Rectangle2D.Double();
            TR = new Rectangle2D.Double();
            LC = new Rectangle2D.Double();
            RC = new Rectangle2D.Double();
            BL = new Rectangle2D.Double();
            BC = new Rectangle2D.Double();
            BR = new Rectangle2D.Double();
            OUTLINE = new BasicStroke(0.0f, BasicStroke.JOIN_BEVEL, BasicStroke.CAP_BUTT, 5.0f, new float[]{6.0f, 3.0f}, 0.0f);
        }

        /**
         * Menggambar 4 titik kontrol di ujung2 wrapper. Jika masih ada space,
         * menggambar 4 titik kontrol tambahan di midpoint setiap sisi wrapper.
         *
         * @param gr
         * @param bounds
         */
        @Override
        public void paint(Graphics2D gr, Rectangle bounds) {
            super.paint(gr, bounds);

            //drawing titik control
            double tz = THICKNESS / scene.getZoomFactor();
            //TOP-LEFT
            TL.setRect(bounds.x, bounds.y, tz, tz);
            gr.fill(TL);
            //TOP-RIGHT
            TR.setRect(bounds.x + bounds.width - tz, bounds.y, tz, tz);
            gr.fill(TR);
            //BOTTOM-LEFT
            BL.setRect(bounds.x, bounds.y + bounds.height - tz, tz, tz);
            gr.fill(BL);
            //BOTTOM-RIGHT
            BR.setRect(bounds.x + bounds.width - tz, bounds.y + bounds.height - tz, tz, tz);
            gr.fill(BR);
            Point center = new Point(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
            if (bounds.width >= tz * 5) {
                //top-center
                TC.setRect(center.x - tz / 2, bounds.y, tz, tz);
                gr.fill(TC);
                //bot-center
                BC.setRect(center.x - tz / 2, bounds.y + bounds.height - tz, tz, tz);
                gr.fill(BC);
            } else {
                TC.setRect(0, 0, 0, 0);
                BC.setRect(0, 0, 0, 0);
            }
            if (bounds.height >= tz * 5) {
                LC.setRect(bounds.x, center.y - tz / 2, tz, tz);
                gr.fill(LC);
                RC.setRect(bounds.x + bounds.width - tz, center.y - tz / 2, tz, tz);
                gr.fill(RC);
            } else {
                LC.setRect(0, 0, 0, 0);
                RC.setRect(0, 0, 0, 0);
            }
        }

        public ResizeControlPointResolver getControlPointResolver() {
            return new ResizeControlPointResolver() {
                @Override
                public ResizeProvider.ControlPoint resolveControlPoint(Widget widget, Point point) {
                    if (TL.contains(point)) {
                        return ResizeProvider.ControlPoint.TOP_LEFT;
                    }
                    if (TC.contains(point)) {
                        return ResizeProvider.ControlPoint.TOP_CENTER;
                    }
                    if (TR.contains(point)) {
                        return ResizeProvider.ControlPoint.TOP_RIGHT;
                    }
                    if (LC.contains(point)) {
                        return ResizeProvider.ControlPoint.CENTER_LEFT;
                    }
                    if (RC.contains(point)) {
                        return ResizeProvider.ControlPoint.CENTER_RIGHT;
                    }
                    if (BL.contains(point)) {
                        return ResizeProvider.ControlPoint.BOTTOM_LEFT;
                    }
                    if (BC.contains(point)) {
                        return ResizeProvider.ControlPoint.BOTTOM_CENTER;
                    }
                    if (BR.contains(point)) {
                        return ResizeProvider.ControlPoint.BOTTOM_RIGHT;
                    }
                    // TODO - resolve CENTER points
                    return null;
                }

            };

        }
    }

    private class WrapperBorder implements Border {

        protected Stroke OUTLINE = new BasicStroke(0.1f, BasicStroke.JOIN_BEVEL, BasicStroke.CAP_BUTT, 5.0f, new float[]{6.0f, 3.0f}, 0.0f);
        protected final Color color = Color.black;

        protected final Insets INNER_INSETS = new Insets(0, 0, 0, 0);

        /**
         * Inset ini harus integer. Artinya ga bisa koma. Jadi biar aman kita
         * bikin square nya di dalam objek aja, jadi insetnya 0,0,0,0.
         *
         * @return
         */
        @Override
        public Insets getInsets() {
            return INNER_INSETS;
        }

        @Override
        public void paint(Graphics2D gr, Rectangle bounds) {
            gr.setColor(color);
            Stroke stroke = gr.getStroke();
            gr.setStroke(OUTLINE);
            gr.draw(bounds);
            gr.setStroke(stroke);
        }

        @Override
        public boolean isOpaque() {
            return false;
        }
    }

    private class Listener implements Scene.SceneListener, ObjectSceneListener {

        @Override
        public void objectAdded(ObjectSceneEvent event, Object addedObject) {
        }

        @Override
        public void objectRemoved(ObjectSceneEvent event, Object removedObject) {
        }

        @Override
        public void objectStateChanged(ObjectSceneEvent event, Object changedObject, ObjectState previousState, ObjectState newState) {
        }

        @Override
        public void selectionChanged(ObjectSceneEvent event, Set<Object> previousSelection, Set<Object> newSelection) {
            //we only put our interest here
            if (newSelection.isEmpty()) {
                setBorder(BorderFactory.createEmptyBorder());
            } else {
                if (scene.getActiveTool().equals(TRANSFORM_TOOL)) {
                    setBorder(TRANSFORM_BORDER);
                } else if (scene.getActiveTool().equals(LayoutScene.FREE_MOVE_TOOL)) {
                    setBorder(WRAPPER_BORDER);
                }
            }
            revalidate();//triggers recalculate area
            scene.validate();
        }

        @Override
        public void highlightingChanged(ObjectSceneEvent event, Set<Object> previousHighlighting, Set<Object> newHighlighting) {
        }

        @Override
        public void hoverChanged(ObjectSceneEvent event, Object previousHoveredObject, Object newHoveredObject) {
        }

        @Override
        public void focusChanged(ObjectSceneEvent event, Object previousFocusedObject, Object newFocusedObject) {
        }

        @Override
        public void sceneRepaint() {
        }

        @Override
        public void sceneValidating() {
            revalidate();
        }

        @Override
        public void sceneValidated() {
        }
    }
}
