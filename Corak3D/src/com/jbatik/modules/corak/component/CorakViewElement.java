/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.corak.component;

import com.jbatik.core.api.CentralLookup;
import com.jbatik.core.api.ExportAsPNG;
import com.jbatik.core.api.JBatikCanvas;
import com.jbatik.core.api.MeasurableCookie;
import com.jbatik.core.api.PNGExportConfiguration;
import com.jbatik.core.api.component.PNGExportPreviewPanel;
import com.jbatik.core.project.JBatikProject;
import com.jbatik.filetype.cor.CorNavigatorLookupHint;
import com.jbatik.filetype.cor.CorakDataObject;
import com.jbatik.lsystem.VisualLSystemRenderer;
import com.jbatik.lsystem.api.editor.InvalidableVisualLSystemModel;
import com.jbatik.lsystem.parser.exceptions.ParseRuleException;
import com.jbatik.modules.corak.CorakLSystem;
import com.jbatik.modules.corak.io.CorakSavable;
import com.jbatik.modules.corak.io.CorakSerializer;
import com.jbatik.modules.interfaces.AnimatibleCookie;
import com.jbatik.modules.interfaces.Appearance3DChangerCookie;
import com.jbatik.modules.interfaces.Archi3DObserverCookie;
import com.jbatik.modules.interfaces.ExportableToOBJCookie;
import com.jbatik.modules.interfaces.Scene3DObserverCookie;
import com.sun.j3d.utils.universe.SimpleUniverse;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collection;
import javax.media.j3d.BranchGroup;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import org.netbeans.api.actions.Savable;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.UndoRedo;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author RAPID01
 */
