/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.component;

import com.jbatik.core.api.GlobalUndoManager;
import com.jbatik.core.api.PNGExportConfiguration;
import com.jbatik.core.project.JBatikProject;
import com.jbatik.filetype.lay.LayNavigatorLookupHint;
import com.jbatik.filetype.lay.LayoutDataObject;
import com.jbatik.modules.layout.LayoutDocument;
import com.jbatik.canvas.util.Scene2ImageExporter;
import com.jbatik.canvas.util.TilingScene2Image;
import com.jbatik.core.api.CentralLookup;
import com.jbatik.modules.layout.explorer.LayerController;
import com.jbatik.modules.layout.io.SceneSavable;
import com.jbatik.modules.layout.library.ExportableToLibCookie;
import com.jbatik.modules.layout.library.palette.LibraryPalette;
import com.jbatik.modules.layout.visual.LayoutScene;
import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.export.SceneExporter;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.spi.navigator.NavigatorLookupPanelsPolicy;
import org.openide.awt.UndoRedo;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author Dimas Y. Danurwenda <dimas@danurwenda.com>
 */
@MultiViewElement.Registration(
        displayName = "#LBL_Lay_VISUAL",
        iconBase = "com/jbatik/filetype/lay/lay-icon.png",
        mimeType = "text/lay+xml",
        persistenceType = TopComponent.PERSISTENCE_NEVER,
        preferredID = "Lay",
        position = 100
)
@NbBundle.Messages(
        {
            "LBL_Lay_VISUAL=Visual",})
public class LayoutViewElement extends javax.swing.JPanel implements
        MultiViewElement, ExportableToLibCookie {

    private JToolBar toolbar;
    private Lookup lookup;
    private UndoRedo.Manager undoredo = new UndoRedo.Manager();
    private InstanceContent ic = new InstanceContent();
    private transient MultiViewElementCallback callback;
    private LayoutDataObject obj;
    private LayoutScene scene;

    public LayoutViewElement(final Lookup lookupFromDataObject) {
        obj = lookupFromDataObject.lookup(LayoutDataObject.class);
        setLayout(new BorderLayout());

        scene = new LayoutScene(this, obj);
        layoutToolbar = new LayoutToolbar(scene);
        final JComponent sceneView = scene.getView();

        //masukin elemen yang berupa JScrollPane berisi scene.getView
        JScrollPane decorated = new JScrollPane(sceneView, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        final LayoutSceneRuler horizontal = new LayoutSceneRuler(scene, LayoutSceneRuler.HORIZONTAL);
        final LayoutSceneRuler vertical = new LayoutSceneRuler(scene, LayoutSceneRuler.VERTICAL);
        UnitMenus unitPopup = new UnitMenus();
        horizontal.addMouseListener(unitPopup);
        vertical.addMouseListener(unitPopup);
        unitPopup.addActionListener(horizontal);
        unitPopup.addActionListener(vertical);
        JLabel unitLabel = new JLabel();
        unitLabel.setHorizontalAlignment(SwingConstants.CENTER);
        LayoutDocument ld = scene.getlSystem().getDocument();
        ld.addPropertyChangeListener((PropertyChangeEvent evt) -> {
            if (evt.getPropertyName().equals(LayoutDocument.UNIT_PROP)) {
                unitPopup.setSelectedUnit((Unit<Length>) evt.getNewValue());
            }
        });
        unitPopup.addActionListener((ActionEvent e) -> {
            String t = "xx";
            JMenuItem selected = (JMenuItem) e.getSource();
            switch (selected.getText()) {
                case "Inches":
                    t = "in";
                    break;
                case "Pixels":
                    t = "px";
                    break;
                case "Millimeters":
                    t = "mm";
                    break;
                case "Centimeters":
                    t = "cm";
                    break;
            }
            unitLabel.setText(t);
        });
        unitPopup.setSelectedUnit(ld.getUnit());
        scene.getPriorActions().addAction(new WidgetAction.Adapter() {
            @Override
            public WidgetAction.State mouseWheelMoved(Widget widget, WidgetAction.WidgetMouseWheelEvent event) {
                horizontal.repaint();
                vertical.repaint();
                return WidgetAction.State.REJECTED;
            }
        });
        scene.addSceneListener(new Scene.SceneListener() {

            @Override
            public void sceneRepaint() {
            }

            @Override
            public void sceneValidating() {
            }

            @Override
            public void sceneValidated() {
                horizontal.repaint();
                vertical.repaint();
            }
        });
        decorated.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, unitLabel);
        decorated.setColumnHeaderView(horizontal);
        decorated.setRowHeaderView(vertical);
        add(decorated, BorderLayout.CENTER);
        decorated.addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent ce) {
                //TODO : preserve center of the screen while maintaining gap
                //jadi setiap kali terjadi zoom dari aksi user,
                //baik melalui dropdown ataupun scroll mouse,
                //disimpan ratio gap nya
                //componentResized ini menjamin bahwa ratio gap yang baru
                //TIDAK KURANG dari ratio gap yang lama
                scene.toPage();
            }

            @Override
            public void componentMoved(ComponentEvent ce) {
            }

            @Override
            public void componentShown(ComponentEvent ce) {
            }

            @Override
            public void componentHidden(ComponentEvent ce) {
            }
        });

