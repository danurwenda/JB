/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.core.api.component;

import com.jbatik.core.api.ExportAsPNG;
import com.jbatik.core.api.PNGExportConfiguration;
import com.jbatik.core.format.DotDecimalFormat;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jscience.physics.amount.Amount;

/**
 *
 * @author Dimas Y. Danurwenda
 */
public class PNGExportPreviewPanel extends javax.swing.JPanel {

    public static final String TITLE = "Export to PNG";
    private String path;
    private PNGExportConfiguration config;
    private Amount<Length> defWidth;
    private Amount<Length> defHeight;
    private Unit<Length> defUnit;
    private AspectRatioBinder binderH;
    private AspectRatioBinder binderW;
    private FindMaxListener roundToSquareListener;
    private ExportAsPNG exportimg;

    /**
     * Creates new form PNGExporterPanel
     *
     * @param path File location on disk where PNG image will be saved
     * @param config PNG exporting configuration
     * @param dim Default dimension of resulted image, in pixel.
     */
    public PNGExportPreviewPanel(String path, PNGExportConfiguration config, Dimension dim, ExportAsPNG provider) {
        this(path, config, Amount.valueOf(dim.width, NonSI.PIXEL), Amount.valueOf(dim.height, NonSI.PIXEL), NonSI.PIXEL, provider);
    }

    public PNGExportPreviewPanel(String path, PNGExportConfiguration config, Amount<Length> width, Amount<Length> height, Unit<Length> unit, ExportAsPNG provider) {
        this.path = path;
        this.config = config;
        this.defWidth = width;
        this.defHeight = height;
        this.defUnit = unit;
        this.exportimg = provider;

        this.roundToSquareListener = new FindMaxListener();

        initComponents();

        //Aspect ratio locking
        binderH = new AspectRatioBinder(jsHeightRatio);
        binderW = new AspectRatioBinder(jsWidthRatio);

        //set lock visible
        lock.setText("");

        //binding listener between input fields
        //1. update unit label based on selected unit
        jcUnit.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String unit = jcUnit.getSelectedItem().toString();
                jlHeightUnit.setText(unit);
                jlWidthUnit.setText(unit);

            }
        });

        //init units combobox
        Object[] units = {NonSI.PIXEL, SI.MILLIMETER, SI.CENTIMETER, NonSI.INCH};
        ArrayList arrayList = new ArrayList();
        arrayList.addAll(Arrays.asList(units));
        arrayList.stream().forEach((arrayList1) -> {
            jcUnit.addItem(arrayList1);
        });
        //set default unit
        jcUnit.setSelectedItem(defUnit);
        //set default width & height
        jsWidth.setValue(defWidth.getEstimatedValue());
        jsHeight.setValue(defHeight.getEstimatedValue());

        //Update width & height spinner when unit dropdown is updated.
        jcUnit.addItemListener(new ItemListener() {
            Unit oldUnit;

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.DESELECTED) {
                    //save old value
                    oldUnit = (Unit) e.getItem();
                } else if (e.getStateChange() == ItemEvent.SELECTED) {
                    //update spinners
                    double width = (double) jsWidth.getValue();
                    double height = (double) jsHeight.getValue();
                    Amount oldWidth = Amount.valueOf(width, oldUnit);
                    Amount oldHeight = Amount.valueOf(height, oldUnit);
                    Unit newUnit = (Unit) e.getItem();
                    jsWidth.setValue(oldWidth.to(newUnit).getEstimatedValue());
                    jsHeight.setValue(oldHeight.to(newUnit).getEstimatedValue());
                }
            }
        });

