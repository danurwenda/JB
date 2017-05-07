/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.imageeditor.jhfilter;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * A filter that change the color of pixels in specific coordinates.
 * @author Dimas Y. Danurwenda
 */
public class CoordinatedPixelColorFilter extends AbstractBufferedImageOp {

    protected boolean canFilterIndexColorModel = false;
    List<Pixel> changed;
    int newColor;

    public CoordinatedPixelColorFilter(List<Pixel> changedPixel) {
        this.changed = changedPixel;
        this.newColor = -1;
    }

    public CoordinatedPixelColorFilter(List<Pixel> changedPixel, int newColor) {
        this.changed = changedPixel;
        this.newColor = newColor;
    }

    @Override
    public BufferedImage filter(BufferedImage src, BufferedImage dst) {
        dst = createCompatibleDestImage(src, null);
        dst.setData(src.getData());
        if (newColor != -1) {
            for (Pixel p : changed) {
                dst.setRGB(p.x, p.y, newColor);
            }
        } else {
            for (Pixel p : changed) {
                dst.setRGB(p.x, p.y, p.color);
            }
        }
        return dst;
    }
}
