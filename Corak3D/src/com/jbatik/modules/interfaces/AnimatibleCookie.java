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
public interface AnimatibleCookie extends Cookie {
    
    public boolean isAnimatingX();
    
    public boolean isAnimatingY();
    
    public boolean isAnimatingZ();
    
    public void initAnimationThread();
    
    public void stopAnimation();
    
    public void toggleXAnimation();
    
    public void toggleYAnimation();
    
    public void toggleZAnimation();
}
