/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.component;

import com.jbatik.modules.layout.visual.LayoutScene;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import org.netbeans.api.annotations.common.StaticResource;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Toolbar for LayoutScene. By default it contains button for reset library
 * mapping and checkbox to toggle auto-select mode.
 *
 * May contain another button if necessary.
 *
 * @author RAPID02
 */
@NbBundle.Messages(
        {
            "CTL_MoveToolButtonHint=Move",
            "CTL_TransformToolButtonHint=Transform",})
public class LayoutToolbar {

    @StaticResource
    private final String MOVE_ICON_LOC = "com/jbatik/modules/layout/component/resources/move_tool.png";
    private final String TRANSFORM_ICON_LOC = "com/jbatik/modules/layout/component/resources/resize_tool.png";

    private LayoutScene scene;

    private JToolBar toolbar, transformToolbar;
//    private JCheckBox autoSelectCB;

    private JToggleButton moveButton;
    private JToggleButton transformButton;
    private ButtonGroup group = new ButtonGroup();
    private Listener listener;

    public JToolBar getToolbar() {
        return toolbar;
    }

    public LayoutToolbar(LayoutScene scene) {
        this.scene = scene;
        initToolbar();
    }

    private void initToolbar() {
        if (toolbar == null) {
            toolbar = new JToolBar();

            toolbar.setFloatable(false);
            toolbar.add(Box.createHorizontalStrut(6));

            //init whatever listener
            listener = new Listener();

            //add auto select checkbox
//            autoSelectCB = new JCheckBox("Auto select");
//            autoSelectCB.setSelected(false);//false by default
//            autoSelectCB.addItemListener(listener);
//            toolbar.add(autoSelectCB);
//            toolbar.add(Box.createHorizontalStrut(6));
            //TOOLS
            /**
             * TODO : ini harusnya bisa bikin interface yang bisa diimplementasi
             * oleh tool provider provider nanti nyediain action yang nanti
             * dibikin togglebuttonnya tapi sementara ini cuma ada 2 tool jadi
             * hardcode di sini aja
             */
            //move (default)
            moveButton = new JToggleButton(ImageUtilities.loadImageIcon(MOVE_ICON_LOC, true));
            moveButton.addActionListener(listener);
            moveButton.setToolTipText(Bundle.CTL_MoveToolButtonHint());
            initButton(moveButton, true);
            moveButton.setSelected(true);
            toolbar.add(moveButton);
            //transform : resize+rotate
            transformButton = new JToggleButton(ImageUtilities.loadImageIcon(TRANSFORM_ICON_LOC, true));
            transformButton.addActionListener(listener);
            transformButton.setToolTipText(Bundle.CTL_TransformToolButtonHint());
            initButton(transformButton, true);
            toolbar.add(transformButton);
            toolbar.addSeparator();
            transformToolbar = new JToolBar();
            transformToolbar.setVisible(false);
            transformToolbar.setFloatable(false);
            for (Action action : getAlignActions()) {
                JButton button = toolbar.add(action);
                initButton(button, false);
            }

            for (Action action : getTransformActions()) {
                JButton buttonx = transformToolbar.add(action);
                initButton(buttonx, false);
            }
            toolbar.addSeparator();
            toolbar.add(transformToolbar);
        }
    }
    private List<Action> transformActions;

    private Collection<Action> getTransformActions() {
        if (transformActions == null) {
            transformActions = new ArrayList<>();
            // Transform actions
            // Move, asks for dy and dx
            transformActions.add(new MovePreciseAction(scene));
            // Rotate, asks for degree
            transformActions.add(new RotatePreciseAction(scene));
            // Resize, asks for new height OR new width, keep aspect ratio
            transformActions.add(new ResizePreciseAction(scene));
        }
        return transformActions;
    }
    private List<Action> designerActions;

    private Collection<Action> getAlignActions() {
        if (designerActions == null) {
            designerActions = new ArrayList<>();
            // Grouping actions
            designerActions.add(new AlignAction(scene, LayoutConstants.HORIZONTAL, LayoutConstants.LEADING));
            designerActions.add(new AlignAction(scene, LayoutConstants.HORIZONTAL, LayoutConstants.TRAILING));
            designerActions.add(new AlignAction(scene, LayoutConstants.HORIZONTAL, LayoutConstants.CENTER));
            designerActions.add(new AlignAction(scene, LayoutConstants.VERTICAL, LayoutConstants.LEADING));
            designerActions.add(new AlignAction(scene, LayoutConstants.VERTICAL, LayoutConstants.TRAILING));
            designerActions.add(new AlignAction(scene, LayoutConstants.VERTICAL, LayoutConstants.CENTER));
        }
        return designerActions;
    }

    private void initButton(AbstractButton button, boolean g) {
        if (!("Windows".equals(UIManager.getLookAndFeel().getID()) // NOI18N
                && (button instanceof JToggleButton))) {
            button.setBorderPainted(false);
        }
        if (g) {
            group.add(button);
        }
        button.setOpaque(false);
        button.setFocusPainted(false);
        button.setMargin(new Insets(0, 0, 0, 0));
    }

    /**
     * Listener untuk controls di toolbar
     */
    private class Listener implements ItemListener, ActionListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            Object source = e.getItemSelectable();
//            if (source == autoSelectCB) {
//                //Now that we know which button was pushed, find out
//                //whether it was selected or deselected.
//                boolean autoSelect = true;
//                if (e.getStateChange() == ItemEvent.DESELECTED) {
//                    autoSelect = false;
//                }
//                scene.setAutoSelect(autoSelect);
//            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == moveButton) {
                scene.setMoveMode();
//                moveButton.setSelected(true);
//                transformButton.setSelected(false);
                transformToolbar.setVisible(false);
            } else if (e.getSource() == transformButton) {
                scene.setTransformMode();
//                moveButton.setSelected(false);
//                transformButton.setSelected(true);
                transformToolbar.setVisible(true);
            }
        }
    }
}