@MultiViewElement.Registration(
        displayName = "#LBL_Cor_VISUAL",
        iconBase = "com/jbatik/filetype/cor/cor-icon.png",
        mimeType = "text/cor+xml",
        persistenceType = TopComponent.PERSISTENCE_NEVER,
        preferredID = "Cor",
        position = 100
)
@NbBundle.Messages(
        {
            "LBL_Cor_VISUAL=Visual"
        }
)
public class CorakViewElement extends JPanel implements
        VisualLSystemRenderer,
        MultiViewElement,
        JBatikCanvas,
        ExportAsPNG,
        Scene3DObserverCookie,
        Archi3DObserverCookie,
        Appearance3DChangerCookie,
        ExportableToOBJCookie,
        MeasurableCookie,
        AnimatibleCookie {
    
    private JToolBar toolbar = new JToolBar();
    private Lookup lookup;
    private UndoRedo.Manager undoRedoManager = new UndoRedo.Manager();
    private transient MultiViewElementCallback callback;
    private CorakDataObject obj;
    private CorakLSystem cor;
    private InstanceContent ic = new InstanceContent();
    
    private KainCorak kain;
    private String projectPath;
    
    public CorakViewElement(final Lookup lookupFromDataObject) {
        obj = lookupFromDataObject.lookup(CorakDataObject.class);
        projectPath = FileOwnerQuery.getOwner(obj.getPrimaryFile()).getProjectDirectory().getPath();
        cor = CorakSerializer.deserialize(obj);
        if (cor != null) {
            init();
            
            cor.setRenderer(this);
            obj.addLookUp(cor);
        }
        ic.add(new CorNavigatorLookupHint());
        ic.add(this);
        //coraklsystem node for editing iteration, angle, length, width
        CorakLSystemPropertyChangeSupport clspcs = new CorakLSystemPropertyChangeSupport(cor);
        CorakLSystemNode node = new CorakLSystemNode(obj.getNodeDelegate(), clspcs);
        clspcs.addPropertyChangeListener(new PropertyChangeListener() {
            
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                //TODO : add undoable
                addSavable0();
                //TODO : refresh sheet
                //node.updateSheet();
            }
        });
        
        ic.add(node);
        //wrap ic into a a lookup
        AbstractLookup al = new AbstractLookup(ic);
        //exclude any node from the original lookup, so that we can ensure that
        //our CorakLSystemNode is displayed on Properties window
        Lookup clear = Lookups.exclude(
                lookupFromDataObject,
                Node.class);
        lookup = new ProxyLookup(
                clear,
                al);
        //vls model for editing axiom and rules
        vlsModel = new InvalidableVisualLSystemModel(cor, undoRedoManager) {
            @Override
            public void addSavable() {
                addSavable0();
            }
        };
    }
    
    public void addSavable0() {
        if (!obj.isModified()) {
            obj.getLookup().lookup(InstanceContent.class).add(new CorakSavable(cor, obj));
            obj.setModified(true);
        }
    }
    
    public void setCor(CorakLSystem cor) {
        this.cor = cor;
    }
    
    public void init() {
        //prepare our JPanel -> this
//        System.out.println("CorakViewElement.init");
        this.setLayout(new BorderLayout());

        //initialize our Canvas3D -> KainCorak kain
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        kain = new KainCorak(config);
        
        kain.init();
        
        this.add(BorderLayout.CENTER, kain);
        
        kain.prepareCorak(projectPath, cor, obj, false);

        //move camera so that object fit in the canvas
        if (kain.getObjRadius() > 0) //if object exists
        {
            setProjectionMode(Scene3DObserverCookie.Projection.PERSPECTIVE);
        }
    }
    
    @Override
    public void setAxisLines(boolean enabled) {
        kain.setAxisLines(enabled);
    }
    
    @Override
    public boolean getAxisLinesEnabled() {
        return kain.getAxisLines();
    }
    
    @Override
    public void setGround(boolean enabled) {
        kain.setGround(enabled);
    }
    
    @Override
    public boolean getGroundEnabled() {
        return kain.getGround();
    }
    
    @Override
    public void optimalView() {
        kain.cameraDefault();
    }
    
    @Override
    public void changeBackgroundColor(Color newColor) {
        kain.changeBackgroundColor(newColor);
    }
    
    @Override
    public Color getBackgroundColor() {
        return kain.getBackgroundColor();
    }
    
    @Override
    public void changeAppearance(Appearance appearance) {
        kain.setCurrentAppearance(appearance);
    }
    
    @Override
    public void setProjectionMode(Projection mode) {
        kain.setProjectionMode(mode);
        kain.cameraDefault();
    }
    
    @Override
    public Projection getProjectionMode() {
        return kain.getProjectionMode();
    }
    
    @Override
    public Appearance getCurrentAppearance() {
        return kain.getCurrentAppearance();
    }
    
    @Override
    public void setLights(boolean enabled) {
        kain.setLights(enabled);
    }
    
    @Override
    public boolean getLightsEnabled() {
        return kain.getLights();
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
    
    @Override
    public BranchGroup getRootGroupForOBJ() {
        return kain.getKainBG();
    }
    
    @Override
    public String getDefaultOBJFilenameSuffix() {
        return obj.getName();
    }
    
    @Override
    public String getProjectPathForOBJ() {
        return projectPath;
    }
    
    @Override
    public void renderEnclosedOBJ() {
        kain.prepareCorak(projectPath, cor, obj, true);
        kain.refreshAppearance();
    }
    
    @Override
    public void doneRenderEnclosedOBJ() {
        kain.prepareCorak(projectPath, cor, obj, false);
        kain.refreshAppearance();
    }
    
    @Override
    public void setMeasurementMode(boolean enabled) {
        kain.setMeasurementMode(enabled);
    }
    
    @Override
    public boolean getMeasurementModeEnabled() {
        return kain.getMeasurementModeEnabled();
    }
    
    @Override
    public void setMeasurementVisible(boolean visible) {
        kain.setMeasurementVisible(visible);
    }
    
    @Override
    public boolean getMeasurementVisible() {
        return kain.getMeasurementVisible();
    }
    
    @Override
    public void clearAllMeasurement() {
        kain.clearAllMeasurment();
    }
    
    @Override
    public boolean isAnimatingX() {
        return kain.isAnimatingX();
    }
    
    @Override
    public boolean isAnimatingY() {
        return kain.isAnimatingY();
    }
    
    @Override
    public boolean isAnimatingZ() {
        return kain.isAnimatingZ();
    }
    
    @Override
    public void initAnimationThread() {
        kain.initAnimationThread();
    }
    
    @Override
    public void stopAnimation() {
        kain.stopAnimation();
    }
    
    @Override
    public void toggleXAnimation() {
        kain.toggleXAnimation();
    }
    
    @Override
    public void toggleYAnimation() {
        kain.toggleYAnimation();
    }
    
    @Override
    public void toggleZAnimation() {
        kain.toggleZAnimation();
    }
    
    @Override
    public void setChoosePivotPointMode(boolean enabled) {
        kain.setChoosePivotPointMode(enabled);
    }
    
    @Override
    public boolean getChoosePivotPointModeEnabled() {
        return kain.getChoosePivotPointModeEnabled();
    }
    
    @Override
    public JComponent getVisualRepresentation() {
        return this;
    }
    
    @Override
    public JComponent getToolbarRepresentation() {
        return toolbar;
    }
    
    @Override
    public Action[] getActions() {
        Collection<? extends AbstractAction> actionsInstances = lookup.lookupAll(AbstractAction.class);
        Action[] actions = actionsInstances.toArray(new Action[actionsInstances.size()]);
        return actions;
    }
    
    @Override
    public Lookup getLookup() {
        return lookup;
    }
    
    @Override
    public void componentOpened() {
        //
    }
    
    @Override
    public void componentClosed() {
        kain.cleanup();
        obj.removeLookUp(cor);
        this.removeAll();
        System.gc();
        
    }
    
    @Override
    public void componentShowing() {
        updateName();
    }
    
    @Override
    public void componentHidden() {
        //
    }
    InvalidableVisualLSystemModel vlsModel;
    
    @Override
    public void componentActivated() {
        InvalidableVisualLSystemModel mod = CentralLookup.getDefault().lookup(InvalidableVisualLSystemModel.class);
        //clearing previous, if any
        if (mod == null) {
            CentralLookup.getDefault().add(vlsModel);
        } else if (mod != vlsModel) {
            CentralLookup.getDefault().remove(mod);
            CentralLookup.getDefault().add(vlsModel);
        }
    }
    
    @Override
    public void componentDeactivated() {
        //
    }
    
    @Override
    public UndoRedo getUndoRedo() {
        return undoRedoManager;
    }
    
    @Override
    public void setMultiViewCallback(MultiViewElementCallback callback) {
        this.callback = callback;
    }
    
    @NbBundle.Messages({
        "# {0} - file name",
        "MSG_SaveModified=File {0} is modified. Save?"
    })
    @Override
    public CloseOperationState canCloseElement() {
        //ini dioverride biar kalo ada outstanding savable, minta popup confirmation dulu
        Savable sav = obj.getLookup().lookup(Savable.class);
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
            save.putValue(Action.LONG_DESCRIPTION, Bundle.MSG_SaveModified(obj.getPrimaryFile().getNameExt()));
            return MultiViewFactory.createUnsafeCloseState("editor", save, null);
        }
        return CloseOperationState.STATE_OK;
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
    
    @Override
    public void render() {
        kain.prepareCorak(projectPath, cor, obj, false);
        kain.refreshAppearance();
    }
    
    @Override
    public void generate() throws ParseRuleException {
        //try set axiom dan rules dari raw ke aslinya
        //this will NOT display notification if parsing fails.
        System.err.println("setaxiom on generate");
        cor.setAxiom(cor.getRawAxiom(), false);
        cor.setStringRules(cor.getRawDetails(), false);
    }
    
    @Override
    public String getDefaultName() {
        return obj.getName();
    }
    
    @Override
    public void writeImage(String path, PNGExportConfiguration config) {
        CorakRenderOptionPanel form = new CorakRenderOptionPanel();
        String msg = "Rendering Options";
        DialogDescriptor dd = new DialogDescriptor(form, msg);
        Object result = DialogDisplayer.getDefault().notify(dd);
        if (result != NotifyDescriptor.OK_OPTION) {
            return;
        }
        //preparing preview panel
        PNGExportPreviewPanel panel = new PNGExportPreviewPanel(path, config, kain.getSize(), this);
        String title = PNGExportPreviewPanel.TITLE;
        DialogDescriptor d2 = new DialogDescriptor(panel, title);

        //examine form..
        boolean useSunflow = (form.getQuality() == CorakRenderOptionPanel.QUALITY);
        
        Object export = DialogDisplayer.getDefault().notify(d2);
        if (export != NotifyDescriptor.OK_OPTION) {
            return;
        }

        //do write to file here
        //get resulted dimension
        boolean withBackground = !panel.getResultTransparent();
        kain.writePNG(panel.getResultDimension(), withBackground, path, useSunflow);
    }
    
    @Override
    public Image getPreviewImage(int w, int h, boolean withBackground) {
        return kain.getRenderedImage(new Dimension(w, h), withBackground);
    }
}
