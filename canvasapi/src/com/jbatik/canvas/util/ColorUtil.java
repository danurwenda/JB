/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.canvas.util;

import java.awt.Color;

/**
 *
 * @author Dimas Y. Danurwenda
 */
public class ColorUtil {

    public static Color getContrastYIQ(Color c) {
        double yiq = ((c.getRed() * 299) + (c.getGreen() * 587) + (c.getBlue() * 114)) / 1000;
        return (yiq >= 128) ? Color.black : Color.white;
    }
}
