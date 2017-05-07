/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.cor.navigator.table.editor;

import com.jbatik.cor.navigator.funiteditor.FUnitEditorPanel;
import com.jbatik.lsystem.turtle.Surface;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.vecmath.Point3d;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author risman
 */
public class FUnitPreviewEditor extends AbstractCellEditor implements TableCellEditor {

    JButton button = new JButton();
    Surface surface;
    protected static final String EDIT = "edit";

    public FUnitPreviewEditor() {

        button.setActionCommand(EDIT);
        button.setBorderPainted(false);

        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                Point3d[] oldPoints = new Point3d[surface.pointsNum()];
                for (int i=0; i<surface.pointsNum(); i++) {
                    oldPoints[i] = new Point3d(surface.getPoints()[i]);
                }

                JButton saveButton = new JButton("Save");
                JButton discardButton = new JButton("Discard");
                JButton helpButton = new JButton("Help");

                FUnitEditorPanel funitEditorPanel = new FUnitEditorPanel(surface);
                DialogDescriptor dd = new DialogDescriptor(funitEditorPanel, "Edit FUnit: " + surface.getName());
                dd.setOptions(new Object[]{saveButton, discardButton, helpButton});

                Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
                dialog.setModal(true);
                dialog.setLocationByPlatform(true);
                dialog.setSize(640, 640);

                dd.setButtonListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (e.getSource() == saveButton) {
                            Object save = DialogDisplayer.getDefault().notify(new NotifyDescriptor("Save this FUnit?", "Confirmation", NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.PLAIN_MESSAGE, null, null));
                            if (save == NotifyDescriptor.OK_OPTION) {
                                surface = funitEditorPanel.getFUnit(); //save funit
                                dialog.setVisible(false);
                                dialog.removeAll();
                                fireEditingStopped();
                            }
                        } else if (e.getSource() == discardButton) {
                            Object discard = DialogDisplayer.getDefault().notify(new NotifyDescriptor("Discard the changes?", "Confirmation", NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.WARNING_MESSAGE, null, null));
                            if (discard == NotifyDescriptor.OK_OPTION) {
                                surface.setPoints(oldPoints);
                                dialog.setVisible(false);
                                dialog.removeAll();
                                fireEditingCanceled();
                            }
                        } else if (e.getSource() == helpButton) {
                            DialogDisplayer.getDefault().notify(new NotifyDescriptor("Click a vertex to select,\n"
                                    + "then drag it to move them around\n"
                                    + "or double-click it to edit the coordinates manually.\n"
                                    + "Press [Delete] on keyboard to remove selected vertex.\n"
                                    + "Double-click on a line to add new vertex on it.",
                                    "Instruction", NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.PLAIN_MESSAGE, new Object[]{NotifyDescriptor.OK_OPTION}, null));
                        }
                    }
                });

                dd.setNoDefaultClose(true); //prevent auto close after click x button
                dd.setClosingOptions(new Object[]{}); //prevent auto close after choose save/cancel options

                dd.addPropertyChangeListener(new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent e) {
                        if (e.getPropertyName().equals(NotifyDescriptor.PROP_VALUE)) {
                            if (e.getNewValue().equals(NotifyDescriptor.CLOSED_OPTION)) { //show confirmation when click x button
                                Object close = DialogDisplayer.getDefault().notify(new NotifyDescriptor("Discard the changes?", "Confirmation", NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.WARNING_MESSAGE, null, null));
                                if (close == NotifyDescriptor.OK_OPTION) {
                                    surface.setPoints(oldPoints);
                                    dialog.setVisible(false);
                                    dialog.removeAll();
                                    fireEditingCanceled();
                                } else {
                                    dd.setValue(NotifyDescriptor.CANCEL_OPTION);
                                }
                            }
                        }
                    }
                });

                dialog.setVisible(true);
            }

        });
    }

    @Override
    public Object getCellEditorValue() {
        return surface;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        surface = (Surface) value;
        return button;
    }

}
