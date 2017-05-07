/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jbatik.core.api;
import java.awt.Color;

/**
 *
 * @author RAPID01
 */
public interface SceneObserverCookie extends FitView{
    
    /**
     * Change background color of the view
     * @param newColor
     */
    public void changeBackgroundColor(Color newColor);
    
    /**
     * 
     * @return current background color
     */
    public Color getBackgroundColor();
}
