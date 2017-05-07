/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jbatik.modules.corak.appearance;

import javax.media.j3d.Appearance;

/**
 *
 * @author Dimas Y. Danurwenda <dimas@danurwenda.com>
 */
public abstract class Java3dAppearanceProvider {
    public abstract boolean supportMaterial();
    
    public abstract boolean supportIndex();

    public Appearance getAppearance(){
        return getAppearance(false);
    };
    
    public abstract Appearance getAppearance(boolean withMaterial);
    
    public abstract String getDisplayName();
}
