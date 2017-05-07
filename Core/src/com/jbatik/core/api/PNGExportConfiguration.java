/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.core.api;

/**
 *
 * @author Dimas Y. Danurwenda
 */
public class PNGExportConfiguration {

    private boolean forceTransparency;
    private boolean lockedRatio;

    public boolean isForceTransparency() {
        return forceTransparency;
    }

    public boolean isLockedRatio() {
        return lockedRatio;
    }

    

    /**
     * 
     * @param trans if true than the resulting image must have a transparent background
     * @param lockedRatio if true than the width/height ratio is locked
     */
    public PNGExportConfiguration(boolean trans, boolean lockedRatio) {
        this.forceTransparency = trans;
        this.lockedRatio = lockedRatio;
    }

    public PNGExportConfiguration() {
        this(false, false);
    }

}
