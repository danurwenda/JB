/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.component;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JPopupMenu;
import org.openide.awt.MouseUtils;

/**
 *
 * @author Dimas Y. Danurwenda
 */
class UnitMenus extends MouseUtils.PopupMouseAdapter {

    JPopupMenu unitOptions;

    JRadioButtonMenuItem pixels = new JRadioButtonMenuItem("Pixels");
    JRadioButtonMenuItem inches = new JRadioButtonMenuItem("Inches");
    JRadioButtonMenuItem centimeters = new JRadioButtonMenuItem("Centimeters");
    JRadioButtonMenuItem millimeters = new JRadioButtonMenuItem("Millimeters");
    JRadioButtonMenuItem percent = new JRadioButtonMenuItem("Percent");
    ButtonGroup units = new ButtonGroup();

    public UnitMenus() {
        units.add(pixels);
        units.add(inches);
        units.add(centimeters);
        units.add(millimeters);
        units.add(percent);
        units.setSelected(pixels.getModel(), true);
    }

    @Override
    protected void showPopup(MouseEvent evt) {
        if (unitOptions == null) {
            unitOptions = new JPopupMenu();
            //hardcode aja ya, opsinya juga terbatas
            //1. pixels
            unitOptions.add(pixels);
            //2. inches
            unitOptions.add(inches);
            //3. centimeters
            unitOptions.add(centimeters);
            //4. millimeters
            unitOptions.add(millimeters);
            //5. percent
//                unitOptions.add(percent);
        }

        unitOptions.show((Component) evt.getSource(), evt.getX(), evt.getY());
    }

    void addActionListener(ActionListener horizontal) {
        pixels.addActionListener(horizontal);
        inches.addActionListener(horizontal);
        centimeters.addActionListener(horizontal);
        millimeters.addActionListener(horizontal);
    }

    void setSelectedUnit(Unit unit) {
        if (unit.equals(NonSI.PIXEL)) {
            pixels.doClick();
        } else if (unit.equals(NonSI.INCH)) {
            inches.doClick();
        } else if (unit.equals(SI.MILLIMETER)) {
            millimeters.doClick();
        } else if (unit.equals(SI.CENTIMETER)) {
            centimeters.doClick();
        }
    }
}
