/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jbatik.core.api;

import org.openide.nodes.Node.Cookie;

/**
 * Point to point measurement.
 * Interaction: user turn on measurement mode in menu/toolbar, then in the next
 * click, user put the starting point for the measurement. Next click, end point.
 * Next click, starting point again. And so on after he turns off the measurement
 * mode.
 * @author RAPID01
 */
public interface MeasurableCookie extends Cookie {
    
    /**
     * Implement the measurement mode here. Stop current mouse click event listener,
     * declare, define, and activate a new click listener to actually do the measurement
     * action. 
     * @param enabled
     */
    public void setMeasurementMode(boolean enabled);
    
    public boolean getMeasurementModeEnabled();
    
    public void setMeasurementVisible(boolean visible);
    
    public boolean getMeasurementVisible();
    
    public void clearAllMeasurement();
    
}
