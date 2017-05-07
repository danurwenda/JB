/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.visual;

import com.jbatik.modules.layout.visual.actions.SelectedLayerRotateProvider;
import com.jbatik.canvas.actions.StrategicZoom;
import com.jbatik.canvas.visual.CalibratedScene;
import com.jbatik.canvas.visual.CanvasWidget;
import com.jbatik.canvas.visual.DesktopLayer;
import com.jbatik.canvas.visual.DesktopScene;
import com.jbatik.core.api.DocumentPaper;
import com.jbatik.core.api.GlobalUndoManager;
import com.jbatik.core.api.SceneObserverCookie;
import com.jbatik.filetype.lay.LayoutDataObject;
import com.jbatik.modules.layout.LayoutDocument;
import com.jbatik.modules.layout.LayoutLSystem;
import com.jbatik.modules.layout.component.LayoutViewElement;
import com.jbatik.modules.layout.explorer.LayerController;
import com.jbatik.modules.layout.explorer.LayerFactory;
import com.jbatik.modules.layout.io.LayoutParser;
import com.jbatik.modules.layout.io.SceneSavable;
import com.jbatik.modules.layout.layering.SubLayoutLayer;
import com.jbatik.canvas.visual.actions.RotateMouseAction;
import com.jbatik.core.api.ProjectPathDependant;
import com.jbatik.modules.layout.layering.LayoutLayer;
import com.jbatik.modules.layout.visual.actions.AssignLibActionOnSLW;
import com.jbatik.modules.layout.visual.actions.MirrorSelectedLayerAction;
import com.jbatik.modules.layout.visual.actions.MoveSelectedLayerAction;
import com.jbatik.modules.layout.visual.actions.ScrollAction;
import com.jbatik.modules.layout.visual.widgets.SubLayoutWidget;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.measure.quantity.Length;
import javax.swing.ActionMap;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.jscience.physics.amount.Amount;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;

/**
 * Lumayan
 *
 * @author RAPID02
 */
