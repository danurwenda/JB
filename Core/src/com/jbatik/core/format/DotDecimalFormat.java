/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.core.format;

import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 *
 * @author Slurp
 */
public class DotDecimalFormat {

    public static DecimalFormatSymbols getSymbols() {
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
        otherSymbols.setGroupingSeparator('.');
        return otherSymbols;
    }
}
