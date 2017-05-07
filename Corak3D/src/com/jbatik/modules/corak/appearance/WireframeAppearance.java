/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.corak.appearance;

import javax.media.j3d.Appearance;
import javax.media.j3d.PolygonAttributes;

/**
 *
 * @author Dimas Y. Danurwenda <dimas@danurwenda.com>
 */
public class WireframeAppearance extends Java3dAppearanceProvider {

    @Override
    public boolean supportMaterial() {
        return false;
    }

    @Override
    public Appearance getAppearance(boolean withMaterial) {
        Appearance wireAppear = new Appearance();

        wireAppear.setCapability(Appearance.ALLOW_MATERIAL_READ);
        wireAppear.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
        wireAppear.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_READ);
        wireAppear.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_WRITE);
        wireAppear.setCapability(Appearance.ALLOW_TEXTURE_READ);
        wireAppear.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
        
        PolygonAttributes polyAttrib = new PolygonAttributes();
        polyAttrib.setPolygonMode(PolygonAttributes.POLYGON_LINE);
        polyAttrib.setCullFace(PolygonAttributes.CULL_NONE);
        wireAppear.setPolygonAttributes(polyAttrib);

        return wireAppear;
    }

    @Override
    public String getDisplayName() {
        return "Wireframe"; //NOI18N
    }

    @Override
    public boolean supportIndex() {
        return false;
    }

}
