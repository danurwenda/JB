/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.lsystem.api.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.SwingPropertyChangeSupport;

/**
 * SpinnerNumberModel with PropertyChangeSupport
 * @author Dimas Y. Danurwenda
 */
public class MySpinnerNumberModel extends SpinnerNumberModel {

    public static final String VALUE = "value";
    private SwingPropertyChangeSupport pcs = new SwingPropertyChangeSupport(this);

   // you will likely need to create multiple constructors to match
    // the ones available to the SpinnerNumberModel class
    public MySpinnerNumberModel(int value, int min, int max, int step) {
        super(value, min, max, step);
    }

    public MySpinnerNumberModel(Number value, Comparable min, Comparable max, Number step) {
        super(value, min, max, step);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    @Override
    public void setValue(Object newValue) {
        // store old value and set the new one
        Object oldValue = getValue();
        super.setValue(newValue);

        // construct the event object using these saved values
        PropertyChangeEvent evt = new PropertyChangeEvent(this, VALUE, oldValue,
                newValue);

        // notify all of the listeners
        pcs.firePropertyChange(evt);
    }
}
