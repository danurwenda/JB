/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.newfile;

import com.jbatik.core.api.component.UnitPresenter;
import com.jbatik.modules.layout.node.LayoutFileUtil;
import com.jbatik.util.paper.PaperSize;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jscience.physics.amount.Amount;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import com.jbatik.core.format.DotDecimalFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import org.openide.util.Exceptions;

public final class NewLayoutVisualPanel1 extends JPanel {

    private static final String CUSTOM_SIZE = "Custom";
    private Project project;
//    private NewLayoutWizardPanel1 panel;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    protected ResourceBundle bundle = NbBundle.getBundle(NewLayoutVisualPanel1.class);
    private long lastDPI;
    private long widthInPixel, heightInPixel;

    /**
     * Creates new form NewLayoutVisualPanel1.
     */
    public NewLayoutVisualPanel1(NewLayoutWizardPanel1 panel) {
        this.project = panel.getProject();
        initComponents();
        //init DPI numTF
        DecimalFormat noComma = new DecimalFormat("####");
        noComma.setGroupingUsed(false);
        noComma.setParseIntegerOnly(true);
        noComma.setMaximumFractionDigits(0);
        numTFDPI.setFormat(noComma);

        DecimalFormat withComma = new DecimalFormat("####.##", DotDecimalFormat.getSymbols());
        withComma.setGroupingUsed(false);
        withComma.setParseIntegerOnly(false);
        withComma.setMaximumFractionDigits(3);
        numTFWidth.setFormat(withComma);
        numTFHeight.setFormat(withComma);

        numTFDPI.setValue(300);
        try {
            lastDPI = numTFDPI.getLongValue();
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        //special behavior of these 3 numeric-only text field.
        // 1. different selected unit may have different number formatter, for example
        // using pixel as unit will cause the width & height text field to always *display*
        // integer value, while some other commonly-used units (cm, mm, inch) permit 
        // setText() for decimal value, which in turn will be converted to (integer) pixel value
        // using current DPI setting
        // for further processing/converting
//
//        //BINDING LISTENER
//        //0. Update width & height spinners when dpi is changed AND current unit is pixel
        //see numTFDPIFocusLost
//        //1. update created files when name text field is updated.
        nameTF.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                fireChange();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                fireChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                fireChange();
            }

            private void fireChange() {
                createdFilesTA.setText(
                        getNewLayoutName().length() > 0
                                ? getNewLayoutPath()
                                : ""
                );
                changeSupport.fireChange();
            }

        });
        changeSupport.addChangeListener(panel);
//
//        //2. Update width & height spinner + unit combo when a predefined size is selected
        ItemListener sizeSetter = (ItemEvent e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                if (e.getItem() instanceof PaperSize) {
                    PaperSize selected = (PaperSize) e.getItem();
                    jcUnit.setSelectedItem(selected.getDefaultUnit());
                    numTFWidth.setValue(selected.getWidth().getEstimatedValue());
                    numTFHeight.setValue(selected.getHeight().getEstimatedValue());
                }
            }
        };
        jcSize.addItemListener(sizeSetter);
