/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jbatik.modules.interfaces;

import com.jbatik.core.api.SceneObserverCookie;



/**
 *
 * @author RAPID01
 */
public interface Scene3DObserverCookie extends SceneObserverCookie{
    
    public enum Projection {
        PARALLEL, PERSPECTIVE
    }
        
    /**
     * Turn x, y, z axis helper lines on (true) or off( false)
     * @param enable 
     */
    public void setAxisLines(boolean enable);
    
    /**
     * Get the state of the axis helper lines, true if on
     * @return
     */
    public boolean getAxisLinesEnabled();
    
    /**
     * Set the projection mode to Scene3DObserverCookie.Projection.PERSPECTIVE
     * or Scene3DObserverCookie.Projection.PARALLEL
     * @param mode 
     */
    public void setProjectionMode(Projection mode);
    
    public Projection getProjectionMode();
    
    /**
     * pick a point to be a rotation center
     * @param enable 
     */
    public void setChoosePivotPointMode(boolean enable);
    
    public boolean getChoosePivotPointModeEnabled();
}
