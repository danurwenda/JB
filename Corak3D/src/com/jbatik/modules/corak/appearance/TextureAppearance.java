/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.corak.appearance;

import com.sun.j3d.utils.image.TextureLoader;
import java.io.File;
import java.net.MalformedURLException;
import javax.media.j3d.Appearance;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.Material;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Texture;
import javax.media.j3d.Texture2D;
import org.openide.util.Utilities;

/**
 *
 * @author Dimas Y. Danurwenda <dimas@danurwenda.com>
 */
public class TextureAppearance extends Java3dAppearanceProvider {

    @Override
    public boolean supportMaterial() {
        return true;
    }

    @Override
    public Appearance getAppearance(boolean withMaterial) {
        return getAppearance(null, withMaterial);
    }

    public Appearance getAppearance(File textureFile, boolean withMaterial) {
        if (textureFile == null) {
            return null;
        } else {
            try {
                TextureLoader loader = new TextureLoader(Utilities.toURI(textureFile).toURL(), null);
                ImageComponent2D image = loader.getImage();
                Texture2D texture = new Texture2D(Texture.BASE_LEVEL, Texture.RGB,
                        image.getWidth(), image.getHeight());
                texture.setImage(0, image);
                texture.setEnable(true);
                texture.setMagFilter(Texture.BASE_LEVEL_LINEAR);
                texture.setMinFilter(Texture.BASE_LEVEL_LINEAR);

                Appearance texturedApp = new Appearance();
                texturedApp.setTexture(texture);
                PolygonAttributes polyAttrib = new PolygonAttributes();
                polyAttrib.setCullFace(PolygonAttributes.CULL_NONE);
                polyAttrib.setBackFaceNormalFlip(true);
                texturedApp.setPolygonAttributes(polyAttrib);

                if (withMaterial) {
                    Material material = new Material();
                    texturedApp.setMaterial(material);
                }
                texturedApp.setCapability(Appearance.ALLOW_MATERIAL_READ);
                texturedApp.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
                texturedApp.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_READ);
                texturedApp.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_WRITE);
                texturedApp.setCapability(Appearance.ALLOW_TEXTURE_READ);
                texturedApp.setCapability(Appearance.ALLOW_TEXTURE_WRITE);

                return texturedApp;
            } catch (MalformedURLException murlex) {
                return null;
            }
        }
    }

    @Override
    public String getDisplayName() {
        return "Textured"; //NOI18N
    }

    @Override
    public boolean supportIndex() {
        return true;
    }

}
