/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.corak;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Dimas Y. Danurwenda
 */
public class CorakUtil {

    /**
     * Menyamakan jumlah index color dan texture.
     * @param cor CorakLSystem yang mau disamakan color dan texture nya
     */
    public static void syncColorsAndTextures(CorakLSystem cor) {
        List<Color> cs = cor.getColors();

        List<String> ts = cor.getTextures();

        //iterate over two lists, bandingin dulu jumlahnya sama ato engga
        List<Color> colorFix = new ArrayList<>();
        List<String> textureFix = new ArrayList<>();
        Iterator<Color> colorIt = cs.iterator();
        Iterator<String> textIt = ts.iterator();
        while (colorIt.hasNext() || textIt.hasNext()) {
            if (!colorIt.hasNext()) {
                        //texture lebih panjang daripada color
                //add default color
                colorFix.add(CorakLSystem.DEFAULT_COLOR);
                textureFix.add(textIt.next());
            } else if (!textIt.hasNext()) {
                        //color lebih panjang daripada texture
                //add default texture
                colorFix.add(colorIt.next());
                textureFix.add(CorakLSystem.DEFAULT_TEXTURE);
            }else{
                //ada dua2nya
                colorFix.add(colorIt.next());
                textureFix.add(textIt.next());
            }
        }
        cor.setColor(colorFix);
        cor.setTextures(textureFix);
    }
}
