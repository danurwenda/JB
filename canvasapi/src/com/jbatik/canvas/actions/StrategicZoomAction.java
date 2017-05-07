/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.canvas.actions;

import com.jbatik.canvas.visual.CalibratedScene;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Scene.SceneListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

@ActionID(
        category = "Tools",
        id = "com.jbatik.modules.layout.actions.StrategicZoomAction"
)
@ActionRegistration(
        lazy = false,
        displayName = "#CTL_StrategicZoomAction"
)
@ActionReference(path = "Toolbars/View", position = 300)
@NbBundle.Messages("CTL_StrategicZoomAction=Strategic Zoom")
/**
 *
 * @author RAPID02
 */
public class StrategicZoomAction
        extends
        AbstractAction
        implements
        ContextAwareAction, LookupListener, Presenter.Toolbar, SceneListener {

    private final Lookup lookup;

    public StrategicZoomAction() {
        this(Utilities.actionsGlobalContext());
    }

    private StrategicZoomAction(Lookup lookup) {
        this.lookup = lookup;
        this.lagiPindahModel = false;
        //set the initial enabled state
        setEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        StrategicZoom z = (StrategicZoom) zoomlist.getSelectedItem();
        if (z != null) {
//           DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("sesuatu "+z.getDisplayName()));
            if (scene != null) {
                scene.getView().requestFocusInWindow();
            }
        }
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new StrategicZoomAction(actionContext);
    }
    private Lookup.Result<Scene> res;

    @Override
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        boolean startListening = getPropertyChangeListeners().length == 0;
        super.addPropertyChangeListener(l);
        if (startListening) {
            res = lookup.lookupResult(Scene.class);
            res.addLookupListener(this);
        }
    }

    @Override
    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        super.removePropertyChangeListener(l);
        if (getPropertyChangeListeners().length == 0) {
            res.removeLookupListener(this);
            res = null;
        }
    }

    private Scene scene;
    private boolean lagiPindahModel;

    @Override
    public void resultChanged(LookupEvent ev) {
        if (!res.allInstances().isEmpty()) {
            if (scene != null) {
                //remove listener on previous scene
                scene.removeSceneListener(this);
            }
            scene = res.allInstances().iterator().next();
            scene.addSceneListener(this);
            Lookup.Result<StrategicZoom> strategies = scene.getLookup().lookupResult(StrategicZoom.class);
            if (!strategies.allInstances().isEmpty()) {
                //set the flag bahwa sekarang ini mau ganti model.
                //by default, setModel akan trigger setItem dari first item di model
                //jadi kalo first item nya toFit, tiap kali pindah tab, bakal jadi toFit
                lagiPindahModel = true;
                zoomlist.setModel(new DefaultComboBoxModel(strategies.allInstances().toArray()));
                lagiPindahModel = false;
            } else {
                zoomlist.setModel(new DefaultComboBoxModel());
            }

            ((ZoomComboBoxEditor) zoomlist.getEditor()).setZoomText(getCalibratedZoomFactor());
        } else {
            if (scene != null) {
                //remove listener on previous scene
                scene.removeSceneListener(this);
            }
        }

        setEnabled(!res.allItems().isEmpty());
    }

    private JComboBox zoomlist;

    @Override
    public Component getToolbarPresenter() {
        //actually start listening here
        if (res == null) {
            res = lookup.lookupResult(Scene.class);
            res.addLookupListener(this);
        }
        return getZoomDropdown();
    }

    /**
     * Returned combobox should be - editable : user can input zoom value -
     * using custom editor : To Height will not displayed as "To Height" - NOT
     * using custom renderer : To Height still displayed as "To Height" on the
     * dropdown menu
     *
     * @return a combo box
     */
    private JComboBox getZoomDropdown() {
        if (zoomlist == null) {
            zoomlist = new JComboBox();
            zoomlist.setPreferredSize(new Dimension(100, 15));
            zoomlist.addActionListener(this);
            zoomlist.setEditable(true);
            zoomlist.setEditor(new ZoomComboBoxEditor());
        }
        return zoomlist;
    }

    @Override
    public final void setEnabled(boolean enabled) {

        super.setEnabled(enabled);
        getZoomDropdown().setEnabled(enabled);

    }

    @Override
    public void sceneRepaint() {
    }

    @Override
    public void sceneValidating() {
    }

    @Override
    public void sceneValidated() {
        //zoom factor might be changed
        ((ZoomComboBoxEditor) zoomlist.getEditor()).setZoomText(getCalibratedZoomFactor());
    }

    private double getCalibratedZoomFactor() {
        if (scene instanceof CalibratedScene) {
            CalibratedScene calibratedScene = (CalibratedScene) scene;
            return calibratedScene.getCalibratedZoomFactor();
        } else {
            return scene.getZoomFactor();
        }
    }

    private void setCalibratedZoomFactor(double d) {
        if (scene instanceof CalibratedScene) {
            CalibratedScene calibratedScene = (CalibratedScene) scene;
            calibratedScene.setCalibratedZoomFactor(d);
        } else {
            scene.setZoomFactor(d);
        }
    }

    private class ZoomComboBoxEditor implements ComboBoxEditor {

        final protected JTextField editor;

        public ZoomComboBoxEditor() {
            editor = new JTextField();
            editor.addActionListener((ActionEvent ae) -> {
                Integer z = null;
                try {
                    z = Integer.parseInt(editor.getText());
                } catch (NumberFormatException ex) {
                } finally {
                    if (z != null) {
                        setCalibratedZoomFactor(((double) z) / 100);
                    }
                }
            });
        }

        @Override
        public Component getEditorComponent() {
            return editor;
        }

        @Override
        public void setItem(Object o) {
            if (!lagiPindahModel) {
                //the Object o should be an instance of StrategicZoom
                if (o instanceof StrategicZoom) {
                    StrategicZoom z = (StrategicZoom) o;
                    z.zoom();
                }
            }
        }

        public void setZoomText(double s) {
            double zoomvalue = Math.floor(s * 100);
            editor.setText(String.valueOf(zoomvalue) + "%");
        }

        @Override
        public Object getItem() {
//            System.err.println("getItem?");
            //the object returned should be an instance of StrategicZoom
            return null;
        }

        @Override
        public void selectAll() {
        }

        @Override
        public void addActionListener(ActionListener al) {
            editor.addActionListener(al);
        }

        @Override
        public void removeActionListener(ActionListener al) {
            editor.removeActionListener(al);
        }

    }

}
