/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.lsystem.api.editor;

import com.jbatik.core.api.CentralLookup;
import com.jbatik.lsystem.InvalidableVisualLSystem;
import com.jbatik.lsystem.VisualLSystemRenderer;
import com.jbatik.lsystem.parser.exceptions.ParseRuleException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Collections;
import java.util.WeakHashMap;
import javax.swing.ImageIcon;
import javax.swing.JSpinner;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.UndoRedo;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//com.jbatik.lsystem.editor//LSystemEditor//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "LSystemEditorTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "properties_float", openAtStartup = true)
@ActionID(category = "Window", id = "com.jbatik.lsystem.editor.LSystemEditorTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_LSystemEditorAction",
        preferredID = "LSystemEditorTopComponent"
)
@Messages({
    "CTL_LSystemEditorAction=LSystem Editor",
    "CTL_LSystemEditorTopComponent=LSystem Editor",
    "HINT_LSystemEditorTopComponent=This is an LSystem Editor window",
    "LBL_Axiom=Axiom",
    "LBL_Detail=Detail",
    "BTN_Generate=Generate"
})
public final class LSystemEditorTopComponent extends TopComponent {

    private UndoRedo.Manager activeUndoRedo;
    private InvalidableVisualLSystem activeVLS;
    private InvalidableVisualLSystemModel activeVLSModel;
    private VisualLSystemModelListener listener;
    private Lookup.Result<InvalidableVisualLSystemModel> resultVLS;
    private LookupListener lookupVLSListener;
    private PropertyChangeListener axiomChangeListener;

    //documents
    private Document rawAxiomDocument;
    private Document rawRulesDocument;
    private boolean outsideChanges = false;
    private boolean innerChanges = false;

    //ICONS
    @StaticResource
    private final String OK_ICON_PATH = "com/jbatik/lsystem/api/editor/ok.png";
    private final ImageIcon OK_ICON = ImageUtilities.loadImageIcon(OK_ICON_PATH, false);
    @StaticResource
    private final String WARNING_ICON_PATH = "com/jbatik/lsystem/api/editor/warning.png";
    private final ImageIcon WARNING_ICON = ImageUtilities.loadImageIcon(WARNING_ICON_PATH, false);

