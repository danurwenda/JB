/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.cor.navigator.table.renderer;

import com.jbatik.cor.navigator.PicturePresenter;
import java.awt.Component;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Nanta Es <kedipkedip@ymail.com>
 */
public class RemoveCellRenderer implements TableCellRenderer {
    private JButton jButton = new JButton();
    
    public RemoveCellRenderer(){
        PicturePresenter.setPicture_Remove(jButton);
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row, int column) {
        
        return jButton;
    }
}