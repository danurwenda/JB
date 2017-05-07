/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.imageeditor.jhfilter;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Dimas Y. Danurwenda
 */
public class MapColorsWithToleranceFilter extends MapColorsFilter {

    private int tolerance;
    private List<Pixel> changedPixel;

    public List<Pixel> getChangedPixel() {
        return changedPixel;
    }

    public MapColorsWithToleranceFilter(int target, int replacement, int tol) {
        super(target, replacement);
        this.tolerance = tol;
        changedPixel  = new ArrayList<>();
    }

    @Override
    public int filterRGB(int x, int y, int rgb) {
        int a = (rgb >> 24) & 0xFF;
        if (a > 0) {
            if (nearColors(rgb, oldColor, tolerance)) {
                changedPixel.add(new Pixel(x,y,rgb));
                return newColor;
            }
        }
        return rgb;
    }

    public static boolean nearColors(int rgb1, int rgb2, int tolerance) {
        int r1 = (rgb1 >> 16) & 0xff;
        int g1 = (rgb1 >> 8) & 0xff;
        int b1 = rgb1 & 0xff;
        int r2 = (rgb2 >> 16) & 0xff;
        int g2 = (rgb2 >> 8) & 0xff;
        int b2 = rgb2 & 0xff;
        return Math.abs(r1 - r2) <= tolerance && Math.abs(g1 - g2) <= tolerance && Math.abs(b1 - b2) <= tolerance;
    }
}
