/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jbatik.modules.corak.sunflowtools;

import org.sunflow.SunflowAPI;
import org.sunflow.core.ParameterList;
import org.sunflow.core.ShadingState;
import org.sunflow.core.Texture;
import org.sunflow.core.TextureCache;
import org.sunflow.core.shader.TexturedPhongShader;
import org.sunflow.image.Color;

/**
 *
 * @author RAPID01
 */
public class MyTexturedConstantShader extends TexturedPhongShader {
    
    private Texture tex;
    
    public MyTexturedConstantShader() {
        tex = null;
    }

    @Override
    public Color getDiffuse(ShadingState state) {
          return tex.getPixel(state.getUV().x, state.getUV().y);
    }

    @Override
    public boolean update(ParameterList pl, SunflowAPI api) {
        String filename = pl.getString("texture", null);
        if (filename != null)
            tex = TextureCache.getTexture(api.resolveTextureFilename(filename), false);
        return tex != null && super.update(pl, api);
    }

    @Override
    public void scatterPhoton(ShadingState state, Color power) {}

    @Override
    public Color getRadiance(ShadingState state) {
        return tex.getPixel(state.getUV().x, state.getUV().y);
    }
    
}
