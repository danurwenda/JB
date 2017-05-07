/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.cor.navigator.table;

import java.util.Vector;
import javax.swing.table.DefaultTableModel;

/**
 * Table model untuk menampilkan list values dengan index terurut (1 s.d
 * banyaknya elemen) di kolom paling kiri serta ada tombol delete di kolom
 * paling kanan. Banyaknya value di antara dua kolom tersebut bebas.
 *
 * Penanganan khusus dilakukan saat REORDER, DELETE, dan ADD agar penomoran di
 * kolom paling kiri tetap konsisten.
 *
 * @author RAPID02
 */
public class OrderedIndexTableModel extends DefaultTableModel {
//SHOULD WE EXTEND AbstractTableModel instead?

    /**
     * Kolom nomor di paling kiri
     */
    public final static String columnNo = "No";
    /**
     * Kolom action di paling kanan. Biasanya action ini isinya delete, karena
     * untuk edit langsung di value columns nya pakai cell editor.
     */
    public final static String columnAction = "";
    
    public OrderedIndexTableModel(Object[] valueColumns) {
        addColumn(columnNo);
        if (valueColumns != null) {
            for (Object valueColumn : valueColumns) {
                addColumn(valueColumn);
            }
        }
        addColumn(columnAction);
    }
    
    protected final void adjustNumber(int from, int to) {
        for (int row = from; row <= to; row++) {
            setValueSilent(row + 1, row, 0);
        }
    } 
    
    @Override
    public void removeRow(int i) {
        super.removeRow(i);
        //ADJUST NUMBER on leftmost column from deleted row until the last element
        adjustNumber(i, getRowCount() - 1);
    }
    
    @Override
    public void moveRow(int i, int i1, int i2) {
        super.moveRow(i, i1, i2);
        if (i2 < i) {
            adjustNumber(i2, i1);
        } else {
            adjustNumber(i, i2 + i1 - i);
        }
    }
    
    @Override
    public void insertRow(int i, Object[] os) {
        super.insertRow(i, os);
        //ADJUST NUMBER on leftmost column from inserted row until the last element
        adjustNumber(i, getRowCount() - 1);
    }
    
    @Override
    public void insertRow(int i, Vector vector) {
//        System.err.println("insertRow vector");
        super.insertRow(i, vector);
        //ADJUST NUMBER on leftmost column from inserted row until the last element
        adjustNumber(i, getRowCount() - 1);
    }
    
    @Override
    public boolean isCellEditable(int row, int column) {
        //kolom pertama dan terakhir ga boleh editable
        if (column == 0) {
            return false;
        } else {
            return super.isCellEditable(row, column);
        }
    }

    private void setValueSilent(Object v, int row, int col) {
        Vector rowVector = (Vector) dataVector.elementAt(row);
        rowVector.setElementAt(v, col);
    }
    
}
