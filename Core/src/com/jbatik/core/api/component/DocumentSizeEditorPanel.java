/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.core.api.component;

import com.jbatik.core.api.DocumentPaper;
import com.jbatik.core.format.DotDecimalFormat;
import com.jbatik.util.paper.PaperSize;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;
import javax.swing.JPanel;
import org.jscience.physics.amount.Amount;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import java.text.DecimalFormat;
import java.text.ParseException;
import org.openide.util.Exceptions;

public final class DocumentSizeEditorPanel extends JPanel {

    private static final String CUSTOM_SIZE = "Custom";
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    protected ResourceBundle bundle = NbBundle.getBundle(DocumentSizeEditorPanel.class);
    private long lastDPI;
    private long widthInPixel, heightInPixel;

    /**
     * Creates new form NewLayoutVisualPanel1.
     */
    public DocumentSizeEditorPanel(DocumentPaper doc) {
        initComponents();
        //init DPI numTF
        DecimalFormat noComma = new DecimalFormat("####");
        noComma.setGroupingUsed(false);
        noComma.setParseIntegerOnly(true);
        noComma.setMaximumFractionDigits(0);
        numTFDPI.setFormat(noComma);

        DecimalFormat withComma = new DecimalFormat("####.##",DotDecimalFormat.getSymbols());
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
                } else{
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
        //load doc
        jcUnit.setSelectedItem(doc.getUnit());
        numTFDPI.setValue(doc.getDPI());
        numTFWidth.setValue(doc.getWidth());
        numTFHeight.setValue(doc.getHeight());
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

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(DocumentSizeEditorPanel.class, "DocumentSizeEditorPanel.jLabel2.text")); // NOI18N

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(DocumentSizeEditorPanel.class, "DocumentSizeEditorPanel.jLabel3.text")); // NOI18N

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(DocumentSizeEditorPanel.class, "DocumentSizeEditorPanel.jLabel4.text")); // NOI18N

        orientationBG.add(jrPortrait);
        org.openide.awt.Mnemonics.setLocalizedText(jrPortrait, org.openide.util.NbBundle.getMessage(DocumentSizeEditorPanel.class, "DocumentSizeEditorPanel.jrPortrait.text")); // NOI18N

        orientationBG.add(jrLandscape);
        org.openide.awt.Mnemonics.setLocalizedText(jrLandscape, org.openide.util.NbBundle.getMessage(DocumentSizeEditorPanel.class, "DocumentSizeEditorPanel.jrLandscape.text")); // NOI18N

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(DocumentSizeEditorPanel.class, "DocumentSizeEditorPanel.jLabel5.text")); // NOI18N

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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 67, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jcSize, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                                .addComponent(jcUnit, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
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
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JComboBox jcSize;
    private javax.swing.JComboBox jcUnit;
    private javax.swing.JRadioButton jrLandscape;
    private javax.swing.JRadioButton jrPortrait;
    private com.jbatik.core.api.component.NumericTextField numTFDPI;
    private com.jbatik.core.api.component.NumericTextField numTFHeight;
    private com.jbatik.core.api.component.NumericTextField numTFWidth;
    private javax.swing.ButtonGroup orientationBG;
    // End of variables declaration//GEN-END:variables

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

    public Unit getUnit() {
        return (Unit) jcUnit.getSelectedItem();
    }

    public long getDPI() throws ParseException {
        return numTFDPI.getLongValue();
    }

    public double getDocumentWidth() throws ParseException {
        return numTFWidth.getDoubleValue();
    }

    public double getDocumentHeight() throws ParseException {
        return numTFHeight.getDoubleValue();
    }
}