public class LayoutScene extends DesktopScene
        implements SceneObserverCookie, CalibratedScene, ProjectPathDependant {

    public static final String TO_PAGE = "toPage";
    public static final String TO_HEIGHT = "toHeight";
    public static final String TO_FIT = "toFit";
    public static final String TO_WIDTH = "toWidth";

    private LayoutViewElement multiview;
    private LayerController layerController;

    public LayerController getLayerController() {
        return layerController;
    }

    public LayoutScene(LayoutViewElement mve, LayoutDataObject obj) {
        super();

        this.dataObject = obj;

        multiview = mve;
        this.projectPath = FileOwnerQuery.getOwner(obj.getPrimaryFile()).getProjectDirectory().getPath();
        this.lSystem = LayoutParser.parse(obj);
//        addSaveAsCapable();

        LayoutDocument ld = lSystem.getDocument();
        ld.addPropertyChangeListener(new DocumentDPIChangeListener());

        //paper layer
        this.paperWidget = new PaperWidget(this, ld);
        //add paper biar bisa diedit2
        ic.add(ld);
        paperLayer.addChild(paperWidget);

        layerController = LayerFactory.createLayer(this, lSystem.getLayers());
        //init em listener
        ExplorerManager em = layerController.getExplorerManager();
        em.addPropertyChangeListener(new NodeSelectionListener());
        //main layer, where sublayout widgets will be added
        mainLayer = layerController.getRootWidget();
        addChild(mainLayer);

        //selection layer, directly above the main layer
        selectionLayer = new LayerWidget(this);
        selectionWrapperWidget = new SelectionWrapperWidget(this);
        selectionLayer.addChild(selectionWrapperWidget);
        addChild(selectionLayer);

        getPriorActions().addAction(new KeySwitchToolAction());

        //scene's actions, applied to all selected widgets
        //key move tool, always available
        getActions().addAction(KEY_MOVE_ACTION);
        //move tool
        createActions(FREE_MOVE_TOOL).addAction(MOVE_ACTION);
        createActions(ON_AXIS_MOVE_TOOL).addAction(ON_AXIS_MOVE_ACTION);
        setActiveTool(FREE_MOVE_TOOL);
        //rotate on outside of selectedwrapper border
        createActions(SelectionWrapperWidget.TRANSFORM_TOOL).addAction(ROTATE_ACTION);
        createActions(SelectionWrapperWidget.SHIFTED_TRANSFORM_TOOL).addAction(DISCREET_ROTATE_ACTION);
        //mirror selected layer, only on transform mode
        createActions(SelectionWrapperWidget.TRANSFORM_TOOL).addAction(MIRROR_ACTION);
        //show popup under right click to show layers under cursor
        getActions().addAction(SELECT_ACTION);
        getPriorActions().addAction(SCROLL_MODIFIER_ACTION);
        initLookup();

        createView();
    }
    //============================LAYERS================================

    /**
     * a layer that represents the desk surface where the paper is laid. This
     * layer is required when displaying paperWidget in a specific ZoomLevel.
     * For example, when using "To Height" ZoomLevel, the vertical scrollbar is
     * placed in the middle instead of not shown at all. This layer contains
     * "dummy widget"s that represent the corners of the desktop.
     */
    private DesktopLayer desktopLayer;

    /**
     * The working paper
     */
    private PaperWidget paperWidget;

    /**
     * Main Layer where SubLayout is laid down.
     */
    private LayerWidget mainLayer;

    /**
     * Selection layer to visualize the rectangle that contains all selected
     * widget
     */
    private LayerWidget selectionLayer;
    private SelectionWrapperWidget selectionWrapperWidget;

    //===========================DATA STRUCTURE============================
    /**
     * Active, editable LayoutLSystem that can then be written to file on saving
     */
    private final LayoutLSystem lSystem;

    public LayoutLSystem getlSystem() {
        return lSystem;
    }

    /**
     * Data Object that is associated with the file from which ls is loaded.
     */
    private LayoutDataObject dataObject;

    /**
     * Path to project. Should be finalized on construction.
     */
    private final String projectPath;

    @Override
    public String getProjectPath() {
        return projectPath;
    }

    //============================LOOKUP===================================
    private FormProxyLookup lookup;
    /**
     * A content that contains all selected SubLayouts
     */
    private InstanceContent icForSelectedSubLayoutOnCanvas = new InstanceContent();

    private void initLookup() {
        Lookup explorerLookup; // lookup for EpxlorerManager
        Lookup plainContentLookup; //general util using ic
        Lookup dataObjectLookup; // to make sure DO is in lookup WHEN no node selected
        if (lookup == null) {
            lookup = new FormProxyLookup();

            explorerLookup = null;

            //general utility content
            //add itself (for satellite view)
            ic.add(this);
            //add calibrated zoom
            ic.add(new FixCenterCalibratedZoom(0.1));
            ic.add(new FixCenterCalibratedZoom(0.25));
            ic.add(new FixCenterCalibratedZoom(0.5));
            ic.add(new FixCenterCalibratedZoom(0.75));
            ic.add(new FixCenterCalibratedZoom(1));
            ic.add(new FixCenterCalibratedZoom(2));
            ic.add(new FixCenterCalibratedZoom(4));
            ic.add(new FixCenterCalibratedZoom(8));
            ic.add(new FixCenterCalibratedZoom(16));
            plainContentLookup = new AbstractLookup(ic);

            dataObjectLookup = null;
        } else {
            Lookup[] lookups = lookup.getSubLookups();
            explorerLookup = lookups[0];
            plainContentLookup = lookups[1];
            dataObjectLookup = lookups[2];
        }
        if (dataObjectLookup == null) {
            dataObjectLookup = dataObject.getNodeDelegate().getLookup();
        }
        if (explorerLookup == null) {
            ActionMap map = new ActionMap();
            explorerLookup = ExplorerUtils.createLookup(
                    layerController.getExplorerManager(), map);
        }
        lookup.setSubLookups(new Lookup[]{
            explorerLookup, plainContentLookup, dataObjectLookup
        });
    }

    public Scene getOffscreenScene() {
        ImmutableLayoutScene offscreen = new ImmutableLayoutScene(dataObject, lSystem);
        return offscreen;
    }

    private static class FormProxyLookup extends ProxyLookup {

        FormProxyLookup() {
            super();
        }

        Lookup[] getSubLookups() {
            return getLookups();
        }

        void setSubLookups(Lookup[] lookups) {
            setLookups(lookups);
        }

    }
    private boolean settingLookup;

    private void switchNodeInLookup(boolean includeDataNodeLookup) {
        if (settingLookup) {
            return;
        }
        Lookup[] lookups = lookup.getSubLookups();
        int index = lookups.length - 1;
        boolean dataNodeLookup = (lookups[index] != Lookup.EMPTY);
        if (includeDataNodeLookup != dataNodeLookup) {
            lookups[index] = includeDataNodeLookup
                    ? dataObject.getNodeDelegate().getLookup()
                    : Lookup.EMPTY;
            try {
                settingLookup = true; // avoid re-entrant call
                lookup.setSubLookups(lookups);
            } finally {
                settingLookup = false;
            }
        }
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    //=========================VIEWING ACTIONS================================
    /**
     * Single selection, using right button click on scene. It will show popup
     * containing layers under mouse cursor.
     */
    private final WidgetAction SELECT_ACTION = ActionFactory.createPopupMenuAction(
            new PopupMenuProvider() {
                public JPopupMenu getPopupMenu(Widget widget, Point p) {
                    //tes dulu
                    //cari ada berapa slw contains p
                    List<SubLayoutWidget> hit = new ArrayList<>();
                    for (Object o : getObjects()) {
                        Widget w = findWidget(o);
                        if (w instanceof SubLayoutWidget && w.isHitAt(w.convertSceneToLocal(p))) {
                            SubLayoutWidget slw = (SubLayoutWidget) w;
                            hit.add(slw);
                        }
                    }
                    JPopupMenu popup = new JPopupMenu();
                    if (!hit.isEmpty()) {
                        //sort slw
                        Collections.sort(hit, new Comparator<SubLayoutWidget>() {
                            List<Widget> getParentWidgets(Widget w) {
                                ArrayList<Widget> ret = new ArrayList<>();
                                Widget wi = w;
                                ret.add(w);//add this
                                while (wi.getParentWidget() != mainLayer) {
                                    ret.add(wi);
                                    wi = wi.getParentWidget();
                                }
                                //reverse so that the oldest ancestor is placed first
                                Collections.reverse(ret);
                                return ret;
                            }

                            @Override
                            public int compare(SubLayoutWidget o1, SubLayoutWidget o2) {
                                List<Widget> l1 = getParentWidgets(o1);
                                List<Widget> l2 = getParentWidgets(o2);
                                int lvl = 0;
                                Widget w1 = l1.get(lvl);
                                Widget w2 = l2.get(lvl);
                                while (w1 == w2) {
                                    lvl++;
                                    w1 = l1.get(lvl);
                                    w2 = l2.get(lvl);
                                }
                                //get the first branch lineage
                                //compare them
                                Widget p = w1.getParentWidget();
                                int r = 0;
                                for (Widget c : p.getChildren()) {
                                    if (c == w1) {
                                        r = 1;
                                        break;
                                    } else if (c == w2) {
                                        r = -1;
                                        break;
                                    }
                                }
                                return r;
                            }
                        });
                        for (SubLayoutWidget slw : hit) {
                            popup.add(new SLWMenuItem(slw));
                        }
                    }
                    return popup;
                }
            });
//
//    public final void suggestSelectedLayer(SubLayoutLayer s) {
//        suggestSelectedLayer(s, false);
//    }
//
//    public final void suggestSelectedLayer(SubLayoutLayer s, boolean invert) {
//        if (s == null) {
//            if (!invert) {
//                icForSelectedSubLayoutOnCanvas.set(Collections.EMPTY_LIST, null);
//            }
//        } else {
//            HashSet<Object> objects = new HashSet<>(getSelectedObjects());
//            if (invert) {
//                //cek apakah sudah ada di selected
//                if (objects.contains(s)) {
//                    System.err.println("invert, sudah ada");
//                    objects.remove(s);
//                    icForSelectedSubLayoutOnCanvas.remove(s);
//                } else {
//                    System.err.println("invert, belum ada");
//                    objects.add(s);
//                    icForSelectedSubLayoutOnCanvas.add(s);
//                }
//                if (objects.isEmpty()) {
//                    System.err.println("invert, jadi kosong");
//                    //kasih default ke ic
//                    icForSelectedSubLayoutOnCanvas.set(Collections.singleton(SubLayout.getDefault()), null);
//                }
//            } else {
//                //cek apakah sudah ada di selected
//                if (!objects.contains(s)) {
//                    System.err.println("bukan invert, belum ada");
//                    icForSelectedSubLayoutOnCanvas.set(Collections.singleton(s), null);
//                }
//            }
//        }
//    }
    //======================LIBRARY ACTIONS================================
    /**
     * Assigning image from library palette with drag n drop gesture
     */
    private final WidgetAction LIB_ACTION = ActionFactory.createAcceptAction(new AssignLibActionOnSLW(this));

    public WidgetAction getLibAction() {
        return LIB_ACTION;
    }

    //======================STRUCTURAL ACTIONS================================
    public static final String FREE_MOVE_TOOL = "freemove";
    private final String ON_AXIS_MOVE_TOOL = "onaxismove";

    /**
     * Move action, to move all selected SubLayoutWidgets. Using CTRL modifier,
     * the displacement will be limited on one axis (x/y).
     */
    private final WidgetAction MOVE_ACTION = new MoveSelectedLayerAction(this, false);
    private final WidgetAction KEY_MOVE_ACTION = new KeyMoveAction(this);
    private final WidgetAction ON_AXIS_MOVE_ACTION = new MoveSelectedLayerAction(this, true);

    /**
     * Mirror action. All selected layer will be mirrored such that these layer
     * will be the reflection of the original layer. The reflection will be done
     * against a vertical line that vertically bisects the bounding box of all
     * selected layer.
     */
    private final WidgetAction MIRROR_ACTION = new MirrorSelectedLayerAction(this);
    /**
     * Rotate action. Rotates all selected SubLayoutWidgets. The rotation center
     * is located at the center of the rectangle enclosing the selected widgets.
     * Using modifier, the rotation will be done in 15-degrees increment.
     */
    private final WidgetAction ROTATE_ACTION = new RotateMouseAction(new SelectedLayerRotateProvider(this), false);
    private final WidgetAction DISCREET_ROTATE_ACTION = new RotateMouseAction(new SelectedLayerRotateProvider(this), true);

    /**
     * Just like 3D, scrolling mouse wheel while holding modifier key (A, W, E)
     * will change the angle, width and length of selected layer, respectively.
     */
    private final WidgetAction SCROLL_MODIFIER_ACTION = new ScrollAction(this);

    @Override
    public void changeBackgroundColor(Color newColor) {
        LayoutDocument doc = lSystem.getDocument();
        Color old = lSystem.getDocument().getBackground();
        ChangeBackgroundUndoableEdit edit = new ChangeBackgroundUndoableEdit(doc, old, newColor);
        GlobalUndoManager.getManager().undoableEditHappened(new UndoableEditEvent(doc, edit));
        lSystem.getDocument().setBackground(newColor);
        addSavable();
    }

    @Override
    public Color getBackgroundColor() {
        return lSystem.getDocument().getBackground();
    }

    @Override
    protected Rectangle getWorkingRectangle(boolean withPaper) {
        Rectangle working = null;
        if (withPaper) {
            working = paperWidget.getPreferredBounds();
        }
        for (Object o : getObjects()) {
            Widget w = findWidget(o);
            if (w instanceof SubLayoutWidget) {
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
        return working;
    }

    public void addSavable() {
        if (!dataObject.isModified()) {
            dataObject.getLookup().lookup(InstanceContent.class).add(new SceneSavable(lSystem, dataObject));
//            ic.add(saverr);
            dataObject.setModified(true);
        }
    }

    public void requestActive() {
        multiview.requestActive();
        getView().requestFocusInWindow();
    }

    public void updateSelectionWrapper() {
        selectionWrapperWidget.revalidate();
        validate();
    }

    public void setAutoSelect(boolean autoSelect) {
        System.err.println("autoselect: " + autoSelect);
    }

    public void setTransformMode() {
        setActiveTool(SelectionWrapperWidget.TRANSFORM_TOOL);
        selectionWrapperWidget.activateTransform();
        requestActive();
    }

    public void setMoveMode() {

        setActiveTool(LayoutScene.FREE_MOVE_TOOL);
        selectionWrapperWidget.deactivateTransform();
        requestActive();
    }

    public void showRemarks(boolean show) {
        selectionWrapperWidget.setVisible(show);
        paperWidget.drawShadow = show;
        paperWidget.revalidate(true);
        for (Object o : getObjects()) {
            Widget w = findWidget(o);
            if (w instanceof SubLayoutWidget) {
                SubLayoutWidget slw = (SubLayoutWidget) w;
                slw.showEmptySquares(show);
            }
        }
    }

    /**
     * When set to false, will hide all except those widgets representing
     * layers.
     */
    @Override
    public void setWorkplaceVisible(boolean show) {
        paperWidget.setVisible(show);
        selectionWrapperWidget.setVisible(show);
        for (Object o : getObjects()) {
            Widget w = findWidget(o);
            if (w instanceof SubLayoutWidget) {
                SubLayoutWidget slw = (SubLayoutWidget) w;
                slw.showEmptySquares(show);
            }
        }
        validate();
    }

    /**
     * Zoom factor that considering current DPI. For example on 96 dpi screen, a
     * 4x4 inch layout in 96 dpi displayed in 100% (actual) zoom will be as
     * large as 4x4 inch layout in 192 dpi in 50% (actual zoom). From user POV,
     * both layout should be displayed in 100%, which is 4x4 inch.
     *
     * @return
     */
    @Override
    public double getCalibratedZoomFactor() {
        double actualZoom = getZoomFactor();
        int screenDPI = Toolkit.getDefaultToolkit().getScreenResolution();
        int layoutDPI = getlSystem().getDocument().getDPI();
        return actualZoom * layoutDPI / screenDPI;
    }

    @Override
    public void setCalibratedZoomFactor(double d) {
        int screenDPI = Toolkit.getDefaultToolkit().getScreenResolution();
        int layoutDPI = getlSystem().getDocument().getDPI();
        setCenteredZoomFactor(d * screenDPI / layoutDPI);
    }

    @Override
    protected CanvasWidget getCanvasWidget() {
        return paperWidget;
    }

    /**
     * Return the rectangle currently occupied by selectionWrapperWidget
     *
     * @return
     */
    public Rectangle getSelectedRectangle() {
        return selectionWrapperWidget.getPreferredBounds();
    }

    @NbBundle.Messages({
        "EXC_NavCannotBeNull=Please open Navigator panel before selecting layer."
    })
    private class SLWMenuItem extends JMenuItem {

        public SLWMenuItem(SubLayoutWidget slw) {
            super(slw.getLayername());
            addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    LayoutLayer selected = (LayoutLayer) findObject(slw);
                    //check whether navigator has already been opened
                    Node n = selected.getNodeDelegate();
                    if (n == null) {
                        DialogDisplayer.getDefault().notify(
                                new NotifyDescriptor.Message(Bundle.EXC_NavCannotBeNull(), NotifyDescriptor.INFORMATION_MESSAGE)
                        );
                    } else {
                        try {
                            layerController.getExplorerManager().setSelectedNodes(new Node[]{n});
                        } catch (PropertyVetoException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            });
        }

    }

    /////////////////////////////////LOOKUP/////////////////////////////////////
    private class FixCenterCalibratedZoom extends StrategicZoom {

        private final double zoomMultiplier;

        public FixCenterCalibratedZoom(double z) {
            zoomMultiplier = z;
        }

        @Override
        public String getDisplayName() {
            return String.valueOf((int) Math.floor(zoomMultiplier * 100)) + "%";
        }

        @Override
        public void zoom() {
            setCalibratedZoomFactor(zoomMultiplier);
        }
    }

    private class ChangeBackgroundUndoableEdit extends AbstractUndoableEdit {

        LayoutDocument d;
        Color o, n;

        public ChangeBackgroundUndoableEdit(LayoutDocument d, Color o, Color n) {
            this.d = d;
            this.n = n;
            this.o = o;
        }

        @Override
        public String getPresentationName() {
            return "Change Layout Background";
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            d.setBackground(n);
            addSavable();
            validate();
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            d.setBackground(o);
            addSavable();
            validate();
        }
    }

    private static final class KeyMoveAction extends WidgetAction.Adapter {

        private LayoutScene scene;

        private KeyMoveAction(LayoutScene aThis) {
            this.scene = aThis;
        }

        /**
         * TODO : Make it undoable?
         *
         * @param widget
         * @param event
         * @return
         */
        @Override
        public WidgetAction.State keyPressed(Widget widget, WidgetAction.WidgetKeyEvent event) {
            if (event.getKeyCode() == KeyEvent.VK_UP) {
                for (Object o : scene.getSelectedObjects()) {
                    if (o instanceof SubLayoutLayer) {
                        SubLayoutLayer sll = (SubLayoutLayer) o;
                        sll.setY(sll.getY() - 1);
                    }
                }
                return WidgetAction.State.CONSUMED;//set it as consumed, so it won't bubble to trigger scrolling
            } else if (event.getKeyCode() == KeyEvent.VK_DOWN) {
                for (Object o : scene.getSelectedObjects()) {
                    if (o instanceof SubLayoutLayer) {
                        SubLayoutLayer sll = (SubLayoutLayer) o;
                        sll.setY(sll.getY() + 1);
                    }
                }
                return WidgetAction.State.CONSUMED;//set it as consumed, so it won't bubble to trigger scrolling
            } else if (event.getKeyCode() == KeyEvent.VK_RIGHT) {
                for (Object o : scene.getSelectedObjects()) {
                    if (o instanceof SubLayoutLayer) {
                        SubLayoutLayer sll = (SubLayoutLayer) o;
                        sll.setX(sll.getX() + 1);
                    }
                }
                return WidgetAction.State.CONSUMED;//set it as consumed, so it won't bubble to trigger scrolling
            } else if (event.getKeyCode() == KeyEvent.VK_LEFT) {
                for (Object o : scene.getSelectedObjects()) {
                    if (o instanceof SubLayoutLayer) {
                        SubLayoutLayer sll = (SubLayoutLayer) o;
                        sll.setX(sll.getX() - 1);
                    }
                }
                return WidgetAction.State.CONSUMED;//set it as consumed, so it won't bubble to trigger scrolling
            }
            return super.keyPressed(widget, event);
        }
    }

    /**
     * On Test. Conflicting with Shift/Ctrl gesture on selecting multiple layer
     * on navigator panel.
     */
    private final class KeySwitchToolAction extends WidgetAction.Adapter {

        @Override
        public WidgetAction.State keyPressed(Widget widget, WidgetAction.WidgetKeyEvent event) {
            if (event.getKeyCode() == KeyEvent.VK_SHIFT) {
                switch (getActiveTool()) {
                    case FREE_MOVE_TOOL:
                        setActiveTool(ON_AXIS_MOVE_TOOL);
                        break;
                    case SelectionWrapperWidget.TRANSFORM_TOOL:
                        setActiveTool(SelectionWrapperWidget.SHIFTED_TRANSFORM_TOOL);
                        break;
                }
            } else if (event.getKeyCode() == KeyEvent.VK_CONTROL) {
                if (getActiveTool().equals(SelectionWrapperWidget.TRANSFORM_TOOL)) {
                    setActiveTool(SelectionWrapperWidget.RESIZE_FROM_CENTER_TOOL);
                }
            }
            return WidgetAction.State.REJECTED;
        }

        @Override
        public WidgetAction.State keyReleased(Widget widget, WidgetAction.WidgetKeyEvent event) {
            if (event.getKeyCode() == KeyEvent.VK_SHIFT) {
                switch (getActiveTool()) {
                    case ON_AXIS_MOVE_TOOL:
                        setActiveTool(FREE_MOVE_TOOL);
                        break;
                    case SelectionWrapperWidget.SHIFTED_TRANSFORM_TOOL:
                        setActiveTool(SelectionWrapperWidget.TRANSFORM_TOOL);
                        break;
                }
            } else if (event.getKeyCode() == KeyEvent.VK_CONTROL) {
                if (getActiveTool().equals(SelectionWrapperWidget.RESIZE_FROM_CENTER_TOOL)) {
                    setActiveTool(SelectionWrapperWidget.TRANSFORM_TOOL);
                }
            }
            return WidgetAction.State.REJECTED;
        }
    }

    private class NodeSelectionListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                Node[] selectedNodes = layerController.getExplorerManager().getSelectedNodes();
                // if no form node, select data node (of FormDataObject) in lookup
                switchNodeInLookup(selectedNodes.length == 0 && dataObject.isValid());
            }
        }

    }

    private class DocumentDPIChangeListener implements PropertyChangeListener {

        /**
         * Update location, width & height of every sublayout, based on DPI.
         *
         * @param evt
         */
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String type = evt.getPropertyName();
            switch (type) {
                case LayoutDocument.DPI_PROP:
                    int oldDPI = (int) evt.getOldValue();
                    int newDPI = (int) evt.getNewValue();
                    for (Object o : getObjects()) {
                        if (o instanceof SubLayoutLayer) {
                            SubLayoutLayer sll = (SubLayoutLayer) o;
                            sll.setLength(newDPI * sll.getLength() / oldDPI);
                            sll.setWidth(newDPI * sll.getWidth() / oldDPI);
                            Point p = sll.getLocation();
                            sll.setLocation(new Point(
                                    p.x * newDPI / oldDPI,
                                    p.y * newDPI / oldDPI
                            ));
                        }
                    }
                    break;
                case LayoutDocument.WIDTH_PROP:
                case LayoutDocument.HEIGHT_PROP:
                    Point2D newMidPoint = new Point2D.Double(paperWidget.getWidth() / 2, paperWidget.getHeight() / 2);
                    Point2D oldMidPoint = new Point2D.Double(paperWidget.getWidth() / 2, paperWidget.getHeight() / 2);
                    Amount<Length> oldValue = (Amount<Length>) evt.getOldValue();
                    //convert to pixel
                    double oldDoubleValue = DocumentPaper.getLengthInPixel(oldValue, getlSystem().getDocument().getDPI());
                    switch (evt.getPropertyName()) {
                        case LayoutDocument.HEIGHT_PROP:
                            oldMidPoint = new Point2D.Double(paperWidget.getWidth() / 2, oldDoubleValue / 2);
                            break;
                        case LayoutDocument.WIDTH_PROP:
                            oldMidPoint = new Point2D.Double(oldDoubleValue / 2, paperWidget.getHeight() / 2);
                            break;
                    }
                    int x_translate = (int) (newMidPoint.getX() - oldMidPoint.getX());
                    int y_translate = (int) (newMidPoint.getY() - oldMidPoint.getY());
                    Point oldWidgetLoc;
                    for (Object o : getObjects()) {
                        if (o instanceof SubLayoutLayer) {
                            SubLayoutLayer sll = (SubLayoutLayer) o;
                            oldWidgetLoc = sll.getLocation();
                            oldWidgetLoc.translate(x_translate, y_translate);
                            sll.setLocation(oldWidgetLoc);
                        }
                    }
                    addSavable();
            }
        }
    }
}