    public LSystemEditorTopComponent() {
        
        initComponents();
        rawAxiomDocument = rawAxiomTF.getDocument();
        rawRulesDocument = rawRulesTA.getDocument();
        setName(Bundle.CTL_LSystemEditorTopComponent());
        setToolTipText(Bundle.HINT_LSystemEditorTopComponent());
        axiomChangeListener = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(InvalidableVisualLSystem.AXIOM_PROP)) {
                    //test whether the event was triggered from rawAxiomTF/rawRulesTA
                    if (!innerChanges) {
                        //update axiom
                        InvalidableVisualLSystem source = (InvalidableVisualLSystem) evt.getSource();
                        outsideChanges = true;
                        rawAxiomTF.setText(source.getRawAxiom());
                        activeVLSModel.addSavable();
                        outsideChanges = false;
                    }
                }
            }
        };
        DocumentListener parseLSListener = new DocumentListener() {
            int errorSource = -1;//0: axiom, 1 :detail, -1 : none

            private void updateStatus(DocumentEvent e) {
                Document d = e.getDocument();
                String s = "";
                try {
                    s = d.getText(0, d.getLength());
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
                //updateDocument may be called from axiom and rules
                if (d.equals(rawAxiomDocument)) {
                    try {
                        activeVLS.setAxiom(s, false);
                        if (errorSource == 0) {
                            //fixing previous axiom error
                            jlStatusIcon.setIcon(OK_ICON);
                            jlError.setText("");
                            errorSource = -1;
                        }
                    } catch (ParseRuleException ex) {
                        // TODO : add styled remark on JTextPane
                        if (errorSource == -1) {
                            jlStatusIcon.setIcon(WARNING_ICON);
                            jlError.setText("Axiom:" + (ex.getErrorPos() + 1));
                            errorSource = 0;
                        }
                    }
                } else if (d.equals(rawRulesDocument)) {
                    try {
                        activeVLS.setStringRules(s, false);
                        if (errorSource == 1) {
                            //fixing previous rules error
                            jlStatusIcon.setIcon(OK_ICON);
                            jlError.setText("");
                            errorSource = -1;
                        }
                    } catch (ParseRuleException ex) {
                        if (errorSource == -1) {
                            jlStatusIcon.setIcon(WARNING_ICON);
                            jlError.setText("Error " + (ex.getLineNumber() + 1) + ":" + (ex.getErrorPos() + 1));
                            errorSource = 1;
                        }
                    }
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateStatus(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateStatus(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateStatus(e);
            }
        };

        rawAxiomDocument.addDocumentListener(parseLSListener);
        rawRulesDocument.addDocumentListener(parseLSListener);

        //vlsmodel listener, listens to the change of lsystem instance+undomanager
        this.lookupVLSListener = (LookupEvent ev) -> {
            Collection<? extends InvalidableVisualLSystemModel> allVLS = resultVLS.allInstances();
            if (allVLS.size() != 1) {
                //we accept one and only one
                setEnableFields(false);
                content.set(Collections.EMPTY_LIST, null);
            } else if (allVLS.size() == 1) {
                setEnableFields(true);
                if (activeVLSModel != null) {
                    removeListener();
                    activeVLSModel.getVls().removePropertyChangeListener(axiomChangeListener);
                }
                activeVLSModel = allVLS.iterator().next();
                activeVLSModel.getVls().addPropertyChangeListener(axiomChangeListener);
                content.set(Collections.singleton(activeVLSModel), null);
                activeVLS = activeVLSModel.getVls();
                activeUndoRedo = activeVLSModel.getUndoredomgr();
                rawAxiomTF.setText(activeVLS.getRawAxiom());
                rawRulesTA.setText(activeVLS.getRawDetails());
                listener = getModelListener(activeVLSModel);
                attachListener();
            }
        };
    }

    boolean fieldsEnable = true;

    synchronized void setEnableFields(boolean enable) {
        if (fieldsEnable != enable) {
            rawAxiomTF.setEnabled(enable);
            rawRulesTA.setEnabled(enable);
            generateButton.setEnabled(enable);
            fieldsEnable = enable;
        }
    }

    // FIXME ini di put terus ga pernah di delete sepanjang jalan kenangan??
    //semoga gegera pakai weak jadi bagus
    private final WeakHashMap<InvalidableVisualLSystemModel, VisualLSystemModelListener> listenersMap = new WeakHashMap<>();

    private VisualLSystemModelListener getModelListener(InvalidableVisualLSystemModel m) {
        VisualLSystemModelListener l = listenersMap.get(m);
        if (l == null) {
            l = new VisualLSystemModelListener();
            listenersMap.put(m, l);
        }
        return l;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel6 = new javax.swing.JLabel();
        rawAxiomTF = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        rawRulesTA = new javax.swing.JTextArea();
        generateButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jlError = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jlStatusIcon = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, Bundle.LBL_Axiom());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, Bundle.LBL_Detail());

        rawRulesTA.setColumns(20);
        rawRulesTA.setRows(5);
        jScrollPane1.setViewportView(rawRulesTA);

        org.openide.awt.Mnemonics.setLocalizedText(generateButton, Bundle.BTN_Generate());
        generateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateButtonActionPerformed(evt);
            }
        });

        jPanel1.setPreferredSize(new java.awt.Dimension(86, 24));

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jlError)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 374, Short.MAX_VALUE)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jlStatusIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jlStatusIcon, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jlError, javax.swing.GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE)
            .addComponent(jSeparator1)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 414, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 310, Short.MAX_VALUE)
                            .addComponent(rawAxiomTF)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(generateButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(rawAxiomTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 294, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(generateButton, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents


    private void generateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateButtonActionPerformed
        VisualLSystemRenderer renderer = activeVLS.getRenderer();
        if (renderer != null) {
            try {
                renderer.generate();
            } catch (ParseRuleException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                renderer.render();
            }
        }
    }//GEN-LAST:event_generateButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton generateButton;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel jlError;
    private javax.swing.JLabel jlStatusIcon;
    private javax.swing.JTextField rawAxiomTF;
    private javax.swing.JTextArea rawRulesTA;
    // End of variables declaration//GEN-END:variables

//    DummyLookupListener dum = new DummyLookupListener();
    @Override
    public void componentOpened() {
        resultVLS = CentralLookup.getDefault().lookupResult(InvalidableVisualLSystemModel.class);
        resultVLS.addLookupListener(lookupVLSListener);
        lookupVLSListener.resultChanged(null);
//        dum.startListening();
    }

    @Override
    public void componentClosed() {
        resultVLS.removeLookupListener(lookupVLSListener);
        resultVLS = null;
//        dum.stopListening();
    }

    private void removeListener() {
        rawAxiomDocument.removeDocumentListener(listener);
        rawRulesDocument.removeDocumentListener(listener);
        rawAxiomDocument.removeUndoableEditListener(listener);
        rawRulesDocument.removeUndoableEditListener(listener);
    }

    private void attachListener() {
        rawAxiomDocument.addDocumentListener(listener);
        rawRulesDocument.addDocumentListener(listener);
        rawAxiomDocument.addUndoableEditListener(listener);
        rawRulesDocument.addUndoableEditListener(listener);
    }

    private void resetFields() {
        rawAxiomTF.setText("");
        rawRulesTA.setText("");
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    public UndoRedo getUndoRedo() {
        return activeUndoRedo;
    }

    private Lookup lookup;
    private InstanceContent content = new InstanceContent();

    @Override
    public Lookup getLookup() {
        if (lookup == null) {
            lookup = new AbstractLookup(content);
        }
        return lookup;
    }

    private class VisualLSystemModelListener
            implements
            DocumentListener,
            UndoableEditListener {

        Object lastEditorField;
        boolean isUndoOrRedoing;

        private VisualLSystemModelListener() {
            this.isUndoOrRedoing = false;
        }

        private void addUndo(JSpinner spinner, int o, int n) {
            lastEditorField = spinner;
        }

        private void addUndo(JSpinner spinner, String o, String n) {
            addUndo(spinner, Integer.parseInt(o), Integer.parseInt(n));
        }

        void addUndoFromEvent(JSpinner s, PropertyChangeEvent e) {
            addUndo(s, e.getOldValue().toString(), e.getNewValue().toString());
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            updateDocument(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            updateDocument(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            updateDocument(e);
        }

        private void updateDocument(DocumentEvent e) {
            if (!outsideChanges) {
                Document d = e.getDocument();
                String s = "";
                try {
                    s = d.getText(0, d.getLength());
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
                //updateDocument may be called from axiom and rules
                if (d.equals(rawAxiomDocument)) {
                    innerChanges = true;
                    activeVLS.setRawAxiom(s);
                    innerChanges = false;
                } else if (d.equals(rawRulesDocument)) {
                    activeVLS.setRawDetails(s);
                }
                activeVLSModel.addSavable();
            }
        }

        @Override
        public void undoableEditHappened(UndoableEditEvent e) {
            activeUndoRedo.undoableEditHappened(e);
        }
    }
}