//        initToolbar();
        lookup = new ProxyLookup(
                scene.getLookup(),// dataobject's lookup is in scene's
                new AbstractLookup(ic));// ic can be expanded later
        ic.add(this);//expose exportablelibcookie
        ic.add((NavigatorLookupPanelsPolicy) () -> NavigatorLookupPanelsPolicy.LOOKUP_HINTS_ONLY);
        ic.add(new LayNavigatorLookupHint());

        // FIXME? : using annotation or whatever so that we can define and register
        // the palette from different module
        ic.add(LibraryPalette.createPalette(
                FileOwnerQuery.getOwner(obj.getPrimaryFile())
                .getLookup().lookup(JBatikProject.class)));

    }

    @Override
    public JComponent getVisualRepresentation() {
        return this;
    }

    private LayoutToolbar layoutToolbar;

    @Override
    public JComponent getToolbarRepresentation() {
        if (toolbar == null) {
            toolbar = layoutToolbar.getToolbar();
        }
        return toolbar;
    }

    @Override
    public Action[] getActions() {
        return new Action[0];
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    @Override
    public void componentOpened() {
        scene.toPage();// default zoom strategy
//        System.err.println("opened");
//        ToolbarUtil.setToolbarVisible(ToolbarPool.getDefault().findToolbar("MyToolbar"), true);
    }

    @Override
    public void componentClosed() {
        lookup = Lookup.EMPTY;
//        ToolbarUtil.setToolbarVisible(ToolbarPool.getDefault().findToolbar("MyToolbar"), false);
        GlobalUndoManager.setManager(null);
    }

    @Override
    public void componentShowing() {
        obj.getLookup().lookup(InstanceContent.class).add(scene.getLayerController());
        updateName();
    }

    @Override
    public void componentHidden() {
        obj.getLookup().lookup(InstanceContent.class).remove(scene.getLayerController());
    }

    @Override
    public void componentActivated() {
        scene.getView().requestFocusInWindow();
        //cek centrallookup ada layercontroller apa engga
        LayerController con = CentralLookup.getDefault().lookup(LayerController.class);
        LayerController conOnScene = scene.getLayerController();
        if (con != conOnScene) {
            //inject
            if (con != null) {
                CentralLookup.getDefault().remove(con);
            }
            CentralLookup.getDefault().add(conOnScene);
        }
        GlobalUndoManager.setManager(undoredo);
    }

    @Override
    public void componentDeactivated() {
    }

    @Override
    public UndoRedo getUndoRedo() {
        return undoredo;
    }

    @Override
    public void setMultiViewCallback(MultiViewElementCallback callback) {
        this.callback = callback;
    }

    public void updateName() {
        if (callback != null) {
            TopComponent tc = callback.getTopComponent();
            String nameExt = obj.getPrimaryFile().getNameExt(); //filename
            tc.setDisplayName(nameExt);
            tc.setHtmlDisplayName(nameExt);
            tc.setToolTipText(obj.getPrimaryFile().getPath());
        }
    }

    public void requestActive() {
        if (callback != null) {
            TopComponent tc = callback.getTopComponent();
            tc.requestActive();
        }
    }

    @NbBundle.Messages({
        "# {0} - file name",
        "MSG_SaveModified=File {0} is modified. Save?"
    })
    @Override
    public CloseOperationState canCloseElement() {
        //ini dioverride biar kalo ada outstanding savable, minta popup confirmation dulu
        SceneSavable sav = obj.getLookup().lookup(SceneSavable.class);
        if (sav != null) {
            AbstractAction save = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        sav.save();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            };
            AbstractAction discardSave = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    sav.discard();
                }
            };
            save.putValue(Action.LONG_DESCRIPTION, Bundle.MSG_SaveModified(obj.getPrimaryFile().getNameExt()));
            return MultiViewFactory.createUnsafeCloseState("editor", save, discardSave);
        }
        return CloseOperationState.STATE_OK;
    }

    @Override
    public String getDefaultName() {
        return obj.getName();
    }

    @Override
    public JBatikProject getProject() {
        Project p = FileOwnerQuery.getOwner(obj.getPrimaryFile());
        if (p instanceof JBatikProject) {
            return (JBatikProject) p;
        } else {
            return null;
        }
    }

    /**
     * Exporting the whole paper on the Scene into a PNG file.
     *
     * @param path
     * @param config
     */
    @Override
    public void writeImage(String path, PNGExportConfiguration config) {
        writeImagePNGJ(path, config);
    }

    private void writeImageS2I(String path, PNGExportConfiguration config) {
        try {
            scene.showRemarks(false);
            Scene2ImageExporter.createImage(
                    scene, new File(path), SceneExporter.ImageType.PNG, SceneExporter.ZoomType.ACTUAL_SIZE, false, false, false, true, scene.getPaperBounds(), 0, 0, 0);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            scene.showRemarks(true);
        }
    }

    private void writeImagePNGJ(String path, PNGExportConfiguration config) {
        TilingScene2Image tiler = new TilingScene2Image(new File(path), scene.getOffscreenScene());
        tiler.setDPI(scene.getlSystem().getDocument().getDPI());
        tiler.createImage(scene.getPaperBounds());
    }

    @Override
    public Image getPreviewImage(int w, int h, boolean withBackground) {
        try {
            return Scene2ImageExporter.createImage(scene, null, SceneExporter.ImageType.PNG, SceneExporter.ZoomType.ACTUAL_SIZE, true, false, false, w, h, 0);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    /**
     * Exporting <em>selected</em> layer to lib. Always uses
     * ZoomType.ACTUAL_SIZE
     *
     * @param path
     */
    @Override
    public void writeLib(String path) {
        //hide workspace
        scene.setWorkplaceVisible(false);
        try {
            Scene2ImageExporter.createImage(scene, new File(path), SceneExporter.ImageType.PNG, SceneExporter.ZoomType.ACTUAL_SIZE, false, true, true, 0, 0, 0);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            scene.setWorkplaceVisible(true);
        }
    }

}
