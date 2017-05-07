/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jbatik.modules.corak.appearance;

import javax.media.j3d.Appearance;
import javax.media.j3d.Material;
import javax.media.j3d.PolygonAttributes;

/**
 *
 * @author Dimas Y. Danurwenda <dimas@danurwenda.com>
 */
public class SolidAppearance extends Java3dAppearanceProvider{

    @Override
    public boolean supportMaterial() {
        return true;
    }

    @Override
    public Appearance getAppearance(boolean withMaterial) {
        Appearance solidAppear = new Appearance();
        PolygonAttributes polyAttrib = new PolygonAttributes();
        polyAttrib.setCullFace(PolygonAttributes.CULL_NONE);
        polyAttrib.setBackFaceNormalFlip(true);
        solidAppear.setPolygonAttributes(polyAttrib);

        if (withMaterial) {
            Material material = new Material();
            solidAppear.setMaterial(material);
        }
        solidAppear.setCapability(Appearance.ALLOW_MATERIAL_READ);
        solidAppear.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
        solidAppear.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_READ);
        solidAppear.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_WRITE);
        solidAppear.setCapability(Appearance.ALLOW_TEXTURE_READ);
        solidAppear.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
        
        return solidAppear;
    }

    @Override
    public String getDisplayName() {
        return "Solid"; //NOI18N
    }

    @Override
    public boolean supportIndex() {
        return false;
    }

}
