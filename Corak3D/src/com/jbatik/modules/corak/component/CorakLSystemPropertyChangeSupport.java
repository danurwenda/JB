/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.corak.component;

import com.jbatik.modules.corak.CorakLSystem;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 *
 * @author Dimas Y. Danurwenda
 */
public class CorakLSystemPropertyChangeSupport {

    PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(listener);
    }
    CorakLSystem cor;
        //delegating methods to wrapped structure
    //from the most basal class
    //LSystem.iteration

    public CorakLSystemPropertyChangeSupport(CorakLSystem cor) {
        this.cor = cor;
    }

    public int getIteration() {
        return cor.getIteration();
    }

    public void setIteration(int iteration) {
        int old = getIteration();
        cor.setIteration(iteration);
        pcs.firePropertyChange(CorakLSystem.ITERATION_PROP, old, iteration);
    }
    //VisualLSystem.angle

    public int getAngle() {
        return cor.getAngle();
    }

    public void setAngle(int angle) {
        int old = getAngle();
        cor.setAngle(angle);
        pcs.firePropertyChange(CorakLSystem.ANGLE_PROP, old, angle);
    }
    //VisualLSystem.length

    public int getLength() {
        return cor.getLength();
    }

    public void setLength(int angle) {
        int old = getLength();
        cor.setLength(angle);
        pcs.firePropertyChange(CorakLSystem.LENGTH_PROP, old, angle);
    }

    //VisualLSystem.width
    public int getWidth() {
        return cor.getWidth();
    }

    public void setWidth(int angle) {
        int old = getWidth();
        cor.setWidth(angle);
        pcs.firePropertyChange(CorakLSystem.WIDTH_PROP, old, angle);
    }
}
