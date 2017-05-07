/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jbatik.modules.interfaces;

import org.openide.nodes.Node.Cookie;

/**
 *
 * @author RAPID01
 */
public interface Appearance3DChangerCookie extends Cookie {
    
    public enum Appearance {
        WIREFRAME, SOLID, TEXTURE
    }
    
    /**
     * Change appearance of all the objects to either Appearance.WIREFRAME,
     * Appearance.SOLID, or Appearance.TEXTURE
     * @param appearance 
     */
    public void changeAppearance(Appearance appearance);
    
    /**
     * 
     * @return current appearance
     */
    public Appearance getCurrentAppearance();
    
    /**
     * Turn lights on (true) or off (false).
     * @param enabled 
     */
    public void setLights(boolean enabled);
    
    /**
     * 
     * @return whether the lights are on or off
     */
    public boolean getLightsEnabled();
}