//
//        //3.a. Update orientation radio buttons when width/height spinner is updated
//        //3.b. Update paper size dropdown when width/height spinner is updated
        ActionListener sizeMatcher = (ActionEvent e) -> {
            try {
                numTFWidth.normalize();
                numTFHeight.normalize();
                double width1 = numTFWidth.getDoubleValue();
                double height1 = numTFHeight.getDoubleValue();
                if (width1 > height1) {
                    jrLandscape.setSelected(true);
                } else {
                    jrPortrait.setSelected(true);
                }
                Unit<Length> activeUnit = (Unit<Length>) jcUnit.getSelectedItem();
                PaperSize s = PaperSize.Factory.create(
                        Amount.valueOf(width1, activeUnit),
                        Amount.valueOf(height1, activeUnit),
                        activeUnit,
                        null
                );
                for (PaperSize ps : Lookup.getDefault().lookupAll(PaperSize.class)) {
                    if (ps.equals(s)) {
                        //found an approximately match paper size
                        //just set selection, without updating the spinners
                        jcSize.removeItemListener(sizeSetter);
                        jcSize.setSelectedItem(ps);
                        jcSize.addItemListener(sizeSetter);
                        return;
                    }
                }
                jcSize.setSelectedItem(CUSTOM_SIZE);
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
        };
        numTFWidth.addActionListener(sizeMatcher);
        numTFHeight.addActionListener(sizeMatcher);
//
//        //5. Swap width & height spinner when orientation selection is changed
        ActionListener swapper = (ActionEvent e) -> {
            try {
                double width1 = numTFWidth.getDoubleValue();
                double height1 = numTFHeight.getDoubleValue();
                if (e.getSource() == jrPortrait) {
                    if (width1 > height1) {
                        numTFHeight.setValue(width1);
                        numTFWidth.setValue(height1);
                    }
                } else if (e.getSource() == jrLandscape) {
                    if (width1 < height1) {
                        numTFHeight.setValue(width1);
                        numTFWidth.setValue(height1);
                    }
                }
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
        };
        jrPortrait.addActionListener(swapper);
        jrLandscape.addActionListener(swapper);
//
//        //paper size and unit
        loadPaperUnit();
//
//        //6. Update width & height spinner when unit dropdown is updated.
        jcUnit.addItemListener(new ItemListener() {
            Unit oldUnit;

            @Override
            public void itemStateChanged(ItemEvent e) {
                try {
                    if (e.getStateChange() == ItemEvent.DESELECTED) {
                        //save old value
                        oldUnit = (Unit) e.getItem();
                        if (oldUnit == NonSI.PIXEL) {
                            oldUnit = NonSI.INCH.divide(numTFDPI.getLongValue());
                            widthInPixel = numTFWidth.getLongValue();
                            heightInPixel = numTFHeight.getLongValue();
                        }
                    } else if (e.getStateChange() == ItemEvent.SELECTED) {
                        //update width & height
                        double width = numTFWidth.getDoubleValue();
                        double height = numTFHeight.getDoubleValue();
                        Amount oldWidth = Amount.valueOf(width, oldUnit);
                        Amount oldHeight = Amount.valueOf(height, oldUnit);

                        double neww, newh;
                        Unit newUnit = (Unit) e.getItem();
                        if (newUnit == NonSI.PIXEL) {
                            newUnit = NonSI.INCH.divide(numTFDPI.getLongValue());
                            neww = oldWidth.to(newUnit).getEstimatedValue();
                            newh = oldHeight.to(newUnit).getEstimatedValue();
                            //karena kalau pixel harusnya ga ada koma nya
                            neww = Math.round(neww);
                            newh = Math.round(newh);
                            numTFWidth.setFormat(noComma);
                            numTFHeight.setFormat(noComma);
                        } else {
                            neww = oldWidth.to(newUnit).getEstimatedValue();
                            newh = oldHeight.to(newUnit).getEstimatedValue();
                            numTFWidth.setFormat(withComma);
                            numTFHeight.setFormat(withComma);
                        }

                        numTFWidth.setValue(neww);
                        numTFHeight.setValue(newh);
                    }
                } catch (ParseException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        loadPaperSize();
        sizeMatcher.actionPerformed(null);
    }

    @Override
    public String getName() {
        return "New Layout File";
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        orientationBG = new javax.swing.ButtonGroup();
        nameLabel = new javax.swing.JLabel();
        nameTF = new javax.swing.JTextField();
        projectLabel = new javax.swing.JLabel();
        projectTF = new JTextField(ProjectUtils.getInformation(project).getDisplayName());
        createdFilesLabel = new javax.swing.JLabel();
        createdFilesTA = new javax.swing.JTextArea();
        jLabel2 = new javax.swing.JLabel();
        jcSize = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jcUnit = new javax.swing.JComboBox();
        jrPortrait = new javax.swing.JRadioButton();
        jrLandscape = new javax.swing.JRadioButton();
        jLabel5 = new javax.swing.JLabel();
        numTFWidth = new com.jbatik.core.api.component.NumericTextField();
        numTFHeight = new com.jbatik.core.api.component.NumericTextField();
        numTFDPI = new com.jbatik.core.api.component.NumericTextField();

        nameLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, org.openide.util.NbBundle.getMessage(NewLayoutVisualPanel1.class, "NewLayoutVisualPanel1.nameLabel.text")); // NOI18N

        nameTF.setText(org.openide.util.NbBundle.getMessage(NewLayoutVisualPanel1.class, "NewLayoutVisualPanel1.nameTF.text")); // NOI18N

        projectLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(projectLabel, org.openide.util.NbBundle.getMessage(NewLayoutVisualPanel1.class, "NewLayoutVisualPanel1.projectLabel.text")); // NOI18N

        projectTF.setEditable(false);

        createdFilesLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(createdFilesLabel, org.openide.util.NbBundle.getMessage(NewLayoutVisualPanel1.class, "NewLayoutVisualPanel1.createdFilesLabel.text")); // NOI18N

        createdFilesTA.setEditable(false);
        createdFilesTA.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        createdFilesTA.setColumns(20);
        createdFilesTA.setRows(5);
        createdFilesTA.setBorder(null);

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(NewLayoutVisualPanel1.class, "NewLayoutVisualPanel1.jLabel2.text")); // NOI18N

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(NewLayoutVisualPanel1.class, "NewLayoutVisualPanel1.jLabel3.text")); // NOI18N

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(NewLayoutVisualPanel1.class, "NewLayoutVisualPanel1.jLabel4.text")); // NOI18N

        orientationBG.add(jrPortrait);
        org.openide.awt.Mnemonics.setLocalizedText(jrPortrait, org.openide.util.NbBundle.getMessage(NewLayoutVisualPanel1.class, "NewLayoutVisualPanel1.jrPortrait.text")); // NOI18N

        orientationBG.add(jrLandscape);
        org.openide.awt.Mnemonics.setLocalizedText(jrLandscape, org.openide.util.NbBundle.getMessage(NewLayoutVisualPanel1.class, "NewLayoutVisualPanel1.jrLandscape.text")); // NOI18N

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(NewLayoutVisualPanel1.class, "NewLayoutVisualPanel1.jLabel5.text")); // NOI18N

        numTFDPI.setValue(300);
        numTFDPI.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                numTFDPIFocusLost(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 67, Short.MAX_VALUE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(nameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jcSize, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(nameTF)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(numTFWidth, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(numTFHeight, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(numTFDPI, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(12, 12, 12)
                                        .addComponent(jrPortrait)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jrLandscape))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(10, 10, 10)
                                        .addComponent(jcUnit, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(projectLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(createdFilesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(projectTF)
                            .addComponent(createdFilesTA, javax.swing.GroupLayout.DEFAULT_SIZE, 307, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLabel)
                    .addComponent(nameTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jcSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jcUnit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(numTFWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jrPortrait)
                    .addComponent(jrLandscape)
                    .addComponent(jLabel4)
                    .addComponent(numTFHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(numTFDPI, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(projectLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(projectTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(createdFilesTA, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(createdFilesLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Hanya relevan saat unit yang terpilih adalah PIXEL. Update angka width
     * dan height dengan suatu integer.
     *
     * @param evt
     */
    private void numTFDPIFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_numTFDPIFocusLost
        if (jcUnit.getSelectedItem().equals(NonSI.PIXEL)) {
            try {
                long oldW = numTFWidth.getLongValue();
                long oldH = numTFHeight.getLongValue();
                long newDPI = numTFDPI.getLongValue();
                numTFWidth.setValue((long) Math.round(oldW * newDPI / lastDPI));
                numTFHeight.setValue((long) Math.round(oldH * newDPI / lastDPI));
                lastDPI = newDPI;
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }//GEN-LAST:event_numTFDPIFocusLost

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel createdFilesLabel;
    private javax.swing.JTextArea createdFilesTA;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JComboBox jcSize;
    private javax.swing.JComboBox jcUnit;
    private javax.swing.JRadioButton jrLandscape;
    private javax.swing.JRadioButton jrPortrait;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTF;
    private com.jbatik.core.api.component.NumericTextField numTFDPI;
    private com.jbatik.core.api.component.NumericTextField numTFHeight;
    private com.jbatik.core.api.component.NumericTextField numTFWidth;
    private javax.swing.ButtonGroup orientationBG;
    private javax.swing.JLabel projectLabel;
    private javax.swing.JTextField projectTF;
    // End of variables declaration//GEN-END:variables

    String getNewLayoutName() {
        return nameTF.getText();
    }

    Double getNewLayoutWidth() throws ParseException {
        return numTFWidth.getDoubleValue();
    }

    Double getNewLayoutHeight() throws ParseException {
        return numTFHeight.getDoubleValue();
    }

    private void loadPaperSize() {
        jcSize.removeAllItems();
        for (PaperSize ps : Lookup.getDefault().lookupAll(PaperSize.class)) {
            jcSize.addItem(ps);
        }
        //and custom size
        jcSize.addItem(CUSTOM_SIZE);
    }

    private void loadPaperUnit() {
        ArrayList arrayList = UnitPresenter.getUnit();
        jcUnit.removeAllItems();
        arrayList.stream().forEach((arrayList1) -> {
            jcUnit.addItem(arrayList1);
        });
    }

    public int showAsDialog(Component parentComponent) {
        nameTF.setText("");

        boolean close = false;
        int choice = JOptionPane.CANCEL_OPTION;

        while (!close) {
            choice = JOptionPane.showConfirmDialog(parentComponent, this,
                    bundle.getString("NewLayoutVisualPanel1.title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (choice == JOptionPane.OK_OPTION) {
                if (nameTF.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(new JFrame(), bundle.getString("NewLayoutVisualPanel1.error1"), "ERROR", JOptionPane.ERROR_MESSAGE);
                    close = false;
                } else {
                    close = true;
                }
            } else {
                close = true;
            }
        }

        return choice;
    }

    String getNewLayoutUnit() {
        return jcUnit.getModel().getSelectedItem().toString();
    }

    boolean valid(WizardDescriptor wiz) {
        if (getNewLayoutName().length() < 1) {
            return false;
        } else {
            File f = new File(getNewLayoutPath());
            if (f.exists()) {
                wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                        "The file " + f.getPath() + " is already exist.");
                return false;
            } else {
                wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
                return true;
            }
        }
    }

    public String getNewLayoutPath() {
        return FileUtil.toFile(LayoutFileUtil.getLayoutsFolder(project, false)).getPath()
                + File.separatorChar + getNewLayoutName() + ".lay";
    }

    Long getNewLayoutDPI() throws ParseException {
        return numTFDPI.getLongValue();
    }
}