//update width spinner when width ratio is updated, and vice versa
        jsWidthRatio.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                DecimalFormat rounder;
                rounder = new DecimalFormat("#");
                Double widthRatio = Double.parseDouble(jsWidthRatio.getValue().toString());
                Unit currentUnit = (Unit) jcUnit.getSelectedItem();
                double newWidth = Math.round(defWidth.to(currentUnit).getEstimatedValue() * widthRatio / 100);
                jsWidth.setValue(newWidth);
                double h = Double.parseDouble(jsHeight.getValue().toString());
                double w = Double.parseDouble(jsWidth.getValue().toString());
                jlRoundedDimension.setText(rounder.format(w) + " x " + (rounder.format(h) + " " + jcUnit.getSelectedItem().toString()));
            }
        });
        jsWidth.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                Unit currentUnit = (Unit) jcUnit.getSelectedItem();
                double newValue = Amount.valueOf((double) jsWidth.getValue(), currentUnit).to(defUnit).getEstimatedValue();
                double defValue = defWidth.getEstimatedValue();
                double ratio = (newValue * 100 / defValue);
                jsWidthRatio.setValue(ratio);

            }
        });

        //update width spinner when width ratio is updated
        jsHeightRatio.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                DecimalFormat rounder;
                rounder = new DecimalFormat("#");
                Double heightRatio = Double.valueOf(jsHeightRatio.getValue().toString());
                Unit currentUnit = (Unit) jcUnit.getSelectedItem();
                double newHeight = Math.round(defHeight.to(currentUnit).getEstimatedValue() * heightRatio / 100);
                jsHeight.setValue(newHeight);
                double h = Double.parseDouble(jsHeight.getValue().toString());
                double w = Double.parseDouble(jsWidth.getValue().toString());
                jlRoundedDimension.setText(rounder.format(w) + " x " + (rounder.format(h) + " " + jcUnit.getSelectedItem().toString()));
            }
        });
        jsHeight.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                Unit currentUnit = (Unit) jcUnit.getSelectedItem();
                double newValue = Amount.valueOf((double) jsHeight.getValue(), currentUnit).to(defUnit).getEstimatedValue();
                double defValue = defHeight.getEstimatedValue();
                double ratio = (newValue * 100 / defValue);
                jsHeightRatio.setValue(ratio);

            }
        });

        if (config.isForceTransparency()) {
            jcbTrans.setSelected(true);
            jcbTrans.setEnabled(false);
        }

        jcbTrans.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                loadImage(e.getStateChange() != ItemEvent.SELECTED);
            }
        });

        // update when Width is update , this listener use for load image with the new width 
        jsWidth.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                loadImage(!jcbTrans.isSelected());
            }
        });

        // update when Height is update , this listener use for load image with the new height
        jsHeight.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                loadImage(!jcbTrans.isSelected());
            }
        });

        // set
        setUnitInfoDimension();

        // loadimage
        loadImage(!jcbTrans.isSelected());

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jcUnit = new javax.swing.JComboBox();
        jsWidth = new javax.swing.JSpinner();
        jsHeight = new javax.swing.JSpinner();
        jlWidthUnit = new javax.swing.JLabel();
        jlHeightUnit = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jcbRatio = new javax.swing.JCheckBox();
        jcbTrans = new javax.swing.JCheckBox();
        jlDimensionTitle = new javax.swing.JLabel();
        jsWidthRatio = new javax.swing.JSpinner();
        jsHeightRatio = new javax.swing.JSpinner();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jlRoundedDimension = new javax.swing.JLabel();
        lock = new javax.swing.JLabel();

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(PNGExportPreviewPanel.class, "PNGExportPreviewPanel.jPanel2.border.title"))); // NOI18N

        jPanel1.setPreferredSize(new java.awt.Dimension(256, 256));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 256, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 256, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 256, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addGap(0, 14, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 14, Short.MAX_VALUE)))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(PNGExportPreviewPanel.class, "PNGExportPreviewPanel.jPanel3.border.title"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(PNGExportPreviewPanel.class, "PNGExportPreviewPanel.jLabel1.text")); // NOI18N

        jTextField1.setEditable(false);
        jTextField1.setText(this.path);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(PNGExportPreviewPanel.class, "PNGExportPreviewPanel.jLabel2.text")); // NOI18N

        jsWidth.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(1.0d), Double.valueOf(1.0d), null, Double.valueOf(1.0d)));

        jsHeight.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(1.0d), Double.valueOf(1.0d), null, Double.valueOf(1.0d)));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(PNGExportPreviewPanel.class, "PNGExportPreviewPanel.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(PNGExportPreviewPanel.class, "PNGExportPreviewPanel.jLabel4.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jcbRatio, org.openide.util.NbBundle.getMessage(PNGExportPreviewPanel.class, "PNGExportPreviewPanel.jcbRatio.text")); // NOI18N
        jcbRatio.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jcbRatioItemStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jcbTrans, org.openide.util.NbBundle.getMessage(PNGExportPreviewPanel.class, "PNGExportPreviewPanel.jcbTrans.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jlDimensionTitle, org.openide.util.NbBundle.getMessage(PNGExportPreviewPanel.class, "PNGExportPreviewPanel.jlDimensionTitle.text")); // NOI18N

        jsWidthRatio.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(100.0d), Double.valueOf(1.0d), null, Double.valueOf(5.0d)));

        jsHeightRatio.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(100.0d), Double.valueOf(1.0d), null, Double.valueOf(5.0d)));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(PNGExportPreviewPanel.class, "PNGExportPreviewPanel.jLabel5.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(PNGExportPreviewPanel.class, "PNGExportPreviewPanel.jLabel6.text")); // NOI18N

        lock.setFont(new java.awt.Font("Adobe Fangsong Std R", 0, 38)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(lock, org.openide.util.NbBundle.getMessage(PNGExportPreviewPanel.class, "PNGExportPreviewPanel.lock.text")); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jcbTrans)
                            .addComponent(jcbRatio)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel3)
                                    .addComponent(jLabel4))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanel3Layout.createSequentialGroup()
                                                .addComponent(jsHeight, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jlHeightUnit))
                                            .addGroup(jPanel3Layout.createSequentialGroup()
                                                .addComponent(jsWidth, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jlWidthUnit)))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jsWidthRatio, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jsHeightRatio, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jLabel5))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(lock))
                                    .addComponent(jcUnit, javax.swing.GroupLayout.PREFERRED_SIZE, 196, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(27, 27, 27)
                                .addComponent(jlDimensionTitle)
                                .addGap(18, 18, 18)
                                .addComponent(jlRoundedDimension, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(134, Short.MAX_VALUE))
                    .addComponent(jTextField1, javax.swing.GroupLayout.Alignment.TRAILING)))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addComponent(lock, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(jcUnit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jsWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jlWidthUnit)
                                    .addComponent(jLabel3))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jsHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jlHeightUnit)
                                    .addComponent(jLabel4)))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel5)
                                    .addComponent(jsWidthRatio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel6)
                                    .addComponent(jsHeightRatio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                .addGap(18, 18, 18)
                .addComponent(jcbRatio)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jcbTrans)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jlDimensionTitle))
                    .addComponent(jlRoundedDimension, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(58, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jcbRatioItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jcbRatioItemStateChanged
        //jika diselect, maka akan mengembalikan ke size mula-mula (this.dim)
        //sehingga ratio kembali ke 100%-100%
        //trus selama ini diselect, angka persentasi di lock biar sama terus,
        //terlepas dari perubahan dilakukan di spinner yang mana
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            //reset ratio to 100-100, will automatically updates width/height spinners
            jsWidthRatio.setValue(100);
            jsHeightRatio.setValue(100);
            jsWidthRatio.addChangeListener(binderH);
            jsHeightRatio.addChangeListener(binderW);
            lock.setText("]");
        } else {
            jsWidthRatio.removeChangeListener(binderH);
            jsHeightRatio.removeChangeListener(binderW);
            lock.setText("");
        }
    }//GEN-LAST:event_jcbRatioItemStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JComboBox jcUnit;
    private javax.swing.JCheckBox jcbRatio;
    private javax.swing.JCheckBox jcbTrans;
    private javax.swing.JLabel jlDimensionTitle;
    private javax.swing.JLabel jlHeightUnit;
    private javax.swing.JLabel jlRoundedDimension;
    private javax.swing.JLabel jlWidthUnit;
    private javax.swing.JSpinner jsHeight;
    private javax.swing.JSpinner jsHeightRatio;
    private javax.swing.JSpinner jsWidth;
    private javax.swing.JSpinner jsWidthRatio;
    private javax.swing.JLabel lock;
    // End of variables declaration//GEN-END:variables

    public final void setDimension(Dimension size) {
        jsWidth.setValue(size.width);
        jsHeight.setValue(size.height);
    }

    public Dimension getResultDimension() {
        //convert width and height to pixel
        double w = (double) jsWidth.getModel().getValue();
        double h = (double) jsHeight.getModel().getValue();
        Unit currentUnit = (Unit) jcUnit.getSelectedItem();

        double wp = Amount.valueOf(w, currentUnit).to(NonSI.PIXEL).getEstimatedValue();
        double hp = Amount.valueOf(h, currentUnit).to(NonSI.PIXEL).getEstimatedValue();
        return new Dimension((int) Math.round(wp), (int) Math.round(hp));

    }

    public boolean getResultTransparent() {
        return jcbTrans.isSelected();
    }

    private class AspectRatioBinder implements ChangeListener {

        private JSpinner oppositeRatio;

        public AspectRatioBinder(JSpinner js) {
            this.oppositeRatio = js;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            JSpinner source = (JSpinner) e.getSource();
            oppositeRatio.setValue(source.getValue());
        }
    }

    private class FindMaxListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            double w = (Double) jsWidth.getModel().getValue();
            double h = (Double) jsHeight.getModel().getValue();
            double r = Math.max(w, h);
            DecimalFormat rounder;
            if (jcUnit.getSelectedItem().equals(NonSI.PIXEL)) {
                rounder = new DecimalFormat("#");
            } else {
                rounder = new DecimalFormat("#.##",DotDecimalFormat.getSymbols());
            }
            String round = rounder.format(r);
            jlRoundedDimension.setText(round + " x " + round);
        }

    }

    public void setUnitInfoDimension() {
        DecimalFormat rounder;
        rounder = new DecimalFormat("#");
        double h = Double.parseDouble(jsHeight.getValue().toString());
        double w = Double.parseDouble(jsWidth.getValue().toString());
        jlRoundedDimension.setText(rounder.format(w) + " x " + (rounder.format(h) + " " + jcUnit.getSelectedItem().toString()));
    }

    public void loadImage(boolean withBackground) {
        double h = Double.parseDouble(jsHeight.getValue().toString());
        double w = Double.parseDouble(jsWidth.getValue().toString());
        //ini kan cuma preview image, ga usah minta gede2 lah
        if (h > 1000 || w > 1000) {
            double max = Math.max(w, h);
            double ratio = 1000 / max;
            h *= ratio;
            w *= ratio;
        }
        Image img = exportimg.getPreviewImage((int) w, (int) h, withBackground);
        jPanel1.removeAll();
        jPanel1.add(new ImagePanel(img, withBackground));
        jPanel1.revalidate();
        revalidate();
        repaint();
    }

    private class ImagePanel extends JPanel {

        private Image img;
        boolean trans;
        int x, y, wres, hres;

        private ImagePanel(Image img, boolean bg) {
            this.img = img;
            this.trans = !bg;
            Dimension size = new Dimension(256, 256);
            setPreferredSize(size);
            setMinimumSize(size);
            setMaximumSize(size);
            setSize(size);
            setLayout(null);
        }

        @Override
        public void paintComponent(Graphics g) {
            double oriw = (double) jsWidth.getValue();
            double orih = (double) jsHeight.getValue();
            double w = 256;
            double h = 256;

            boolean maintainRatio = true;

            double oriRat = (oriw * 1.0) / (orih * 1.0);
            double resRat = (w * 1.0) / (h * 1.0);

            if (!maintainRatio || oriRat == resRat) {
                x = 0;
                y = 0;
                wres = (int) w;
                hres = (int) h;
            } else if (resRat > oriRat) {
                y = 0;
                hres = (int) h;
                wres = (int) (oriw * h / orih);
                x = (int) ((w - wres) / 2);
            } else {
                x = 0;
                wres = (int) w;
                hres = (int) (orih * w / oriw);
                y = (int) ((h - hres) / 2);
            }
            g.translate(x, y);
            //draw rect grid for image with transparent bg
            if (trans) {
                int s = 5; //nilai sisi
                int colnum = wres / s;
                int rownum = hres / s;
                for (int a = 0; a <= colnum; a++) {

                    for (int b = 0; b <= rownum; b++) {

                        if (((a + b) & 1) == 0) {
                            g.setColor(Color.WHITE);
                        } else {
                            g.setColor(Color.LIGHT_GRAY);
                        }
                        g.fillRect(a * s, b * s, a == colnum ? (wres - s * colnum) : s, b == rownum ? (hres - s * rownum) : s);
                    }
                }
            }
            g.drawImage(img, 0, 0, wres, hres, null);
            g.dispose();
        }

    }

}
