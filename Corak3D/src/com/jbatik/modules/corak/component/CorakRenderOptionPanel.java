/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.corak.component;

/**
 *
 * @author RAPID02
 */
public class CorakRenderOptionPanel extends javax.swing.JPanel {

    public static final int PERFORMANCE = 835;
    public static final int QUALITY = 815;

    /**
     * Creates new form CorakRenderOptionPanel
     */
    public CorakRenderOptionPanel() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        renderingBGroup = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        performanceRB = new javax.swing.JRadioButton();
        qualityRB = new javax.swing.JRadioButton();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(CorakRenderOptionPanel.class, "CorakRenderOptionPanel.jLabel1.text")); // NOI18N

        renderingBGroup.add(performanceRB);
        performanceRB.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(performanceRB, org.openide.util.NbBundle.getMessage(CorakRenderOptionPanel.class, "CorakRenderOptionPanel.performanceRB.text")); // NOI18N

        renderingBGroup.add(qualityRB);
        org.openide.awt.Mnemonics.setLocalizedText(qualityRB, org.openide.util.NbBundle.getMessage(CorakRenderOptionPanel.class, "CorakRenderOptionPanel.qualityRB.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(qualityRB)
                            .addComponent(performanceRB))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(performanceRB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(qualityRB)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JRadioButton performanceRB;
    private javax.swing.JRadioButton qualityRB;
    private javax.swing.ButtonGroup renderingBGroup;
    // End of variables declaration//GEN-END:variables

    int getQuality() {
        if (performanceRB.isSelected()) {
            return PERFORMANCE;
        } else if (qualityRB.isSelected()) {
            return QUALITY;
        }
        return 0;
    }
}